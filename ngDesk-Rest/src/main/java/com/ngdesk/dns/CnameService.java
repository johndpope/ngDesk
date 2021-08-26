package com.ngdesk.dns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.ngdesk.Authentication;
import com.ngdesk.Global;
import com.ngdesk.exceptions.BadRequestException;
import com.ngdesk.exceptions.ForbiddenException;
import com.ngdesk.exceptions.InternalErrorException;
import com.ngdesk.roles.RoleService;

@Component
@RestController
public class CnameService {

	@Autowired
	Global global;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Authentication auth;

	@Autowired
	RoleService roleService;
	
	@Value("${cert.pk}")
	private String pk;

	private final Logger log = LoggerFactory.getLogger(CnameService.class);

	@GetMapping("/companies/dns/cname")
	public ResponseEntity<Object> getCname(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String subdomain = userDetails.getString("COMPANY_SUBDOMAIN");
			String role = userDetails.getString("ROLE");
			String companyId = userDetails.getString("COMPANY_ID");

			MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
			Document document = dnsRecordsCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			if (document == null) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String cname = "";
			if (document.containsKey("CNAME") && document.get("CNAME") != null) {
				cname = document.getString("CNAME");
			}

			JSONObject resultJson = new JSONObject();
			resultJson.put("CNAME", cname);

			return new ResponseEntity<>(resultJson.toString(), HttpStatus.OK);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/companies/dns/cname/challenge/{challenge_token}")
	public ResponseEntity<Object> getChallengeToken(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("challenge_token") String challengeToken) {

		try {

			String cname = request.getAttribute("CNAME").toString();
			log.trace(
					"Enter CnameService.getChallengeToken() cname=" + cname + " , challenge_token = " + challengeToken);
			MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
			Document document = dnsRecordsCollection
					.find(Filters.and(Filters.eq("CNAME", cname), Filters.eq("CHALLENGE_TOKEN", challengeToken)))
					.first();

			if (document == null) {
				throw new BadRequestException("SUBDOMAIN_NOT_EXIST");
			}
			if (document.containsKey("CHALLENGE_AUTHORIZATION") && document.get("CHALLENGE_AUTHORIZATION") != null) {
				response.setContentType("text/plain");
				return new ResponseEntity<>(document.getString("CHALLENGE_AUTHORIZATION"), HttpStatus.OK);
			}
			throw new BadRequestException("CHALLENGE_AUTHORIZATION_MISSING");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@GetMapping("/companies/dns/cname/certificate")
	public ResponseEntity<Object> getCert(@RequestParam("cname") String cname, @RequestParam("secret") String secret) {
		try {

			log.trace("Enter CnameService.getCert() cname=" + cname);

			if (!pk.equals(secret)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
			List<String> fieldNames = new ArrayList<String>();
			fieldNames.add("CERTIFICATE");

			Document document = dnsRecordsCollection.find(Filters.eq("CNAME", cname))
					.projection(Filters.and(Projections.include(fieldNames), Projections.excludeId())).first();

			if (document == null) {
				throw new ForbiddenException("DNS_RECORD_NOT_FOUND");
			}

			if (document.containsKey("CERTIFICATE")) {
				return new ResponseEntity<>(document.getString("CERTIFICATE"), HttpStatus.OK);
			} else {
				throw new BadRequestException("CERT_MISSING");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@GetMapping("/companies/dns/cname/private_key")
	public ResponseEntity<Object> getCsr(@RequestParam("cname") String cname, @RequestParam("secret") String secret) {
		try {

			log.trace("Enter CnameService.getCsr() cname=" + cname);

			if (!pk.equals(secret)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
			List<String> fieldNames = new ArrayList<String>();
			fieldNames.add("PRIVATE_KEY");

			Document document = dnsRecordsCollection.find(Filters.eq("CNAME", cname))
					.projection(Filters.and(Projections.include(fieldNames), Projections.excludeId())).first();

			if (document == null) {
				throw new ForbiddenException("DNS_RECORD_NOT_FOUND");
			}

			if (document.containsKey("PRIVATE_KEY")) {
				return new ResponseEntity<>(document.getString("PRIVATE_KEY"), HttpStatus.OK);
			} else {
				throw new BadRequestException("CERT_MISSING");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");

	}

	@PostMapping("/companies/dns/cname")
	public ResponseEntity<Object> createCname(@RequestParam("cname") String cname,
			@RequestParam("authentication_token") String uuid, HttpServletRequest request) {
		String token;
		log.trace("Enter CnameService.createCname() cname=" + cname);
		if (request.getHeader("authentication_token") != null) {
			token = request.getHeader("authentication_token").toString();
		} else {
			token = uuid;
		}
		JSONObject userDetails = auth.getUserDetails(token);
		String subdomain = userDetails.getString("COMPANY_SUBDOMAIN");
		String role = userDetails.getString("ROLE");
		String companyId = userDetails.getString("COMPANY_ID");

		if (!roleService.isSystemAdmin(role, companyId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
		InitialDirContext idc = null;
		Attributes attrs = null;
		Object cnameObj = null;
		try {
			idc = new InitialDirContext(env);
			if (idc == null) {
				throw new BadRequestException("CNAME_RECORD_MISSING");
			}
			attrs = idc.getAttributes(cname, new String[] { "CNAME" });
			Attribute attr = attrs.get("CNAME");
			if (attr == null) {
				throw new BadRequestException("CNAME_RECORD_MISSING");
			}
			cnameObj = attr.get();
		} catch (NamingException | NullPointerException e) {
			e.printStackTrace();
			throw new BadRequestException("CNAME_RECORD_MISSING");
		}

		if (!cnameObj.toString().equals(subdomain + ".ngdesk.com.")) {
			throw new BadRequestException("INVALID_SUBDOMAIN");
		}

		Session session = new Session("acme://letsencrypt.org");
		StringReader sr = null;
		KeyPair kp = null;
		URL accountLocationUrl = null;
		try {
			sr = new StringReader(global.getFile("acme4j-account-keypair-prd.pem"));
			kp = KeyPairUtils.readKeyPair(sr);
			accountLocationUrl = new URL("https://acme-v02.api.letsencrypt.org/acme/acct/52280645");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Login login = session.login(accountLocationUrl, kp);
		Account account = login.getAccount();

		Order order = null;

		MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");

		try {
			order = account.newOrder().domains(cname).create();

			for (Authorization auth : order.getAuthorizations()) {
				if (auth.getStatus() != Status.VALID) {

					log.trace("Enter Challenge Block: ");

					Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

					String fileName = challenge.getToken();
					String fileContent = challenge.getAuthorization();
					dnsRecordsCollection.updateOne(Filters.eq("COMPANY_SUBDOMAIN", subdomain),
							Updates.combine(Updates.set("CHALLENGE_TOKEN", fileName),
									Updates.set("CHALLENGE_AUTHORIZATION", fileContent), Updates.set("CNAME", cname)));

					log.trace("Triggering Challenge: ");
					challenge.trigger();

					int i = 0;
					while (auth.getStatus() != Status.VALID) {
						i++;
						if (i == 5) {
							throw new BadRequestException("AUTHORIZATION_FAILED");
						}
						auth.update();
						Thread.sleep(1000L);
					}
				}
			}

			log.trace("Challenge Succesful");

			KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

			CSRBuilder csrb = new CSRBuilder();
			csrb.addDomain(cname);
			csrb.setOrganization(subdomain);
			csrb.sign(domainKeyPair);
			byte[] csr = csrb.getEncoded();
			StringWriter outputWriter = new StringWriter();
			csrb.write(outputWriter);

			Security.addProvider(new BouncyCastleProvider());
			RSAPrivateKey priv = (RSAPrivateKey) domainKeyPair.getPrivate();
			PemObject pemObject = new PemObject("RSA PRIVATE KEY", priv.getEncoded());
			StringWriter str = new StringWriter();
			PemWriter pemWriter = new PemWriter(str);
			pemWriter.writeObject(pemObject);
			pemWriter.close();
			str.close();
			String pk = str.toString();

			String exampleCsr = outputWriter.toString();
			order.execute(csr);

			int i = 0;
			while (order.getStatus() != Status.VALID) {
				i++;
				if (i == 5) {
					throw new BadRequestException("ORDER_STATUS_INVALID");
				}
				order.update();
				Thread.sleep(1000L);
			}

			Certificate cert = order.getCertificate();

			X509Certificate x509Cert = cert.getCertificate();
			List<X509Certificate> chain = cert.getCertificateChain();

			StringWriter certWriter = new StringWriter();
			cert.writeCertificate(certWriter);
			String exampleCert = certWriter.toString();

			dnsRecordsCollection.updateOne(Filters.eq("COMPANY_SUBDOMAIN", subdomain),
					Updates.combine(Updates.set("PRIVATE_KEY", pk), Updates.set("CERTIFICATE", exampleCert)));

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (AcmeException e) {
			e.printStackTrace();
			throw new InternalErrorException("ACME_ERROR");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

	@DeleteMapping("/companies/dns/cname")
	public ResponseEntity<Object> deleteCname(HttpServletRequest request,
			@RequestParam(value = "authentication_token", required = false) String uuid) {
		try {
			log.trace("Entered cname delete call");
			if (request.getHeader("authentication_token") != null) {
				uuid = request.getHeader("authentication_token").toString();
			}
			JSONObject userDetails = auth.getUserDetails(uuid);
			String subdomain = userDetails.getString("COMPANY_SUBDOMAIN");
			String role = userDetails.getString("ROLE");
			String companyId = userDetails.getString("COMPANY_ID");

			if (!roleService.isSystemAdmin(role, companyId)) {
				throw new ForbiddenException("FORBIDDEN");
			}

			MongoCollection<Document> dnsCollection = mongoTemplate.getCollection("dns_records");
			Document document = dnsCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();
			if (!document.containsKey("CNAME")) {
				throw new BadRequestException("CNAME_RECORD_MISSING");
			}

			dnsCollection.updateOne(Filters.eq("COMPANY_SUBDOMAIN", subdomain),
					Updates.combine(Updates.unset("CNAME"), Updates.unset("CHALLENGE_TOKEN"),
							Updates.unset("CHALLENGE_AUTHORIZATION"), Updates.unset("PRIVATE_KEY"),
							Updates.unset("CERTIFICATE")));
			log.trace("Exit cname delete call");
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		throw new InternalErrorException("INTERNAL_ERROR");
	}

}

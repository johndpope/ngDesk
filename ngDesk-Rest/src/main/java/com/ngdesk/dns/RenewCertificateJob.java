package com.ngdesk.dns;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.bson.Document;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;

@Component
public class RenewCertificateJob {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	Global global;

	@Value("${email.host}")
	private String host;

	@Value("${env}")
	private String environment;

	private final Logger log = LoggerFactory.getLogger(RenewCertificateJob.class);

	@Scheduled(fixedRate = 86400000)
	public void executeJob() {
		try {

			log.trace("Entered RenewCertificateJob.renewCertificate()");
			MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
			MongoCollection<Document> companiesCollection = mongoTemplate.getCollection("companies");

			List<Document> documents = dnsRecordsCollection.find().into(new ArrayList<Document>());
			for (Document document : documents) { 
				if (document.containsKey("CNAME")) {
					String cname = document.get("CNAME").toString();
					String subdomain = document.getString("COMPANY_SUBDOMAIN");

					Document company = companiesCollection.find(Filters.eq("COMPANY_SUBDOMAIN", subdomain)).first();

					if (company == null) {
						continue;
					}

					String companyId = company.getObjectId("_id").toString();

					log.trace("Company ID: " + companyId);
					MongoCollection<Document> usersCollection = mongoTemplate.getCollection("Users_" + companyId);
					MongoCollection<Document> rolesCollection = mongoTemplate.getCollection("roles_" + companyId);

					Document systemAdmin = rolesCollection.find(Filters.eq("NAME", "SystemAdmin")).first();

					String roleId = systemAdmin.getObjectId("_id").toString();
					List<Document> users = usersCollection.find(Filters.and(Filters.eq("ROLE", roleId),
							Filters.eq("DELETED", false), Filters.eq("EFFECTIVE_TO", null))).into(new ArrayList<>());

					if (document.containsKey("CERTIFICATE")) {
						String cert = document.getString("CERTIFICATE");

						CertificateFactory cf = CertificateFactory.getInstance("X.509");
						X509Certificate x509certOld = (X509Certificate) cf
								.generateCertificate(new ByteArrayInputStream(cert.getBytes()));

						SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
						Date date = sdf.parse(x509certOld.getNotAfter().toString());
						Date now = new Date();
						long diffInMillies = date.getTime() - now.getTime();
						long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
						if (diff <= 7) {
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
							boolean authValid = true;
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
														Updates.set("CHALLENGE_AUTHORIZATION", fileContent),
														Updates.set("CNAME", cname)));

										log.trace("Triggering Challenge: ");
										challenge.trigger();

										int i = 0;
										outer: while (auth.getStatus() != Status.VALID) {
											i++;
											if (i == 5) {
												for (Document user : users) {
													try {
														String authorizationFailedBody = global
																.getFile("RenewCertificateErrorEmailBody.html");

														if (user.containsKey("FIRST_NAME")
																&& user.get("FIRST_NAME") != null) {
															authorizationFailedBody = authorizationFailedBody.replace(
																	"FIRST_NAME", user.getString("FIRST_NAME"));
														} else {
															authorizationFailedBody = authorizationFailedBody
																	.replace("FIRST_NAME", "");
														}

														if (user.containsKey("LAST_NAME")
																&& user.get("LAST_NAME") != null) {
															authorizationFailedBody = authorizationFailedBody
																	.replace("LAST_NAME", user.getString("LAST_NAME"));
														} else {
															authorizationFailedBody = authorizationFailedBody
																	.replace("LAST_NAME", "");
														}

														String emailAddress = user.getString("EMAIL_ADDRESS");
														SendEmail email = new SendEmail(emailAddress,
																"support@ngdesk.com",
																"Failed to authorize for certificate renewal",
																authorizationFailedBody, host);
														email.sendEmail();
													} catch (Exception e) {
														e.printStackTrace();
													}
													authValid = false;
												}
												break outer;
											}
											auth.update();
											Thread.sleep(1000L);
										}
									}
								}

								if (!authValid) {
									continue;
								}

								log.debug("Challenge Succesful");

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
								outer: while (order.getStatus() != Status.VALID) {
									i++;
									if (i == 5) {
										for (Document user : users) {
											try {
												String orderStatusInvalid = global
														.getFile("RenewCertificateErrorEmailBody.html");

												if (user.containsKey("FIRST_NAME") && user.get("FIRST_NAME") != null) {
													orderStatusInvalid = orderStatusInvalid.replace("FIRST_NAME",
															user.getString("FIRST_NAME"));
												} else {
													orderStatusInvalid = orderStatusInvalid.replace("FIRST_NAME", "");
												}

												if (user.containsKey("LAST_NAME") && user.get("LAST_NAME") != null) {
													orderStatusInvalid = orderStatusInvalid.replace("LAST_NAME",
															user.getString("LAST_NAME"));
												} else {
													orderStatusInvalid = orderStatusInvalid.replace("LAST_NAME", "");
												}

												String emailAddress = user.getString("EMAIL_ADDRESS");
												SendEmail email = new SendEmail(emailAddress, "support@ngdesk.com",
														"Failed to order a new certificate", orderStatusInvalid, host);
												email.sendEmail();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
										break outer;
									}
									order.update();
									Thread.sleep(1000L);
								}

								Certificate newCert = order.getCertificate();

								X509Certificate x509Cert = newCert.getCertificate();
								List<X509Certificate> chain = newCert.getCertificateChain();

								StringWriter certWriter = new StringWriter();
								newCert.writeCertificate(certWriter);
								String exampleCert = certWriter.toString();

								dnsRecordsCollection.updateOne(Filters.eq("COMPANY_SUBDOMAIN", subdomain),
										Updates.combine(Updates.set("PRIVATE_KEY", pk),
												Updates.set("CERTIFICATE", exampleCert)));

							} catch (AcmeException e) {
								e.printStackTrace();
								for (Document user : users) {
									try {
										String acmeErrorBody = global.getFile("RenewCertificateErrorEmailBody.html");

										if (user.containsKey("FIRST_NAME") && user.get("FIRST_NAME") != null) {
											acmeErrorBody = acmeErrorBody.replace("FIRST_NAME",
													user.getString("FIRST_NAME"));
										} else {
											acmeErrorBody = acmeErrorBody.replace("FIRST_NAME", "");
										}

										if (user.containsKey("LAST_NAME") && user.get("LAST_NAME") != null) {
											acmeErrorBody = acmeErrorBody.replace("LAST_NAME",
													user.getString("LAST_NAME"));
										} else {
											acmeErrorBody = acmeErrorBody.replace("LAST_NAME", "");
										}

										String emailAddress = user.getString("EMAIL_ADDRESS");
										SendEmail email = new SendEmail(emailAddress, "support@ngdesk.com",
												"Failed to renew certificate", acmeErrorBody, host);
										email.sendEmail();
									} catch (Exception e1) {
										e1.printStackTrace();
									}
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
					}
				}
			}
			log.trace("Exit RenewCertificateJob.renewCertificate()");

		} catch (Exception e) {
			e.printStackTrace();

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			String sStackTrace = sw.toString();

			if (environment.equals("prd")) {
				SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com", "support@ngdesk.com",
						"Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToSpencer.sendEmail();

				SendEmail sendEmailToShashank = new SendEmail("shashank.shankaranand@allbluesolutions.com",
						"support@ngdesk.com", "Internal Error: Stack Trace", sStackTrace, host);
				sendEmailToShashank.sendEmail();
			}

		}
	}

}

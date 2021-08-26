package com.ngdesk.auth.forgot.password;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.DNSRecordRepository;

@Component
public class VerifyRecaptcha {

	@Autowired
	DNSRecordRepository dnsRecordrepository;

	@Value("${google.recaptcha.url}")
	private String url;
	
	@Value("${google.recaptcha.secret}")
	private String secret;
	
	private final static String USER_AGENT = "Mozilla/5.0";

	public Boolean verify(String gRecaptchaResponse, String subDomain) {

		try {
			if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
				return false;
			}

			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			// add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String postParams = "secret=" + secret + "&response=" + gRecaptchaResponse;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postParams);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			ObjectMapper mapper = new ObjectMapper();
			Map map = mapper.readValue(response.toString(), Map.class);
			boolean result = (boolean) map.get("success");

			if (result) {
				String hostname = (String) map.get("hostname");
				if (!hostname.equals("localhost") && !hostname.endsWith(".ngdesk.com")
						&& !hostname.equals("10.2.15.85")) {

					Optional<Map<String, Object>> optionalDNSRecord = dnsRecordrepository
							.findDNSRecordBySubDomain(subDomain, "dns_records");

					String cname = (String) optionalDNSRecord.get().get("CNAME");
					if (!cname.equals(hostname)) {
						result = false;
					}
				}
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

package com.ngdesk;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

@Component
public class VerifyRecaptcha {

	@Autowired
	MongoTemplate mongoTemplate;

	private static Logger logger = LogManager.getLogger(VerifyRecaptcha.class);

	@Value("${google.recaptcha.url}")
	private String url;
	
	@Value("${google.recaptcha.secret}")
	private String secret;
	
	private final static String USER_AGENT = "Mozilla/5.0";

	public Boolean verify(String gRecaptchaResponse) {
		logger.trace("Enter VerifyRecaptcha.verify() gRecaptchaResponse: " + gRecaptchaResponse);

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

			JSONObject jsonObject = new JSONObject(response.toString());

			Boolean result = jsonObject.getBoolean("success");

			logger.debug("RESPONSE_JSON: " + jsonObject);
			if (result) {
				String hostname = jsonObject.getString("hostname");
				if (!hostname.equals("localhost") && !hostname.endsWith(".ngdesk.com")
						&& !hostname.equals("10.2.15.85")) {

					MongoCollection<Document> dnsRecordsCollection = mongoTemplate.getCollection("dns_records");
					Document document = dnsRecordsCollection.find(Filters.eq("CNAME", hostname)).first();

					if (document == null) {
						result = false;
					}

				}
			}

			logger.trace("Exit VerifyRecaptcha.verify() gRecaptchaResponse: " + gRecaptchaResponse);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

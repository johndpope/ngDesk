package com.ngdesk.resources;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Requests {

	private final static Logger log = LoggerFactory.getLogger(Requests.class);

	// FUNCTION TO PERFORM GET CALL
	public static String get(String url, Map<String, String> headerMap) {
		StringBuffer result = new StringBuffer();
		HttpResponse response;

		try {
			log.trace("Enter Requests.get() url: " + url);
			// CREATE HTTP CLIENT
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);

			// SET HEADERS
			if (headerMap != null) {
				for (Map.Entry<String, String> header : headerMap.entrySet()) {
					request.setHeader(header.getKey(), header.getValue());
				}
			}

			response = client.execute(request);

			// WRITE RESULT
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		log.trace("Exit Requests.get() url: " + url);
		return result.toString();
	}

	// FUNCTION TO PERFORM POST, PUT, OR DELETE CALLS
	public static String request(String url, String body, String type, Map<String, String> headerMap) {
		int responsecode = -1;
		StringBuffer response = new StringBuffer();
		try {
			log.trace("Enter Requests.request() url: " + url + ", type: " + type);
			// OPEN CONNECTION
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// ADD REQUEST HEADER
			con.setRequestMethod(type.toUpperCase());
			con.setRequestProperty("Content-Type", "application/json");

			// SET PROPERTIES OF REQUEST
			if (headerMap != null) {
				for (Map.Entry<String, String> header : headerMap.entrySet()) {
					con.setRequestProperty(header.getKey(), header.getValue());
				}
			}

			// SEND POST REQUEST
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());

			wr.writeBytes(body);
			wr.flush();
			wr.close();

			responsecode = con.getResponseCode();

			if (responsecode < 200 || responsecode >= 300) {
				return null;
			}

			// WRITE RESULTS
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

		} catch (Exception e) {
			e.printStackTrace();

		}
		log.trace("Exit Requests.request() url: " + url + ", type: " + type);
		return response.toString();
	}
}

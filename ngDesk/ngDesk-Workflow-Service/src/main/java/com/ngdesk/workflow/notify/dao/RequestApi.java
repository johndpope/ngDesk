package com.ngdesk.workflow.notify.dao;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class RequestApi {

	public static String request(String url, String body, String type, Map<String, String> headerMap) {
		int responsecode = -1;
		StringBuffer response = new StringBuffer();
		HttpURLConnection con = null;
		try {

			// OPEN CONNECTION
			URL obj = new URL(url);
			con = (HttpURLConnection) obj.openConnection();

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
			if (body != null) {
				DataOutputStream wr = new DataOutputStream(con.getOutputStream());
				wr.write(body.getBytes("UTF-8"));
				wr.flush();
				wr.close();
			}
			responsecode = con.getResponseCode();

			if (responsecode < 200 || responsecode >= 300) {

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
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
			return null;
		} finally {
			con.disconnect();
		}
		return response.toString();
	}

}

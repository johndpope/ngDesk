package com.ngdesk.modules.forms;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class HttpRequestNode {

	public JSONObject request(String url, String body, String type, Map<String, String> headerMap) {

		int responsecode = -1;
		StringBuffer response = new StringBuffer();
		try {

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

			wr.write(body.getBytes("UTF-8"));
			wr.flush();
			wr.close();

			responsecode = con.getResponseCode();

			if (responsecode < 200 || responsecode >= 300) {

				String responseError = IOUtils.toString(con.getErrorStream(), "UTF-8");

				JSONObject result = new JSONObject();
				result.put("ERROR", responseError);
				result.put("RESPONSE_CODE", responsecode);
				return result;
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

		JSONObject result = new JSONObject();
		result.put("RESPONSE", response.toString());
		result.put("RESPONSE_CODE", responsecode);
		return result;
	}

}

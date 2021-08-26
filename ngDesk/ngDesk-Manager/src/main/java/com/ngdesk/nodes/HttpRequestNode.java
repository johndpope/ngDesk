package com.ngdesk.nodes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.Global;

@Component
public class HttpRequestNode extends Node {

	private final Logger log = LoggerFactory.getLogger(HttpRequestNode.class);

	@Autowired
	Global global;

	// FUNCTION TO DIRECT HTTP REQUEST AND RETURN RESULTS MAPPING
	public Map<String, Object> executeNode(Document node, Map<String, Object> inputMessage) {
		log.trace("Enter HttpRequestNode.executeNode()");

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			// GET NODE VALUES
			Document values = (Document) node.get("VALUES");
			List<Document> headers = (List<Document>) values.get("HTTP_PARAMS");
			Map<String, String> headerMap = new HashMap<String, String>();

			// BUILD HEADERS
			for (Document header : headers) {

				String key = header.getString("KEY");
				String value = header.getString("VALUE");

				headerMap.put(global.getValue(key, inputMessage), global.getValue(value, inputMessage));
			}

			String result = null;

			// RUN FUNCTION TO SUBMIT HTTP REQUEST
			if (values.getString("REQUEST_TYPE").equalsIgnoreCase("GET")) {

				String url = global.getValue(values.getString("URL"), inputMessage);
				result = get(url, headerMap);
			} else {

				String url = global.getValue(values.getString("URL"), inputMessage);
				String body = global.getValue(values.getString("BODY"), inputMessage);
				
				JSONObject response = request(url, body, values.getString("REQUEST_TYPE"), headerMap);
				
				if (response.has("RESPONSE")) {
					result = response.getString("RESPONSE");
				}
			}

			String nextNode = null;

			// REQUEST SUCCESS
			if (result != null) {

				// CREATE MAP OF RESULT JSON DATA
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Object> map = new HashMap<String, Object>();

				// CHECK LENGTH OF RESULT TO AVOID MAPPING ERROR
				if (result.length() > 0) {
					map = objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {
					});
					inputMessage.put(node.getString("NAME"), map);
				}

				// GET NEXT NODE
				nextNode = getNodeByLabel(node, "OUT_SUCCESS");

				// REQUEST FAILED
			} else {

				inputMessage.put(null, null);

				// GET NEXT NODE
				nextNode = getNodeByLabel(node, "OUT_FAIL");
			}

			// CREATE MAP FOR PARENTNODE FUNCTIONS
			resultMap.put("INPUT_MESSAGE", inputMessage);
			resultMap.put("NODE_ID", nextNode);

		} catch (Exception e) {
			e.printStackTrace();
		}

		log.trace("Exit HttpRequestNode.executeNode()");
		return resultMap;
	}

	// FUNCTION TO RETURN NEXT NODE CONNECTION TO
	public String getNodeByLabel(Document node, String label) {
		log.trace("Enter HttpRequestNode.getNodeByLabel() label: " + label);
		String result = null;

		// GET NODE CONNECTIONS
		List<Document> connections = (List<Document>) node.get("CONNECTIONS_TO");

		for (Document connection : connections) {
			if (connection.getString("FROM").equalsIgnoreCase(label)) {
				// GET NODE TO BASED ON LABEL, SUCCESS OR FAIL
				result = connection.getString("TO_NODE");
				break;
			}
		}

		log.trace("Exit HttpRequestNode.getNodeByLabel() label: " + label);
		return result;
	}

	// FUNCTION TO PERFORM GET CALL
	public String get(String url, Map<String, String> headerMap) {
		log.trace("Enter HttpRequestNode.get() url: " + url);
		StringBuffer result = new StringBuffer();
		HttpResponse response;

		try {

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
		log.trace("Exit HttpRequestNode.get() url: " + url);
		return result.toString();
	}

	// FUNCTION TO PERFORM POST, PUT, OR DELETE CALLS
	public JSONObject request(String url, String body, String type, Map<String, String> headerMap) {
		log.trace("Enter HttpRequestNode.request()");
		int responsecode = -1;
		StringBuffer response = new StringBuffer();
		try {

			// OPEN CONNECTION
			URL obj = new URL(url);
			log.trace(obj.getPath());
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

			log.trace("Response Code: " + responsecode);

			if (responsecode < 200 || responsecode >= 300) {
				
				String responseError = IOUtils.toString(con.getErrorStream(), "UTF-8");
				
				JSONObject result = new JSONObject();
				result.put("ERROR", responseError);
				result.put("RESPONSE_CODE", responsecode);
				
				return result;
			}

			log.trace("Response Message: " + con.getResponseMessage());

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

		log.trace("Exit HttpRequestNode.request()");
		JSONObject result = new JSONObject();
		result.put("RESPONSE", response.toString());
		result.put("RESPONSE_CODE", responsecode);
		return result;
	}

}

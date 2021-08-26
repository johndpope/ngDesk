package com.ngdesk.commons;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.ngdesk.commons.exceptions.InternalErrorException;

@Component
public class Global {

	@Autowired
	ResourceLoader resourceLoader;

	public static Map<String, Map<String, String>> translations;
	public static HashMap<String, String> currencies;
	public static HashMap<String, String> currenciesReverseLookUp;
	public static List<String> timezones;

	public static String[] pathsWhitelisted = { "/actuator/(.*)", "/v3/api-docs/(.*)", "/v3/api-docs",
			"/swagger-ui.html", "/swagger-ui/(.*)", "/noauth/query", "/reports/download", "/reports/schedules/download",
			"/channels/chat_channel" };

	public static String errorMsg(String language, String msgKey) {
		try {
			if (!translations.containsKey(language)) {
				language = "en";
			}
			if (translations.get(language).containsKey(msgKey)) {
				return translations.get(language).get(msgKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return msgKey;
	}

	public String getFile(String fileName) {
		try {
			Resource resource = resourceLoader.getResource("classpath:" + fileName);
			InputStream inputStream = resource.getInputStream();

			byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
			String data = new String(bdata, StandardCharsets.UTF_8);
			return data;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public InputStream getInputStream(String filename) {
		try {
			Resource resource = resourceLoader.getResource("classpath:" + filename);
			InputStream inputStream = resource.getInputStream();
			return inputStream;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getvariableErrorMessage(String language, String msgKey, String[] vars) {
		try {
			String errorMessage = errorMsg(language, msgKey);
			if (vars != null) {
				for (int i = 0; i < vars.length; i++) {
					String translatedKey = errorMsg(language, vars[i]);
					errorMessage = errorMessage.replace("${" + (i + 1) + "}", translatedKey);
				}
			}
			return errorMessage;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return msgKey;
	}

	public String getBaseManagerUrl(String subdomain) {
		return "https://" + subdomain + ".ngdesk.com/ngdesk-manager/ngdesk/";
	}

	public String getBaseRestUrl(String subdomain) {
		return "https://" + subdomain + ".ngdesk.com/ngdesk-rest/ngdesk/";
	}

	public String passwordHash(String pwd) {
		String hashedPassword = "";

		if (pwd == "") {
			return "";
		}

		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(pwd.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			hashedPassword = bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return hashedPassword;
	}

	public String getIsoCode(String variable) {

		if (currencies.containsKey(variable)) {
			return variable;
		} else if (currenciesReverseLookUp.containsKey(variable)) {

			return currenciesReverseLookUp.get(variable);
		}

		return null;
	}

}

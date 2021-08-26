package com.ngdesk;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class Startup implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private Global global;

	@Autowired
	private Environment env;

	private final Logger log = LoggerFactory.getLogger(Startup.class);

	@Override
	public void onApplicationEvent(ApplicationReadyEvent arg0) {

		log.trace("Enter Startup");

		try {
			
			// adding translations
			global.errors.put("ar", new JSONObject(global.getFile("ar.json")));
			global.errors.put("de", new JSONObject(global.getFile("de.json")));
			global.errors.put("el", new JSONObject(global.getFile("el.json")));			
			global.errors.put("en", new JSONObject(global.getFile("en.json")));
			global.errors.put("es", new JSONObject(global.getFile("es.json")));
			global.errors.put("fr", new JSONObject(global.getFile("fr.json")));
			global.errors.put("hi", new JSONObject(global.getFile("hi.json")));
			global.errors.put("it", new JSONObject(global.getFile("it.json")));
			global.errors.put("ms", new JSONObject(global.getFile("ms.json")));
			global.errors.put("no", new JSONObject(global.getFile("no.json")));
			global.errors.put("pt", new JSONObject(global.getFile("pt.json")));
			global.errors.put("ru", new JSONObject(global.getFile("ru.json")));
			global.errors.put("zh", new JSONObject(global.getFile("zh.json")));
			
			String[] supportedLanguages = { "ar", "de", "el", "en", "es", "fr", "hi", "it", "ms", "no", "pt", "ru", "zh" };
			global.languages = (List) Arrays.asList(supportedLanguages);
			
			for (String language:global.languages) {
				String translation = global.getFile(language + ".json");
				global.translation.put(language, new JSONObject(translation));
			}
			
			global.postHeaders = new HttpHeaders();
			global.postHeaders.setContentType(MediaType.APPLICATION_JSON);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.trace("Exit Startup");
	}

}

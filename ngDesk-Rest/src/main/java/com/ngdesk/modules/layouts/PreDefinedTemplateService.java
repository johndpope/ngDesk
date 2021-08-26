package com.ngdesk.modules.layouts;

import java.io.IOException;

import org.bson.Document;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.ngdesk.exceptions.BadRequestException;

@Component
@RestController
public class PreDefinedTemplateService {

	@Autowired
	private MongoTemplate mongoTemplate;

	private Logger log = LoggerFactory.getLogger(PreDefinedTemplate.class);

	@GetMapping("/layouts/{module_name}/{layout_type}")
	public PreDefinedTemplate getGlobalTemplate(@PathVariable("module_name") String moduleName,
			@RequestParam(value = "authentication_token", required = false) String uuid,
			@PathVariable("layout_type") String layoutType) {
		try {
			log.trace("Enter PreDefinedTemplateService.getPredefinedTemplate() : ModuleName " + moduleName);

			MongoCollection<Document> predefinedTemplatesCollection = mongoTemplate
					.getCollection("predefined_templates");
			if (layoutType.equalsIgnoreCase("DETAIL_LAYOUTS")) {
				layoutType = "EDIT_LAYOUTS";
			}
			Document template = predefinedTemplatesCollection
					.find(Filters.and(Filters.eq("MODULE_NAME", moduleName), Filters.eq("LAYOUT_TYPE", layoutType)))
					.first();
			if (template == null) {
				throw new BadRequestException("PREDEFINED_TEMPLATE_NOT_FOUND");
			}
			template.remove("_id");

			PreDefinedTemplate preDefinedTemplate = new ObjectMapper().readValue(template.toJson(),
					PreDefinedTemplate.class);

			log.trace("Exit PreDefinedTemplateService.getPredefinedTemplate() : ModuleName " + moduleName);
			return preDefinedTemplate;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
}

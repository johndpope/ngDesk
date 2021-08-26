package com.ngdesk.module.template;

import java.util.Optional;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.HtmlTemplateRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RefreshScope
@RestController
public class HtmlTemplateAPI {

	@Autowired
	private HtmlTemplateRepository htmlTemplateRepository;

	@Autowired
	private AuthManager authManager;

	@GetMapping("modules/{module_id}/templates")
	@Operation(summary = "Get all", description = "Gets all the templates with pagination and search")
	@PageableAsQueryParam
	public Page<HtmlTemplate> getTemplates(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId) {

		return htmlTemplateRepository.findAllTemplates(pageable, moduleId,
				"html_templates_" + authManager.getUserDetails().getCompanyId());
	}

	@GetMapping("modules/{module_id}/template/{id}")
	@Operation(summary = "Get by ID", description = "Gets the html template based on ID")
	public HtmlTemplate getTemplateById(
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId,
			@Parameter(description = "Template ID", required = true) @PathVariable("id") String id) {
		Optional<HtmlTemplate> optional = htmlTemplateRepository.findById(id,
				"html_templates_" + authManager.getUserDetails().getCompanyId());
		if (optional.isEmpty()) {
			String vars[] = { "HTML_TEMPLATE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		return optional.get();
	}

	@PostMapping("modules/{module_id}/template")
	@Operation(summary = "Post template", description = "Post a single template")
	public HtmlTemplate postTemplate(@Valid @RequestBody HtmlTemplate template,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId) {
		return htmlTemplateRepository.save(template, "html_templates_" + authManager.getUserDetails().getCompanyId());
	}

	@PutMapping("modules/{module_id}/template")
	@Operation(summary = "Put Template", description = "Update a Template")
	public HtmlTemplate putTemplate(@Valid @RequestBody HtmlTemplate template,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId) {

		Optional<HtmlTemplate> optional = htmlTemplateRepository.findById(template.getId(),
				"html_templates_" + authManager.getUserDetails().getCompanyId());

		if (optional.isEmpty()) {
			String vars[] = { "HTML_TEMPLATE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		return htmlTemplateRepository.save(template, "html_templates_" + authManager.getUserDetails().getCompanyId());

	}

	@DeleteMapping("modules/{module_id}/template/{id}")
	@Operation(summary = "Delete template", description = "Delete a template by ID")
	public void deleteTemplate(@Parameter(description = "Template ID", required = true) @PathVariable("id") String id,
			@Parameter(description = "Module ID", required = true) @PathVariable("module_id") String moduleId) {
		Optional<HtmlTemplate> optional = htmlTemplateRepository.findById(id,
				"html_templates_" + authManager.getUserDetails().getCompanyId());

		if (optional.isEmpty()) {
			String vars[] = { "HTML_TEMPLATE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		htmlTemplateRepository.deleteById(id, "html_templates_" + authManager.getUserDetails().getCompanyId());
	}

}

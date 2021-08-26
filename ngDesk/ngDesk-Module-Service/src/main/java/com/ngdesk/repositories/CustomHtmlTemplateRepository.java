package com.ngdesk.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.module.template.HtmlTemplate;

public interface CustomHtmlTemplateRepository {
	
	public Page<HtmlTemplate> findAllTemplates(Pageable pageable, String moduleId, String collectionName);
}

package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.module.template.HtmlTemplate;

@Repository
public interface HtmlTemplateRepository extends CustomHtmlTemplateRepository,  CustomNgdeskRepository<HtmlTemplate, String>{

	
}

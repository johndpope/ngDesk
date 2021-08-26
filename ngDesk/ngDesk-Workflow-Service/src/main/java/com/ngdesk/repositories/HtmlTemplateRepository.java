package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.data.dao.HtmlTemplate;


@Repository
public interface HtmlTemplateRepository extends CustomHtmlTemplateRepository,  CustomNgdeskRepository<HtmlTemplate, String>{

	
}

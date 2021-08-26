package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.data.dao.HtmlTemplate;


public interface CustomHtmlTemplateRepository {
	
	public List<HtmlTemplate> findTemplatesByModuleId(String moduleId, String collectionName) ;
		
}

package com.ngdesk.data.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.module.entry.ModulesRepository;

@Component
public class SwidtagService {
	
	@Autowired
	ModulesRepository moduleRepository;
	
	public void postSwidag(Swidtag swidtag) {
		
		// Check if a file exists with this name
		
		
		
	}
	
}

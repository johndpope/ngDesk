package com.ngdesk.sam.swidtag;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.SwidtagRepository;

@Component
public class SwidtagService {
	@Autowired
	AuthManager authManager;

	@Autowired
	SwidtagRepository swidtagRepository;

	@Autowired
	ModuleRepository moduleRepository;

	public Optional<Map<String, Object>> validateAsset(String assetId, String collectionName) {
		return moduleRepository.findById(assetId, collectionName);
	}

}
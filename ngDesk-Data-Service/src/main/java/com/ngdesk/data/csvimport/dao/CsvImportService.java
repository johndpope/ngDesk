package com.ngdesk.data.csvimport.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.InternalErrorException;
import com.ngdesk.data.elastic.Wrapper;
import com.ngdesk.data.modules.dao.Module;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;

@Service
public class CsvImportService {

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesRepository modulesRepository;
	
	@Autowired
	Wrapper wrapper;

	public boolean accountExists(String accountName, String companyId) {

		String collectionName = "Accounts_" + companyId;
		Optional<Map<String, Object>> optionalAccountEntry = moduleEntryRepository.findEntryByFieldName("ACCOUNT_NAME",
				accountName, collectionName);

		if (optionalAccountEntry.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public Map<String, Object> createAccount(String accountName, String companyId, String globalTeamId) {
		Map<String, Object> accountEntry = new HashMap<String, Object>();
		try {

			Optional<Module> optionalAccountModule = modulesRepository.findIdbyModuleName("Accounts",
					"modules_" + companyId);
			Module accountModule = optionalAccountModule.get();

			Map<String, Object> account = new HashMap<String, Object>();
			account.put("ACCOUNT_NAME", accountName);
			account.put("DATE_CREATED", new Date());
			account.put("TEAMS", Arrays.asList(globalTeamId));
			account.put("DELETED", false);

			accountEntry = wrapper.postData(companyId, accountModule, account);

		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("INTERNAL_ERROR");
		}
		return accountEntry;
	}
}

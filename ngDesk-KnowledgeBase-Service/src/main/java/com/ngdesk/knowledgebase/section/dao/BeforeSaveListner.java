package com.ngdesk.knowledgebase.section.dao;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.knowledgebase.categories.dao.Category;
import com.ngdesk.knowledgebase.role.dao.Role;
import com.ngdesk.repositories.CategoryRepository;
import com.ngdesk.repositories.RolesRepository;
import com.ngdesk.repositories.section.SectionRepository;

@Component
public class BeforeSaveListner extends AbstractMongoEventListener<Section> {

	@Autowired
	AuthManager authManager;

	@Autowired
	SectionRepository sectionRepository;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	CategoryRepository categoryRepository;

	String[] languageCodes = { "ar", "de", "el", "en", "es", "fr", "hi", "it", "ms", "pt", "ru", "zh", "no" };

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Section> event) {

		Section section = event.getSource();
		checkValidTeam(section.getVisibleTo(), authManager.getUserDetails().getCompanyId());
		checkValidTeam(section.getManagedBy(), authManager.getUserDetails().getCompanyId());
		checkValidUser();
		checkValidLanguageSource(section.getLanguage());
		checkValidCategory(section.getCategory(), authManager.getUserDetails().getCompanyId());

	}

	public void checkValidTeam(List<String> visibleTo, String companyId) {
		for (String team : visibleTo) {
			Optional<Map<String, Object>> optionalVisibleTo = sectionRepository.findByVisibleTo(team,
					"Teams_" + companyId);

			if (optionalVisibleTo.isEmpty()) {
				String[] vars = { team };
				throw new NotFoundException("VISIBLE_TO_INVALID", vars);
			} else if (optionalVisibleTo.toString().contains("Ghost Team")) {
				throw new BadRequestException("INVALID_VISIBLE_TO", null);
			}

		}

	}

	private void checkValidUser() {
		String userId = authManager.getUserDetails().getRole();
		Optional<Role> optionalSystemAdminId = rolesRepository.findByRoleName("SystemAdmin",
				"roles_" + authManager.getUserDetails().getCompanyId());

		if (optionalSystemAdminId.isPresent() && !optionalSystemAdminId.get().getId().equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}

	}

	private void checkValidLanguageSource(String language) {
		List<String> languageCode = Arrays.asList(languageCodes);
		if (!languageCode.contains(language)) {
			String[] vars = { "SECTION" };
			throw new BadRequestException("SOURCE_LANGUAGE_DOES_NOT_EXIST", vars);
		}
	}

	private void checkValidCategory(String category, String companyId) {
		Optional<Category> optionalCategory = categoryRepository.findById(category, "categories_" + companyId);

		if (optionalCategory.isEmpty()) {
			throw new ForbiddenException("INVALID_CATEGORY");
		}
	}

}

package com.ngdesk.knowledgebase.categories.dao;

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
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.CategoryRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.RolesRepository;

@Component("SaveListener")
public class BeforeSaveListener extends AbstractMongoEventListener<Category> {

	@Autowired
	AuthManager authManager;

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Category> event) {
		Category category = event.getSource();
		validateSystemAdmin();
		validateSourceLanguage(category);
		validateVisibleTo(category);
		validateDuplicateCategory(category);
	}

	public void validateDuplicateCategory(Category category) {
		Optional<Category> optionalCategory = categoryRepository.validateDuplicateCategory(category.getName(),
				"categories_" + authManager.getUserDetails().getCompanyId());
		if (!optionalCategory.isEmpty()) {
			String[] vars = { "Category", "Name" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
		}
	}

	public void validateSystemAdmin() {
		String systemAdminId = rolesRepository
				.findByRoleName("SystemAdmin", "roles_" + authManager.getUserDetails().getCompanyId()).get().getId();
		String userId = authManager.getUserDetails().getRole();

		if (!systemAdminId.equals(userId)) {
			throw new ForbiddenException("FORBIDDEN");
		}
	}

	public void validateSourceLanguage(Category category) {
		List<String> supportedLanguages = Arrays.asList("ar", "de", "el", "en", "es", "fr", "hi", "it", "ms", "pt",
				"ru", "zh", "no");

		if (!supportedLanguages.contains(category.getSourceLanguage())) {
			throw new BadRequestException("UNSUPPORTED_LANGUAGE", null);
		}
	}

	public void validateVisibleTo(Category category) {
		if (category.getVisibleTo().isEmpty()) {
			throw new BadRequestException("VISIBLE_TO_INVALID", null);
		}

		for (String teamId : category.getVisibleTo()) {
			Optional<Map<String, Object>> optionalTeamId = moduleEntryRepository.findTeamById(teamId);
			if (optionalTeamId.isEmpty()) {
				throw new BadRequestException("INVALID_VISIBLE_TO", null);
			}
		}

	}
}

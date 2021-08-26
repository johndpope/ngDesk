package com.ngdesk.sam.rules.dao;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ModuleRepository;

@Valid
@Component
public class RuleService {
	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleRepository moduleRepository;

	public void validateRuleCondition(SamFileRule samFileRule) {
		switch (samFileRule.getRuleCondition()) {
		case "fileName, file Path, Hash":
		case "fileName, file Path":
		case "Hash":
			if ((samFileRule.getFileName() == null)
					|| (samFileRule.getFileName() != null && samFileRule.getFileName().isEmpty())) {
				throw new BadRequestException("FILE_NAME_MISSING", null);
			}
			if ((samFileRule.getFilePath() == null)
					|| (samFileRule.getFilePath() != null && samFileRule.getFilePath().isEmpty())) {
				throw new BadRequestException("FILE_PATH_MISSING", null);
			}
			break;
		case "fileName":
		case "fileName, Hash":
			if ((samFileRule.getFileName() == null)
					|| (samFileRule.getFileName() != null && samFileRule.getFileName().isEmpty())) {
				throw new BadRequestException("FILE_NAME_MISSING", null);
			}
			break;
		default:
			break;
		}
	}

}

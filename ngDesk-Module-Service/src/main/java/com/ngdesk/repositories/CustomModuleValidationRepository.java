package com.ngdesk.repositories;

import com.ngdesk.module.validations.dao.ModuleValidation;

public interface CustomModuleValidationRepository {

	public void saveModuleValidation(ModuleValidation moduleValidation, String moduleId, String collectionName);

	public void removeModuleValidation(String validationId, String moduleId, String collectionName);

	public void updateModuleValidation(ModuleValidation moduleValidation, String moduleId, String collectionName);

}

package com.ngdesk.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ngdesk.module.field.dao.ModuleField;

public interface CustomFieldRepository {

	public Optional<List<ModuleField>> findFields(String moduleId, String tier, String collectionName);

	public Optional<List<ModuleField>> findRelationshipFields(String moduleId, String tier, List<String> modules,
			String collectionName);

	public void saveField(String collectionName, ModuleField moduleField, String moduleId);

	public void removeField(String moduleId, String fieldId, String collectionName);
	
	public void updateField(ModuleField moduleField, String moduleId, String collectionName);

	public Page<ModuleField> findAllFieldsWithPagenation(Pageable pageable, String moduleId, String companyId);

}

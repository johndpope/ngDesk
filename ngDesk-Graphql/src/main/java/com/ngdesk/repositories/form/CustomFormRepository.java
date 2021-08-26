package com.ngdesk.repositories.form;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.form.dao.Form;

public interface CustomFormRepository {

	public Optional<Form> findFormById(String formId, String moduleId, String companyId, String collectionName);
	
	public Optional<Form> findFormByIdAndTeams(String formId, String moduleId, String companyId, String collectionName, List<String> teamIds);

	public List<Form> findAllForms(String companyId, String moduleId, Pageable pageable, String collectionName);
	
	public List<Form> findAllFormsWithTeams(String companyId, String moduleId, Pageable pageable, String collectionName, List<String> teamIds);

	public int formsCount(String companyId, String moduleId, String collectionName);

}

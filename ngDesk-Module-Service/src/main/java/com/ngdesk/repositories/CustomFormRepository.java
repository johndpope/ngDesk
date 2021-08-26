package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.module.form.dao.Form;
import com.ngdesk.workflow.dao.Workflow;
import com.ngdesk.module.dao.Module;

public interface CustomFormRepository {

	public void removeFormById(String formId, String companyId, String moduleId, String collectionName);

	public Optional<Form> findFormById(String formId, String companyId, String moduleId, String collectionName);

	Optional<Form> findFormByName(String companyId, String moduleId, String name, String formId, String collectionName);

	public Optional<Form> findFormWithDuplicateName(String moduleId, String name, String companyId, String formId,
			String collectionName);

	public Optional<Workflow> findWorkflowByModuleIdAndWorkflowId(String workflowId, String moduleId, String companyId);

}

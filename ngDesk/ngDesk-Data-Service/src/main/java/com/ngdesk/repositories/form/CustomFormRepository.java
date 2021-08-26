package com.ngdesk.repositories.form;

import java.util.Optional;

import com.ngdesk.data.form.dao.Form;

public interface CustomFormRepository {

	public Optional<Form> findFormByFormId(String formId, String moduleId, String companyId, String collectionName);

}

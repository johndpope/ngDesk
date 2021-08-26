package com.ngdesk.repositories.form;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.form.dao.Form;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface FormRepository extends CustomFormRepository, CustomNgdeskRepository<Form, String> {

}

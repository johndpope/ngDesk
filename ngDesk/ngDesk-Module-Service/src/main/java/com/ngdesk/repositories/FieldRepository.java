package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.module.field.dao.ModuleField;

@Repository
public interface FieldRepository extends CustomFieldRepository, CustomNgdeskRepository<ModuleField, String> {

}

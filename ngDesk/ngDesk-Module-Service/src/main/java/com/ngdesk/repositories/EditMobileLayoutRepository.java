package com.ngdesk.repositories;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;

public interface EditMobileLayoutRepository
		extends CustomNgdeskRepository<CreateEditMobileLayout, String>, CustomEditMobileLayoutRepository {

}

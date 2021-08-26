package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.module.userplugins.dao.UserPlugin;

@Repository
public interface UserPluginRepository
		extends CustomNgdeskRepository<UserPlugin, String>, CustomUserPluginRepository {

}

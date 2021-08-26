package com.ngdesk.repositories.userplugin;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.userplugin.dao.UserPlugin;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface UserPluginRepository
		extends CustomUserPluginRepository, CustomNgdeskRepository<UserPlugin, String> {

}

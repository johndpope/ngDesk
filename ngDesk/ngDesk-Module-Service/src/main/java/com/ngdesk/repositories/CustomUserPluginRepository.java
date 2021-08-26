package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.module.userplugins.dao.UserPlugin;

public interface CustomUserPluginRepository {

	public Optional<UserPlugin> findPluginByName(String name, String collectionName);

	public Optional<UserPlugin> findOtherPluginsWithDuplicateName(String name, String pluginId, String collectionName);

}

package com.ngdesk.repositories.userplugin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.userplugin.dao.UserPlugin;

public interface CustomUserPluginRepository {

	public Optional<List<UserPlugin>> findAllUserPlugins(Pageable pageable, String collectionName);

	public Optional<List<UserPlugin>> findAllUserPluginsByStatus(String companyId, String status, Pageable pageable,
			String collectionName);

	public Optional<List<UserPlugin>> findAllUserPluginsByCompanyId(String companyId, Pageable pageable,
			String collectionName);

	public Optional<List<UserPlugin>> findAllPublishedUserPlugins(Pageable pageable, String collectionName);

	public Optional<UserPlugin> findUserPluginByCompanyId(String companyId, String id, String collectionName);

}

package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.company.plugin.dao.Plugin;

public interface CustomPluginRepository {
	
	public Optional<Plugin> findPluginByName(String name);
}

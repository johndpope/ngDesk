package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.company.plugin.dao.Plugin;

@Repository
public interface PluginRepository extends CustomPluginRepository, CustomNgdeskRepository<Plugin, String> {

}

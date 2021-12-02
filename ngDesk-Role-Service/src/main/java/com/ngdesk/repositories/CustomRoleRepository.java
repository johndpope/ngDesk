package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ngdesk.role.dao.Role;

@Repository
public interface CustomRoleRepository {

	public Optional<Role> findRoleByName(String name, String collectionName);

	public Optional<Role> findRoleByNameAndRoleId(String name, String roleId, String collectionName);

	public Map saveDefaultTeams(Map hashMap, String collectionName);

	public Optional<Map> findByTeamName(String name, String string);

}

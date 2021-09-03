package com.ngdesk.graphql.company.dao;

import java.util.List;

public class AuthorizedUsersForChat {
	
	List<String> users;

	public AuthorizedUsersForChat() {
		super();
		
	}

	public AuthorizedUsersForChat(List<String> users) {
		super();
		this.users = users;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}
	

}

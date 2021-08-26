package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.sam.controllers.user.dao.User;

public interface CustomUserRepository {
	public Optional<User> findByUserEmail(String email, String collection);
}

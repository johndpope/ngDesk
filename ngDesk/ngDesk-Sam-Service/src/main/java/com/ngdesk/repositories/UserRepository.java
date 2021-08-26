package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.commons.models.User;

@Repository
public interface UserRepository extends CustomUserRepository, CustomNgdeskRepository<User, String> {

}

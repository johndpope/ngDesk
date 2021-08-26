package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.notify.dao.UserToken;

@Repository
public interface UserTokenRepository extends CustomUserTokenRepository, CustomNgdeskRepository<UserToken, String> {

}

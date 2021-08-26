package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.escalation.notify.UserToken;

@Repository
public interface UserTokenRepository extends CustomUserTokenRepository, CustomNgdeskRepository<UserToken, String>{

}

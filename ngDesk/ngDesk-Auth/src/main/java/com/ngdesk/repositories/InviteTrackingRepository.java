package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.auth.forgot.password.InviteTracking;

@Repository
public interface InviteTrackingRepository extends CustomInviteTrackingRepository,CustomNgdeskRepository<InviteTracking, String>{

}

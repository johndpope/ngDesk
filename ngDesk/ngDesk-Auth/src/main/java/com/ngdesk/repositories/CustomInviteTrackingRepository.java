package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import com.ngdesk.auth.forgot.password.InviteTracking;

public interface CustomInviteTrackingRepository {

	public void removeInviteTrackingByUuid(String userUuid, String collectionName);

	public InviteTracking findInviteTrackingByUuidAndTempUuid(String collectionName, String userUuid, String tempUuid);

}

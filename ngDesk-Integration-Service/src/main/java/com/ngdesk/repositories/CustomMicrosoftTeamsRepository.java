package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.integration.microsoft.teams.dao.MicrosoftTeams;

public interface CustomMicrosoftTeamsRepository {

	public Optional<MicrosoftTeams> findByChannelId(String channelId, String collectionName);

}

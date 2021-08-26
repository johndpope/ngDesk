package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import com.ngdesk.workflow.microsoft.teams.dao.MicrosoftTeams;

public interface CustomMicrosoftTeamsRepository {

	public Optional<MicrosoftTeams> findMsTeamEntryByVariable(String variable, String value, String collectionName);

	public List<MicrosoftTeams> findMsTeamEntriesByVariable(String variable, String value, String collectionName);

}

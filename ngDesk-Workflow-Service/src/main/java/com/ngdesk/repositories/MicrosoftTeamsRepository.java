package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.microsoft.teams.dao.MicrosoftTeams;

@Repository
public interface MicrosoftTeamsRepository extends CustomMicrosoftTeamsRepository, CustomNgdeskRepository<MicrosoftTeams, String>{

}

package com.ngdesk.integration.microsoft.teams.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.integration.amazom.aws.dao.DataProxy;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.MicrosoftTeamsRepository;
import com.ngdesk.repositories.ModuleEntryRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class MicrosoftTeamsAPI {

	@Autowired
	MicrosoftTeamsRepository microsoftTeamsRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	MicrosoftTeamsService micrisoftTeamsService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DataProxy dataProxy;

	@PostMapping("/microsoft_team")
	@Operation(summary = "Post Microsoft Team", description = "Post a single microsoft team")
	public MicrosoftTeams postMicrosoftTeams(@Valid @RequestBody MicrosoftTeams microsoftTeams) {

		microsoftTeamsRepository.save(microsoftTeams, "microsoft_teams");

		return microsoftTeams;
	}

	@PutMapping("/microsoft_team")
	@Operation(summary = "Put Microsoft Team", description = "Update microsoft team")
	public MicrosoftTeams putMicrosoftTeams(@Valid @RequestBody MicrosoftTeams microsoftTeams) {

		String channelId = microsoftTeams.getChannelId();
		Optional<MicrosoftTeams> optionalMicrosoftTeams = microsoftTeamsRepository.findByChannelId(channelId,
				"microsoft_teams");
		if (optionalMicrosoftTeams.isEmpty()) {
			throw new NotFoundException("MICROSOFT_TEAMS_CHANNEL_NOT_FOUND", null);
		}
		MicrosoftTeams optionalMicrosoftTeam = optionalMicrosoftTeams.get();
		optionalMicrosoftTeam.setCompanyId(authManager.getUserDetails().getCompanyId());
		optionalMicrosoftTeam.setSubdomain(authManager.getUserDetails().getCompanySubdomain());

		if (!optionalMicrosoftTeam.isAuthenticated() && microsoftTeams.isAuthenticated()) {
			RestTemplate restTemplate = new RestTemplate();
			Map<String, Object> payload = new HashMap<String, Object>();
			payload.put("TEAMS_CONTEXT_ACTIVITY", optionalMicrosoftTeam.getTeamsContextActivity());
			payload.put("SUBDOMAIN", optionalMicrosoftTeam.getSubdomain());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
			String url = "http://localhost:3978/api/authenticated"; // Set URL

			ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(url, request,
					(Class<Map<String, Object>>) (Class) Map.class);
		}

		optionalMicrosoftTeam.setAuthenticated(microsoftTeams.isAuthenticated());
		microsoftTeamsRepository.save(optionalMicrosoftTeam, "microsoft_teams");

		return optionalMicrosoftTeam;
	}

	@PutMapping("/microsoft_team/deactivate")
	@Operation(summary = "Put Microsoft Team", description = "Api call to deactivate microsoft teams channel")
	public MicrosoftTeams deactivateChannel(@Valid @RequestBody MicrosoftTeams microsoftTeams) {

		String channelId = microsoftTeams.getChannelId();
		Optional<MicrosoftTeams> optionalMicrosoftTeams = microsoftTeamsRepository.findByChannelId(channelId,
				"microsoft_teams");
		if (optionalMicrosoftTeams.isEmpty()) {
			throw new NotFoundException("MICROSOFT_TEAMS_CHANNEL_NOT_FOUND", null);
		}

		MicrosoftTeams optionalMicrosoftTeam = optionalMicrosoftTeams.get();
		optionalMicrosoftTeam.setAuthenticated(false);
		optionalMicrosoftTeam.setCompanyId(null);
		optionalMicrosoftTeam.setSubdomain(null);
		microsoftTeamsRepository.save(optionalMicrosoftTeam, "microsoft_teams");

		return optionalMicrosoftTeam;
	}

	@GetMapping("/microsoft_team")
	@Operation(summary = "Get Microsoft Team by Channel Id", description = "Get a single entry of a microsoft team based on  channel id")
	public MicrosoftTeams getMicrosoftTeam(
			@Parameter(description = "Channel ID", required = true) @RequestParam(value = "channel_id", required = true) String channelId) {

		Optional<MicrosoftTeams> optionalMicrosoftTeams = microsoftTeamsRepository.findByChannelId(channelId,
				"microsoft_teams");
		if (optionalMicrosoftTeams.isEmpty()) {
			throw new NotFoundException("MICROSOFT_TEAMS_CHANNEL_NOT_FOUND", null);
		}
		MicrosoftTeams optionalMicrosoftTeam = optionalMicrosoftTeams.get();

		return optionalMicrosoftTeam;
	}

	@GetMapping("/microsoft_teams")
	@Operation(summary = "Get all", description = "Gets all the microsoft teams with pagination")
	@PageableAsQueryParam
	public Page<MicrosoftTeams> getMicrosoftTeams(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable) {

		String companyId = authManager.getUserDetails().getCompanyId();

		return microsoftTeamsRepository.findAllByCompanyId(pageable, "microsoft_teams", companyId);

	}

	@PostMapping("/microsoft_teams/ticket_status")
	@Operation(summary = "Post Status", description = "API call to change status of Tickets by microsoftteams")
	public void postStatus(@RequestBody HashMap<String, Object> entry) {

		if (entry.get("STATUS") == null) {
			return;
		}

		String subDomain = entry.get("SUBDOMAIN").toString();
		String moduleId = entry.get("MODULE_ID").toString();
		String status = entry.get("STATUS").toString();
		String name = entry.get("NAME").toString();
		String dataId = entry.get("DATA_ID").toString();
		String emailAddress = entry.get("EMAIL_ADDRESS").toString();
		String companyId = micrisoftTeamsService.getCompanyIdBySubDomain(subDomain);

		Optional<Map<String, Object>> optionalModule = moduleEntryRepository.findById(moduleId, "modules_" + companyId);

		if (optionalModule.isEmpty()) {
			return;
		}
		Map<String, Object> module = optionalModule.get();
		String moduleName = module.get("NAME").toString();
		if (!moduleName.equals("Tickets")) {
			return;
		}

		Optional<Map<String, Object>> optionalTicketEntry = moduleEntryRepository.findEntryByVariable("_id", dataId,
				"Tickets_" + companyId);

		if (optionalTicketEntry.isEmpty()) {
			return;
		}

		Map<String, Object> ticketEntry = optionalTicketEntry.get();
		if (ticketEntry.get("DELETED").equals(true)) {
			return;
		}

		String ticketStatus = ticketEntry.get("STATUS").toString();

		if (ticketStatus.equals("Closed")) {
			return;
		}

		Optional<Map<String, Object>> optionalUser = moduleEntryRepository.findEntryByVariable("EMAIL_ADDRESS",
				emailAddress, "Users_" + companyId);

		HashMap<String, Object> updatedTicketEntry = new HashMap<String, Object>();
		updatedTicketEntry.put("DATA_ID", dataId);
		updatedTicketEntry.put("STATUS", status);
		String userUUID;

		if (optionalUser.isEmpty()) {

			Map<String, Object> user = micrisoftTeamsService.getUserUUID("system@ngdesk.com", companyId);
			userUUID = user.get("USER_UUID").toString();
			micrisoftTeamsService.addMetaDataMessage(status, user, emailAddress, name, companyId, subDomain, moduleId,
					dataId);

		} else {
			Map<String, Object> user = optionalUser.get();
			userUUID = user.get("USER_UUID").toString();
			micrisoftTeamsService.addMetaDataMessage(status, user, emailAddress, name, companyId, subDomain, moduleId,
					dataId);

		}

		dataProxy.putModuleEntry(updatedTicketEntry, moduleId, false, companyId, userUUID, false);

	}

}

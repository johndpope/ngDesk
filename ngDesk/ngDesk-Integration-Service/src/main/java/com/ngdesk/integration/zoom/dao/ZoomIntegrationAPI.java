package com.ngdesk.integration.zoom.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.bson.internal.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.integration.company.dao.Company;
import com.ngdesk.repositories.CompanyRepository;
import com.ngdesk.repositories.ZoomRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class ZoomIntegrationAPI {

	@Autowired
	ZoomRepository zoomRepository;

	@Value("${zoom.client.id}")
	String zoomClientId;

	@Value("${zoom.client.secret}")
	String zoomClientSecret;

	@Autowired
	ZoomService zoomService;

	@Autowired
	AuthManager authManager;

	@Autowired
	CompanyRepository companyRepository;

	@GetMapping("/zoom/authorized")
	@Operation(summary = "Get Zoom Authorized", description = "Get the zoom authorized")
	public void authrorizeZoom(
			@Parameter(description = "Code", required = false) @RequestParam(value = "code", required = false) String code,
			@Parameter(description = "State", required = false) @RequestParam(value = "state", required = false) String state) {

		if (state == null) {
			String[] vars = { "COMPANY_SUBDOMAIN" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}

		Optional<Company> optionalCompany = companyRepository.getCompanyBySubdomain(state);
		if (optionalCompany.isEmpty()) {
			String[] vars = { state };
			throw new BadRequestException("COMPANY_SUBDOMAIN_NOT_FOUND", vars);
		}

		Optional<ZoomIntegrationData> optionalZoomData = zoomRepository
				.findZoomDataByCompany(optionalCompany.get().getComapnyId());

		ZoomIntegrationData zoomData = new ZoomIntegrationData();
		if (optionalZoomData.isPresent()) {
			zoomData = optionalZoomData.get();
		}

		zoomData.setCode(code);
		zoomData.setCompanyId(optionalCompany.get().getComapnyId());
		zoomData.setDateCreated(new Date());
		zoomData.setTokenUpdatedDate(new Date());

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders httpHeaders = new HttpHeaders();

		String base64String = Base64.encode((zoomClientId + ":" + zoomClientSecret).getBytes());
		httpHeaders.add("Authorization", "Basic " + base64String);

		// ZOOM AUTHORIZATION URL
		String url = "https://zoom.us/oauth/token?grant_type=authorization_code&code=" + code;

		HttpEntity<Map<String, String>> entity = new HttpEntity<>(httpHeaders);
		try {
			ResponseEntity<ZoomAuthentication> response = restTemplate.postForEntity(url, entity,
					ZoomAuthentication.class);

			ZoomAuthentication authentication = response.getBody();

			zoomData.setZoomAuthentication(authentication);

			zoomData.setZoomUserInformation(zoomService.getZoomUserInformation(authentication.getAccessToken()));
			zoomRepository.save(zoomData, "zoom_integrations");
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			String vars[] = { e.getStatusCode().toString() };
			throw new BadRequestException("ZOOM_INTEGRATION_ERROR", vars);
		}

	}

	@GetMapping("/zoom/status")
	@Operation(summary = "Get Zoom Status", description = "Get the status of zoom")
	public ZoomStatus getZoomStatus() {
		ZoomStatus status = new ZoomStatus();
		if (authManager.getUserDetails().getCompanyId() == null) {
			status.setZoomAuthenticated(false);
			return status;
		}

		String companySubdomain = authManager.getUserDetails().getCompanySubdomain();
		Optional<ZoomIntegrationData> zoomRepoObject = zoomRepository
				.findZoomDataByCompany(authManager.getUserDetails().getCompanyId());

		if (!zoomRepoObject.isEmpty()) {
			status.setZoomAuthenticated(true);

			status.setZoomUserInformation(zoomRepoObject.get().getZoomUserInformation());
		} else {
			// IF FALSE NAVIGATE THEM TO COMPANY SETTINGS IF THE USER IS A SYSTEM ADMIN ELSE
			// SHOW MESSAGE CONTACT ADMIN
			status.setZoomAuthenticated(false);
		}
		return status;
	}

	@PostMapping("/zoom/create_meeting")
	@Operation(summary = "Post Zoom Meeting", description = "Api call to post zoom meeting")
	public ZoomMeeting generateZoomMeetingLink(@RequestBody @Valid MeetingRequest meetingRequest) {
		Optional<ZoomIntegrationData> optionalZoomData = zoomRepository
				.findZoomDataByCompany(authManager.getUserDetails().getCompanyId());

		if (!optionalZoomData.isPresent()) {
			throw new BadRequestException("ZOOM_DATA_NOT_FOUND", null);
		}

		ZoomIntegrationData zoomIntegrationData = optionalZoomData.get();

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR, -1);
		Date pastDate = cal.getTime();

		ZoomMeeting zoomMeeting = new ZoomMeeting();

		if (zoomIntegrationData.getTokenUpdatedDate().before(pastDate)) {
			// MAKE API CALL AND GET NEW REFRESH TOKEN
			ZoomAuthentication authentication = zoomService
					.refreshAccessToken(zoomIntegrationData.getZoomAuthentication().getRefreshToken());

			zoomIntegrationData.setZoomAuthentication(authentication);
			zoomIntegrationData.setTokenUpdatedDate(new Date());

		}

		zoomMeeting = zoomService.createInstantZoomMeeting(zoomIntegrationData.getZoomUserInformation().getUserId(),
				zoomIntegrationData.getZoomAuthentication().getAccessToken(), meetingRequest.getTopic());

		if (zoomIntegrationData.getMeetingsScheduled() == null) {
			zoomIntegrationData.setMeetingsScheduled(new ArrayList<ZoomMeeting>());
		}

		zoomMeeting.setEntryId(meetingRequest.getEntryId());
		zoomMeeting.setModuleId(meetingRequest.getModuleId());
		zoomMeeting.setDateCreated(new Date());
		zoomMeeting.setCreatedBy(authManager.getUserDetails().getUserId());

		zoomIntegrationData.getMeetingsScheduled().add(zoomMeeting);
		zoomRepository.save(zoomIntegrationData, "zoom_integrations");
		zoomService.postDiscussionToEntry(meetingRequest.getEntryId(), meetingRequest.getModuleId(), zoomMeeting);

		return zoomMeeting;
	}

	@PostMapping("/zoom/uninstall")
	@Operation(summary = "Post Zoom Uninstall", description = "Api call to uninstall zoom")
	public void getUninstall(@RequestBody Map<String, Object> body) {

		Map<String, Object> payload = (Map<String, Object>) body.get("payload");
		String event = body.get("event").toString();
		String accountId = payload.get("account_id").toString();
		String userId = payload.get("user_id").toString();

		String dataRetention = payload.get("user_data_retention").toString();
		zoomRepository.removeZoomData(accountId, userId);
		if (dataRetention.equalsIgnoreCase("false")) {
			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders httpHeaders = new HttpHeaders();

			String base64String = Base64.encode((zoomClientId + ":" + zoomClientSecret).getBytes());
			httpHeaders.add("Authorization", "Basic " + base64String);

			// ZOOM AUTHORIZATION URL
			String url = "https://api.zoom.us/oauth/data/compliance";

			Map<String, Object> dataCompliancePayload = new HashMap<String, Object>();
			dataCompliancePayload.put("account_id", accountId);
			dataCompliancePayload.put("client_id", payload.get("client_id").toString());
			dataCompliancePayload.put("compliance_completed", true);
			dataCompliancePayload.put("deauthorization_event_received", payload);
			dataCompliancePayload.put("user_id", userId);

			HttpEntity<Map<String, Object>> entity1 = new HttpEntity<>(dataCompliancePayload, httpHeaders);

			ResponseEntity<Map> response3 = restTemplate.exchange(url, HttpMethod.POST, entity1, Map.class);
		}
	}

}

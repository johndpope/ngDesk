package com.ngdesk.sam.controllers.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ControllerRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class ControllersAPI {

	@Autowired
	private AuthManager authManager;

	@Autowired
	private ControllerRepository controllerRepository;

	@Autowired
	private ControllerService controllerService;

	@Autowired
	private ElasticSearch elasticSearch;

	@Autowired
	private ControllerVersion controllerVersion;

	@GetMapping("/controllers")
	@Operation(summary = "Get all", description = "Gets all the controllers with pagination and search")
	@PageableAsQueryParam
	public Page<Controller> getControllers(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable,
			@Parameter(description = "Search string to search controllers", required = false, example = "HOST_NAME=Laptop~~LAST_SEEN=2020-04-21T08:03:345Z~2020-04-26T08:03:345Z") String search) {

		boolean isAuthorised = controllerService.isAuthorised();
		if (!isAuthorised) {
			throw new ForbiddenException("FORBIDDEN");
		}

		if (!StringUtils.isBlank(search)) {
			List<ObjectId> controllerIds = elasticSearch.getObjectIdsFromElastic(search,
					authManager.getUserDetails().getCompanyId());
			return controllerRepository.findByControllerIdsAndCompanyId(controllerIds, pageable,
					authManager.getUserDetails().getCompanyId(), "controllers");
		}
		return controllerRepository.findAllByCompanyId(pageable, "controllers",
				authManager.getUserDetails().getCompanyId());
	}

	@GetMapping("/controller/{controller_id}")
	@Operation(summary = "Get by Controller ID", description = "Gets the controller based on controller uuid")
	public Controller getControllerById(
			@Parameter(description = "Controller ID", required = true) @PathVariable("controller_id") String controllerId) {

		boolean isAuthorised = controllerService.isAuthorised();
		if (!isAuthorised) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<Controller> optional = controllerRepository.findByIdAndCompanyId(controllerId,
				authManager.getUserDetails().getCompanyId(), "controllers");
		if (optional.isEmpty()) {
			String vars[] = { "CONTROLLER" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		return optional.get();
	}

	@Operation(summary = "Post", description = "Api call to post a controller")
	@PostMapping("/controller")
	public Controller postController(@Valid @RequestBody Controller controller) {
		boolean isAuthorised = controllerService.isAuthorised();
		String companyId = authManager.getUserDetails().getCompanyId();
		if (!isAuthorised) {
			throw new ForbiddenException("FORBIDDEN");
		}
		controller.setCompanyId(companyId);
		controller.setVersion(controllerVersion.version.getVersion());
		for (SubApp sub : controller.getSubAppList()) {
			sub.setLastSeen(new Date());
			SubAppVersion subApp = controllerVersion.version.getSubAppVersions().stream()
					.filter(subAppVersion -> subAppVersion.getName().equals(sub.getName())).findFirst().orElse(null);
			sub.setVersion(subApp.getVersion());
		}
		controller.setLastSeen(new Date());
		controller = controllerRepository.save(controller, "controllers");
		elasticSearch.insertControllerToElastic(controller);
		return controller;
	}

	@Operation(summary = "Get Versions for controller and sub apps", description = "Api to get versions of controller and sub apps")
	@GetMapping("/versions")
	public Version getVersions() {
		return controllerVersion.version;
	}

}

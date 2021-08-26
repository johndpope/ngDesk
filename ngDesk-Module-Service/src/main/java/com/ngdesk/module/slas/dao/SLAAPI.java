package com.ngdesk.module.slas.dao;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.field.dao.FieldAPI;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.SLARepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class SLAAPI {

	@Autowired
	AuthManager authManager;

	@Autowired
	SLARepository slaRepository;

	@Autowired
	RoleService roleService;

	@Autowired
	FieldAPI fieldAPI;

	@Autowired
	SLAService slaService;

	@PostMapping("/module/{moduleId}/sla")
	@Operation(summary = "Post SLA", description = "Post Service Level Agreements")
	public SLA postSla(@Valid @RequestBody SLA sla,
			@Parameter(description = "Module ID", required = true) @PathVariable("moduleId") String moduleId) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		sla = slaService.setDefaultFields(sla, null, moduleId);
		slaRepository.save(sla, "slas");
		slaService.postSlaField(sla, moduleId);
		return sla;

	}

	@PutMapping("/module/{moduleId}/sla")
	@Operation(summary = "Put SLA", description = "Update Service Level Agreements")
	public SLA putSla(@Valid @RequestBody SLA sla,
			@Parameter(description = "Module ID", required = true) @PathVariable String moduleId) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		String slaId = sla.getSlaId();
		String companyId = authManager.getUserDetails().getCompanyId();
		Optional<SLA> optionalExistingSla = slaRepository.findSlaBySlaId(slaId, companyId, moduleId);
		if (optionalExistingSla.isEmpty()) {
			throw new BadRequestException("SLA_ID_INVALID", null);
		}
		SLA existingSla = optionalExistingSla.get();
		sla = slaService.setDefaultFields(sla, existingSla, moduleId);
		slaService.isSlaNameChanged(sla);
		return slaRepository.save(sla, "slas");

	}

	@PutMapping("module/{moduleId}/sla/{slaId}")
	@Operation(summary = "Put Enable SLA", description = "Enable Service Level Agreements")
	public void putSlaEnable(@Parameter(description = "sla ID", required = true) @PathVariable("slaId") String slaId,
			@Parameter(description = "Module ID", required = true) @PathVariable("moduleId") String moduleId) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		String companyId = authManager.getUserDetails().getCompanyId();
		Optional<SLA> optionalExistingSla = slaRepository.findSlaDeletedBySlaId(slaId, companyId, moduleId);
		if (optionalExistingSla.isEmpty()) {
			throw new BadRequestException("SLA_ID_NOT_DISABLED", null);
		}
		slaRepository.updateSla(slaId, companyId, moduleId, false);

	}

	@DeleteMapping("/module/{moduleId}/sla/{slaId}")
	@Operation(summary = "Delete SLA", description = "Delete Service Level Agreements")
	public void deleteSla(@Parameter(description = "sla ID", required = true) @PathVariable("slaId") String slaId,
			@Parameter(description = "Module ID", required = true) @PathVariable("moduleId") String moduleId) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		String companyId = authManager.getUserDetails().getCompanyId();
		Optional<SLA> optionalExistingSla = slaRepository.findSlaBySlaId(slaId, companyId, moduleId);
		if (optionalExistingSla.isEmpty()) {
			throw new BadRequestException("SLA_ID_INVALID", null);
		}
		slaRepository.updateSla(slaId, companyId, moduleId, true);

	}
}

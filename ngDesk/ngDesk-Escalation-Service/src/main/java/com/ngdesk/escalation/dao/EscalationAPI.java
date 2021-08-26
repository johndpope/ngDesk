package com.ngdesk.escalation.dao;

import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.springdoc.core.converters.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.EscalationRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class EscalationAPI {

	@Autowired
	private EscalationRepository escalationRepository;

	@Autowired
	private AuthManager authManager;

	@GetMapping("/escalations")
	@Operation(summary = "Get all", description = "Gets all the escalations with pagination and search")
	@PageableAsQueryParam
	public Page<Escalation> getEscalations(
			@Parameter(description = "Pageable object to control pagination", required = true, hidden = true) Pageable pageable)
			throws JsonProcessingException {
		return escalationRepository.findAll(pageable, "escalations_" + authManager.getUserDetails().getCompanyId());
	}

	@GetMapping("/escalations/{id}")
	@Operation(summary = "Get by ID", description = "Gets the escalation based on ID")
	public Escalation getEscalationById(
			@Parameter(description = "Escalation ID", required = true) @PathVariable("id") String id) {
		Optional<Escalation> optional = escalationRepository.findById(id,
				"escalations_" + authManager.getUserDetails().getCompanyId());
		if (optional.isEmpty()) {
			String vars[] = { "ESCALATION" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		return optional.get();
	}

	@PostMapping("/escalation")
	@Operation(summary = "Post Escalation", description = "Post a single escalation")
	public Escalation postEscalation(@Valid @RequestBody Escalation escalation) {
		escalation.setDateCreated(new Date());
		escalation.setDateCreated(new Date());
		escalation.setLastUpdated(authManager.getUserDetails().getUserId());
		escalation.setCreatedBy(authManager.getUserDetails().getUserId());

		return escalationRepository.save(escalation, "escalations_" + authManager.getUserDetails().getCompanyId());
	}

	@PutMapping("/escalation")
	@Operation(summary = "Put Escalation", description = "Update a escalation")
	public Escalation putEscalation(@Valid @RequestBody Escalation escalation) {

		Optional<Escalation> optional = escalationRepository.findById(escalation.getId(),
				"escalations_" + authManager.getUserDetails().getCompanyId());

		if (optional.isEmpty()) {
			String vars[] = { "ESCALATION" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		Escalation existingEscalation = optional.get();

		escalation.setDateCreated(existingEscalation.getDateCreated());
		escalation.setCreatedBy(existingEscalation.getCreatedBy());
		escalation.setDateUpdated(new Date());
		escalation.setLastUpdated(authManager.getUserDetails().getUserId());

		// TODO: Check validations
		return escalationRepository.save(escalation, "escalations_" + authManager.getUserDetails().getCompanyId());

	}

	@DeleteMapping("/escalation/{id}")
	@Operation(summary = "Delete escalation", description = "Delete a escalation by ID")
	public void deleteEscalation(
			@Parameter(description = "Escalation ID", required = true) @PathVariable("id") String id) {
		Optional<Escalation> optional = escalationRepository.findById(id,
				"escalations_" + authManager.getUserDetails().getCompanyId());

		if (optional.isEmpty()) {
			String vars[] = { "ESCALATION" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		escalationRepository.deleteById(id, "escalations_" + authManager.getUserDetails().getCompanyId());
	}

}

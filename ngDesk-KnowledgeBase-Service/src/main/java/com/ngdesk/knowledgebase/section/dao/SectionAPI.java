package com.ngdesk.knowledgebase.section.dao;

import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.section.SectionRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class SectionAPI {

	@Autowired
	AuthManager authManager;

	@Autowired
	SectionRepository sectionRepository;

	@PostMapping("/sections")
	@Operation(summary = "Post Section", description = "Post Sections")
	public Section postSection(@Valid @RequestBody Section section) {

		Section postSection = setDefaultValuesToPost(section);

		return sectionRepository.save(postSection, "sections_" + authManager.getUserDetails().getCompanyId());

	}

	@PutMapping("/sections")
	@Operation(summary = "Put Section", description = "Update a Section")
	public Section updateSection(@Valid @RequestBody Section section) {

		Optional<Section> optionalExistingSection = sectionRepository.findById(section.getSectionId(),
				"sections_" + authManager.getUserDetails().getCompanyId());
		if (optionalExistingSection.isEmpty()) {
			String vars[] = { "SECTION" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		Section existingSection = optionalExistingSection.get();
		Section updatedSection = setDefaultValuesForUpdate(section, existingSection);
		return sectionRepository.save(updatedSection, "sections_" + authManager.getUserDetails().getCompanyId());
	}

	@DeleteMapping("/sections/{id}")
	@Operation(summary = "Deletes a Section", description = "Deletes a Section")
	public void deleteSection(
			@Parameter(description = "Section ID", required = true) @PathVariable("id") String sectionId) {

		if (sectionRepository.findById(sectionId, "sections_" + authManager.getUserDetails().getCompanyId())
				.isEmpty()) {
			String vars[] = { "SECTION" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		Optional<Section> existingSection = sectionRepository.findById(sectionId,
				"sections_" + authManager.getUserDetails().getCompanyId());
		if (existingSection.isEmpty()) {
			String vars[] = { "SECTION" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);

		}
		sectionRepository.deleteById(sectionId, "sections_" + authManager.getUserDetails().getCompanyId());
	}

	public Section setDefaultValuesToPost(Section section) {
		int totalCount = sectionRepository.getCount("sections_" + authManager.getUserDetails().getCompanyId());
		section.setOrder(totalCount + 1);
		section.setDateCreated(new Date());
		section.setDateUpdated(new Date());
		section.setCreatedBy(authManager.getUserDetails().getUserId());
		section.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		return section;

	}

	public Section setDefaultValuesForUpdate(Section section, Section existingSection) {
		section.setOrder(existingSection.getOrder());
		section.setCreatedBy(existingSection.getCreatedBy());
		section.setDateCreated(existingSection.getDateCreated());
		section.setDateUpdated(new Date());
		section.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		return section;
	}

}

package com.ngdesk.schedule.dao;

import java.util.Date;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ScheduleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RefreshScope
public class ScheduleAPI {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private AuthManager authManager;

	@Autowired
	private ScheduleRepository scheduleRepository;


	@Operation(summary = "Get all", description = "Gets all the schedules with pagination and search")
	@GetMapping("/schedules")
	public Page<Schedule> getSchedules(
			@Parameter(description = "Pageable object to control pagination", required = true) Pageable pageable) {
		return scheduleRepository.findAll(pageable, "schedules_" + authManager.getUserDetails().getCompanyId());
	}

	@Operation(summary = "Get by Name", description = "Gets the schedule based on Name")
	@GetMapping("/schedule/{id}")
	public Schedule getScheduleById(
			@Parameter(description = "Schedule ID", required = true) @PathVariable("id") String id) {
		Optional<Schedule> optional = scheduleRepository.findById(id,
				"schedules_" + authManager.getUserDetails().getCompanyId());
		if (optional.isEmpty()) {
		//	throw new NotFoundException("SCHEDULE_NOT_FOUND");
		}
		return optional.get();
	}

	@Operation(summary = "Post Schedule", description = "Post a single schedule")
	@PostMapping("/schedule")
	public Schedule postSchedule(@Valid @RequestBody Schedule schedule) {
		schedule.setDateCreated(new Date());
		schedule.setDateCreated(new Date());
		schedule.setCreatedBy(authManager.getUserDetails().getUserId());
		schedule.setLastUpdatedBy(authManager.getUserDetails().getUserId());
		schedule = scheduleRepository.save(schedule, "schedules_" + authManager.getUserDetails().getCompanyId());
		return schedule;
	}

	@Operation(summary = "Put Schedule", description = "Update a schedule")
	@PutMapping("/schedules/{id}")
	public Schedule putSchedule(@Valid @RequestBody Schedule schedule) {

		Optional<Schedule> optional = scheduleRepository.findById(schedule.getId(),
				"schedules_" + authManager.getUserDetails().getCompanyId());

		if (optional.isEmpty()) {
		//	throw new NotFoundException("SCHEDULE_NOT_FOUND");
		}

		Schedule existingSchedule = optional.get();

		schedule.setDateCreated(existingSchedule.getDateCreated());
		schedule.setDateUpdated(new Date());
		schedule.setCreatedBy(authManager.getUserDetails().getUserId());
		schedule.setLastUpdatedBy(authManager.getUserDetails().getUserId());

		// TODO: Check validations
		schedule = scheduleRepository.save(schedule, "schedules_" + authManager.getUserDetails().getCompanyId());

		return schedule;
	}

	@Operation(summary = "Delete schedule", description = "Delete a schedule")
	@DeleteMapping("/schedule/{id}")
	public void deleteSchedule(@Parameter(description = "Schedule ID", required = true) @PathVariable("id") String id) {
		Optional<Schedule> optional = scheduleRepository.findById(id,
				"schedules_" + authManager.getUserDetails().getCompanyId());

		if (optional.isEmpty()) {
		//	throw new NotFoundException("SCHEDULE_NOT_FOUND");
		}

		scheduleRepository.deleteById(id, "schedules_" + authManager.getUserDetails().getCompanyId());
	}

//	public String getNameofUser(String companyId, String userId) {
//
//		try {
//			log.trace("Enter ScheduleService.getNameofUser() companyId: " + companyId + ", userId: " + userId);
//			String collectionName = "Users_" + companyId;
//			MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
//
//			if (new ObjectId().isValid(userId)) {
//				Document userDocument = collection.find(Filters.eq("_id", new ObjectId(userId))).first();
//				if (userDocument != null) {
//					String firstName = userDocument.getString("FIRST_NAME");
//					String lastName = userDocument.getString("LAST_NAME");
//					log.trace("Exit ScheduleService.getNameofUser() companyId: " + companyId + ", userId: " + userId);
//					return new String(firstName + " " + lastName);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
////			 TODO
////			throw new InternalErrorException("INTERNAL_ERROR");
//		}
//
//		return "";
//	}

}

package com.ngdesk.module.task.dao;

import java.util.Date;
import java.util.Optional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.role.dao.RoleService;
import com.ngdesk.repositories.task.TaskRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
public class TaskAPI {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private AuthManager auth;
	@Autowired
	private RoleService roleService;

	@PostMapping("/module/{moduleId}/task")
	@Operation(summary = "Post Task", description = "Post a single Task")
	public Task addTask(@Valid @RequestBody Task task, @PathVariable String moduleId) {

		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		task.setDateCreated(new Date());
		task.setCompanyId(auth.getUserDetails().getCompanyId());
		task.setCreatedBy(auth.getUserDetails().getUserId());
		task.setDateUpdated(new Date());
		task.setLastUpdatedBy(auth.getUserDetails().getUserId());
		task.setStopDate(task.getStopDate());
		return taskRepository.save(task, "tasks");
	}

	@PutMapping("/module/{moduleId}/task")
	@Operation(summary = "Put Task", description = "Update a Task")
	public Task updateTask(@Valid @RequestBody Task task, @PathVariable String moduleId) {
		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}
		Optional<Task> optionalExistingTask = taskRepository.findById(task.getTaskId(), "tasks");
		if (optionalExistingTask.isEmpty()) {
			String vars[] = { "TASK" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		Task existingTask = optionalExistingTask.get();
		task.setDateCreated(existingTask.getDateCreated());
		task.setCreatedBy(existingTask.getCreatedBy());
		task.setLastExecuted(existingTask.getLastExecuted());
		task.setDateUpdated(new Date());
		task.setLastUpdatedBy(auth.getUserDetails().getUserId());
		task.setCompanyId(auth.getUserDetails().getCompanyId());
		task.setStopDate(task.getStopDate());
		return taskRepository.save(task, "tasks");
	}

	@DeleteMapping("/module/{moduleId}/task/{taskId}")

	@Operation(summary = "Deletes a Task", description = "Deletes a Task")
	public void deleteTask(@Parameter(description = "Task ID", required = true) @PathVariable("taskId") String taskId,
			@Parameter(description = "Module ID", required = true) @PathVariable("moduleId") String moduleId) {
		if (!roleService.isSystemAdmin()) {
			throw new ForbiddenException("FORBIDDEN");
		}

		String companyId = auth.getUserDetails().getCompanyId();
		if (taskRepository.findById(moduleId, "modules_" + companyId).isEmpty()) {
			String vars[] = { "MODULE" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}

		Optional<Task> existingTask = taskRepository.findById(taskId, "tasks");
		if (existingTask.isEmpty()) {
			String vars[] = { "TASK" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		taskRepository.deleteById(taskId, "tasks");
	}

}

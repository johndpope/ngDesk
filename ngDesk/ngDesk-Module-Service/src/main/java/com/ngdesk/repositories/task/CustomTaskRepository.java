package com.ngdesk.repositories.task;

import java.util.List;
import java.util.Optional;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.task.dao.Task;;

public interface CustomTaskRepository {
	
	Optional<Module> findByModuleId(String moduleId, String collectionName);
	List<Task> getAllTasks(String collectionName);
	
	Optional<Task> findTaskByName(String name,String companyId, String moduleId,String collectionName);

	public Optional<Task> findOtherTaskWithDuplicateName(String name, String companyId, String taskId, String moduleId,String collectionName) ;
	
	public void updateLastExecutedDate(String taskId, String companyId);

}  

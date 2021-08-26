package com.ngdesk.repositories.task;

import com.ngdesk.module.task.dao.Task;
import com.ngdesk.repositories.CustomNgdeskRepository;

public interface TaskRepository extends CustomTaskRepository,CustomNgdeskRepository<Task, String>{

}

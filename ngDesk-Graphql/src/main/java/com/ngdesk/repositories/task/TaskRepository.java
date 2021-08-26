package com.ngdesk.repositories.task;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.task.dao.Task;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface TaskRepository extends CustomTaskRepository, CustomNgdeskRepository<Task, String>{

}

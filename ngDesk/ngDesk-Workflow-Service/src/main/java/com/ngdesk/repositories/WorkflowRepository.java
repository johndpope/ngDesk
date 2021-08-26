package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.dao.Workflow;

@Repository
public interface WorkflowRepository extends CustomWorkflowRepository, CustomNgdeskRepository<Workflow, String>{

}

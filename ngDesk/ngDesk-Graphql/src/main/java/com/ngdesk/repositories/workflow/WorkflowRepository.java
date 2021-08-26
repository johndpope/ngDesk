package com.ngdesk.repositories.workflow;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.workflow.Workflow;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface WorkflowRepository extends CustomNgdeskRepository<Workflow, String>, CustomWorkflowRepository {

}

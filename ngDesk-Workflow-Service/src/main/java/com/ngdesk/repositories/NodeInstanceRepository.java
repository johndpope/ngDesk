package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.executor.dao.NodeInstance;

@Repository
public interface NodeInstanceRepository extends CustomNgdeskRepository<NodeInstance, String>{

}

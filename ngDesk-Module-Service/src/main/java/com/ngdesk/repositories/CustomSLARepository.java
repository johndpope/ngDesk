package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import com.ngdesk.module.slas.dao.SLA;
import com.ngdesk.workflow.dao.Workflow;

public interface CustomSLARepository {

	public Optional<Workflow> findWorkflowByModuleIdAndWorkflowId(String workflowId, String moduleId, String companyId);

	public Optional<SLA> findDuplicateSlaName(String name, String companyId, String moduleId);

	public Optional<SLA> findOtherSlaWithDuplicateName(String name, String companyId, String SlaId, String moduleId);

	public Optional<Map<String, Object>> findTeamByTeamId(String teamId,String name, String collectionName);

	public Optional<SLA> findSlaBySlaId(String slaId, String companyId, String moduleId);

	public Optional<SLA> findSlaDeletedBySlaId(String slaId, String companyId, String moduleId);

	public Optional<Map<String,Object>> findBycompanyId(String companyId, String collectionName);
	
	public void updateSla(String slaId, String companyId, String moduleId, boolean value);

	
	
}

package com.ngdesk.repositories;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.ngdesk.module.slas.dao.SLA;
import com.ngdesk.workflow.dao.Workflow;

public class CustomSLARepositoryImpl implements CustomSLARepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<SLA> findDuplicateSlaName(String name, String companyId, String moduleId) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("name").is(name), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), SLA.class, "slas"));

	}

	@Override
	public Optional<SLA> findOtherSlaWithDuplicateName(String SlaName, String companyId, String SlaId,
			String moduleId) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("name").is(SlaName), Criteria.where("_id").ne(SlaId),
				Criteria.where("moduleId").is(moduleId), Criteria.where("companyId").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), SLA.class, "slas"));

	}

	@Override
	public Optional<Workflow> findWorkflowByModuleIdAndWorkflowId(String workflowId, String moduleId,
			String companyId) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("_id").is(workflowId),
				Criteria.where("COMPANY_ID").is(companyId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Workflow.class, "module_workflows"));

	}

	@Override
	public Optional<Map<String, Object>> findTeamByTeamId(String teamId, String name, String collectionName) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("DELETED").is(false), Criteria.where("EFFETIVE_TO").is(null),
				Criteria.where("_id").is(teamId), Criteria.where("NAME").is(name));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));

	}

	@Override
	public Optional<SLA> findSlaBySlaId(String slaId, String companyId, String moduleId) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(slaId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId), Criteria.where("deleted").is(false));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), SLA.class, "slas"));

	}

	@Override
	public Optional<SLA> findSlaDeletedBySlaId(String slaId, String companyId, String moduleId) {

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(slaId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId), Criteria.where("deleted").is(true));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), SLA.class, "slas"));

	}

	@Override
	public void updateSla(String slaId, String companyId, String moduleId, boolean value) {

		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(slaId), Criteria.where("companyId").is(companyId),
				Criteria.where("moduleId").is(moduleId));
		query.addCriteria(criteria);
		mongoOperations.updateFirst(query, new Update().update("deleted", value), "slas");

	}

	@Override
	public Optional<Map<String, Object>> findBycompanyId(String companyId, String collectionName) {
		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(companyId));
		query.addCriteria(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Map.class, collectionName));
	}

}

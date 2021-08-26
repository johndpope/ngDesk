package com.ngdesk.repositories.sla;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.slas.dao.SLA;


public interface CustomSLARepository {
public Optional<SLA> findSlaById(String companyId,String moduleId,String slaId, String collectionName);
	
	public List<SLA>findAllSlas(String companyId,String moduleId,Pageable pageable,String collectionName);

	Integer count(String companyId,String moduleId, String collectionName);
}

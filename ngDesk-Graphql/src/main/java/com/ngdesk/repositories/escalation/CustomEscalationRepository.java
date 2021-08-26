package com.ngdesk.repositories.escalation;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.escalation.dao.Escalation;

public interface CustomEscalationRepository {

	public List<Escalation> findAllEscalations(Pageable pageable, String collectionName);

	public Integer getEscalationCount(String collectionName);
}

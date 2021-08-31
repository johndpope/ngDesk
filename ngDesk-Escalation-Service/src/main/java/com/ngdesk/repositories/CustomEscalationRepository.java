package com.ngdesk.repositories;

import java.util.Map;
import java.util.Optional;

import com.ngdesk.escalation.dao.Escalation;

public interface CustomEscalationRepository {

	public Optional<Escalation> findEscalationByName(String name, String collection);

	public Optional<Escalation> findOtherEscalationsWithDuplicateName(String name, String escalationId,
			String collection);

	public Optional<Map<String, Object>> validateById(String userId, String collectionName);
}

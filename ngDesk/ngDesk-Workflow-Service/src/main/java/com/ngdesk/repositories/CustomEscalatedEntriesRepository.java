package com.ngdesk.repositories;

import java.util.Optional;

import com.ngdesk.workflow.escalation.dao.EscalatedEntries;

public interface CustomEscalatedEntriesRepository {

	public Optional<EscalatedEntries> findEscalatedEntries(String entryId, String escalationId, String collectionName);
	
	public Optional<EscalatedEntries> deleteEscalatedEntries(String entryId,String collectionName);
}

package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.escalation.dao.EscalatedEntries;

@Repository
public interface EscalatedEntriesRepository extends CustomNgdeskRepository<EscalatedEntries, String>, CustomEscalatedEntriesRepository {

}

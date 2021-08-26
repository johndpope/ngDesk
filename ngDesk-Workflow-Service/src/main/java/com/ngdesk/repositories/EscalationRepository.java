package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.workflow.escalation.dao.Escalation;

public interface EscalationRepository extends CustomNgdeskRepository<Escalation, String> {

}

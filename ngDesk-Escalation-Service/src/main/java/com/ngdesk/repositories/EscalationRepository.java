package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.escalation.dao.Escalation;

@Repository
public interface EscalationRepository extends CustomEscalationRepository, CustomNgdeskRepository<Escalation, String> {

	
	
}

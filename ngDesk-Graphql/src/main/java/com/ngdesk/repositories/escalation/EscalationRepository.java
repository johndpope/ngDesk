package com.ngdesk.repositories.escalation;

import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.escalation.dao.Escalation;
import com.ngdesk.repositories.CustomNgdeskRepository;

@Repository
public interface EscalationRepository extends CustomNgdeskRepository<Escalation, String>, CustomEscalationRepository {

	
	
	
}

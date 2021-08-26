package com.ngdesk.escalation.dao;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.escalation.RepositoryTest;
import com.ngdesk.repositories.EscalationRepository;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RepositoryTest.class })
@SpringBootTest
public class EscalationRepositoryTest {

	@Autowired
	EscalationRepository escalationRepository;

	@Autowired
	MongoOperations mongoOperations;

	@BeforeEach
	public void setup() throws Exception {
		mongoOperations.dropCollection("escalations_test");
	}

	@DisplayName("Test to determine if duplicate names are saved or not")
	@Test
	public void testPostEscalationDuplicateName() {
		List<String> ids = new ArrayList<String>();
		ids.add(new ObjectId().toString());

		List<EscalationRule> rules = new ArrayList<EscalationRule>();
		rules.add(new EscalationRule(1, 1, new EscalateTo(new ArrayList<String>(), ids, new ArrayList<String>())));

		Escalation escalation = new Escalation(null, "Test Escalation", "Test Description", rules, null, null, null,
				null);

		Escalation duplicateEscalation = new Escalation(null, "Test Escalation", "Test Description", rules, null, null,
				null, null);

		escalationRepository.save(escalation, "escalations_test");
		assertThrows(BadRequestException.class, () -> {
			escalationRepository.save(duplicateEscalation, "escalations_test");
		});
	}

	@AfterEach
	public void tearDown() throws Exception {
		mongoOperations.dropCollection("escalations_test");
	}
}

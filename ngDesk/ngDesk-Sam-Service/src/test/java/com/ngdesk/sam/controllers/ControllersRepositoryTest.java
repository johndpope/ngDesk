package com.ngdesk.sam.controllers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.ArrayList;
import java.util.List;

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

import com.ngdesk.repositories.ControllerRepository;
import com.ngdesk.sam.RepositoryTest;
import com.ngdesk.sam.controllers.dao.Controller;
import com.ngdesk.sam.controllers.dao.ControllerService;
import com.ngdesk.sam.controllers.dao.SubApp;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RepositoryTest.class })
@SpringBootTest
//@ActiveProfiles("test")
public class ControllersRepositoryTest {

	@Autowired
	ControllerRepository controllerRepository;

	@Autowired
	MongoOperations mongoOperations;

	@Autowired
	ControllerService controllerService;

	@BeforeEach
	public void setup() throws Exception {
		mongoOperations.dropCollection("controllers");
	}

	@DisplayName("Test to determine if duplicate names are saved or not")
	@Test
	public void testPostControllerDuplicateName() {

		List<SubApp> subApps = new ArrayList<SubApp>();
		subApps.add(new SubApp("NAME", "Online", null, 1));
		Controller controller = new Controller(null, "Test Controllers", subApps, "Online", null, null, 1);
		Controller duplicateController = new Controller(null, "Test Controllers", subApps, "Offline", null, null, 1);

		controllerRepository.save(controller, "controllers");
		assertDoesNotThrow(() -> {
			controllerRepository.save(duplicateController, "controllers");
		});
	}

	@AfterEach
	public void tearDown() throws Exception {
		mongoOperations.dropCollection("controllers");
	}
}

package com.ngdesk.sidebar.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ngdesk.repositories.SidebarRepository;
import com.ngdesk.sidebar.RepositoryTest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RepositoryTest.class })
@SpringBootTest
public class SidebarRepositoryTest {
	


}

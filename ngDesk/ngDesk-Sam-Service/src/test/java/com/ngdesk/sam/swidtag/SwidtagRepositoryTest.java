package com.ngdesk.sam.swidtag;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.ngdesk.sam.RepositoryTest;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RepositoryTest.class })
@SpringBootTest
public class SwidtagRepositoryTest {

}

package com.ngdesk.repositories;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(repositoryBaseClass = CustomNgdeskRepositoryImpl.class)
public class Config {
}

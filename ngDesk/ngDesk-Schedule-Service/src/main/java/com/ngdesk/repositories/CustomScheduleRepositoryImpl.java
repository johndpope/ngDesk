package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.schedule.dao.Schedule;

@Repository
public class CustomScheduleRepositoryImpl implements CustomScheduleRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Schedule> findScheduleByName(String name, String collection) {
		return Optional.ofNullable(mongoOperations.findOne(new Query(Criteria.where("NAME").is(name)), Schedule.class));
	}

}
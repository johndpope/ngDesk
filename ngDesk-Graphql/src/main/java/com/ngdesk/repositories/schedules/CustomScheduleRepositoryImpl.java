package com.ngdesk.repositories.schedules;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.ngdesk.graphql.schedules.dao.Schedule;

@Repository
public class CustomScheduleRepositoryImpl implements CustomScheduleRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Schedule> findScheduleByName(String name, String collection) {
		return Optional.ofNullable(mongoOperations.findOne(new Query(Criteria.where("NAME").is(name)), Schedule.class));
	}

	@Override
	public List<Schedule> findAllSchedules(Pageable pageable, String collectionName) {
		Query query = new Query();
		query.with(pageable);
		return mongoOperations.find(query, Schedule.class, collectionName);
	}

	@Override
	public int getCount(String collectionName) {
		return (int) mongoOperations.count(new Query(), collectionName);
	}

	@Override
	public List<Schedule> findSchedulesByIds(List<String> scheduleIds, String collectionName) {
		Query query = new Query(Criteria.where("_id").in(scheduleIds));
		return mongoOperations.find(query, Schedule.class, collectionName);
	}
}
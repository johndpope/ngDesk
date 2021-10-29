package com.ngdesk.repositories.schedules;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.ngdesk.graphql.schedules.dao.Schedule;

public interface CustomScheduleRepository {

	public Optional<Schedule> findScheduleByName(String name, String collection);

	public List<Schedule> findAllSchedules(Pageable pageable, String collectionName);

	public int getCount(String collectionName);

	public List<Schedule> findSchedulesByIds(List<String> scheduleIds, String collectionName);
}

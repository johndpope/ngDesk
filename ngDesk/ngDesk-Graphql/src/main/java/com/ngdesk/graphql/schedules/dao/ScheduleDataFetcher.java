package com.ngdesk.graphql.schedules.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.schedules.ScheduleRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ScheduleDataFetcher implements DataFetcher<Schedule> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ScheduleRepository scheduleRepository;

	@Override
	public Schedule get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String scheduleId = environment.getArgument("scheduleId");

		Optional<Schedule> optionalSchedule = scheduleRepository.findById(scheduleId, "schedules_" + companyId);
		if (optionalSchedule.isPresent()) {
			return optionalSchedule.get();
		}

		return null;
	}

}

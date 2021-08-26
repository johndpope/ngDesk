package com.ngdesk.repositories;


import org.springframework.stereotype.Repository;

import com.ngdesk.company.dao.Tracker;

@Repository
public interface TrackerRepository extends CustomTrackerRepository, CustomNgdeskRepository<Tracker, String> {

}

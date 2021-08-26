package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.websocket.sam.dao.Controller;

@Repository
public interface ControllerRepository extends CustomControllerRepository, CustomNgdeskRepository<Controller, String> {

}

package com.ngdesk.repositories;

import org.springframework.stereotype.Repository;

import com.ngdesk.sam.controllers.dao.Controller;

@Repository
public interface ControllerRepository extends CustomControllerRepository, CustomNgdeskRepository<Controller, String> { 

}

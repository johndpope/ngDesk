package com.ngdesk.sam.controllers.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.ControllerRepository;

@Component
public class ControllerBeforeSaveListener extends AbstractMongoEventListener<Controller> {

	@Autowired
	ControllerRepository controllerRepository;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Controller> event) {
		Controller controller = event.getSource();

	}

}

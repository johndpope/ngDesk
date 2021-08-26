package com.ngdesk.websocket.dao;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.data.dao.PublishControllerInstruction;
import com.ngdesk.websocket.sam.dao.InstructionController;

@Component
@RabbitListener(queues = "notify-probe", concurrency = "5")
public class PublishControllerInstructionListener {

	@Autowired
	InstructionController instructionController;

	@RabbitHandler
	public void onController(PublishControllerInstruction publishController) {
		instructionController.publishInstructionToProbe(publishController.getSubdomain(),
				publishController.getControllerInstruction().getControllerId(),
				publishController.getControllerInstruction().getInstruction());

	}

}

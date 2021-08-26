package com.ngdesk.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.ngdesk.Global;
import com.ngdesk.SendMail;
import com.ngdesk.nodes.SendEmail;

@ControllerAdvice
@Controller
public class CustomizedExceptionHandler {
	private static final Logger logger = LogManager.getLogger(CustomizedExceptionHandler.class);

	@Autowired
	private Global global;
	
	@Autowired
	private Environment env;
	
	@Autowired
	SendMail sendMail;
	
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
		
		System.out.println("hits this");
		
		String url = ((ServletWebRequest)request).getRequest().getRequestURL().toString();
		
		String method = ((ServletWebRequest)request).getRequest().getMethod();
		
		String exception = ex.getCause().getClass().getSimpleName();
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		ex.printStackTrace(pw);
		String sStackTrace = sw.toString(); // stack trace as a string
		ex.printStackTrace();
		ExceptionResponse exceptionResponse = new ExceptionResponse(sStackTrace);
		
		String environment = env.getProperty("env");
		if (environment.equals("prd")) {
			String body = "URL: "+ url + "<br/>" +
			"TYPE: " + method + "<br/><br/><br/> <h1>Stack trace: </h1> <br/><br/>" + sStackTrace;
			
			sendMail.send("spencer@allbluesolutions.com", "support@ngdesk.com", "Internal Error: Stack Trace", body);
			sendMail.send("shashank@allbluesolutions.com", "support@ngdesk.com", "Internal Error: Stack Trace", body);

		}

		return new ResponseEntity("Internal error, please contact support" ,HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(UnauthorizedErrorException.class)
	public final ResponseEntity<Object> handleUnauthorizedException(UnauthorizedErrorException ex, WebRequest request) {

		String language = request.getAttribute("LANGUAGE", WebRequest.SCOPE_REQUEST).toString();
		if (!global.languages.contains(language)) {
			language = "en";
		}
		String messageKey = ex.getMessage();
		
		String errormessage = global.errorMsg(language, messageKey);
		ExceptionResponse exceptionResponse = new ExceptionResponse(errormessage);

		return new ResponseEntity(exceptionResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(InternalErrorException.class)
	public final ResponseEntity<Object> handleInternalErrorException(InternalErrorException ex, WebRequest request) {

		String language = request.getAttribute("LANGUAGE", WebRequest.SCOPE_REQUEST).toString();
		if (!global.languages.contains(language)) {
			language = "en";
		}
		String messageKey = ex.getMessage();

		String errormessage = global.errorMsg(language, messageKey);
		ExceptionResponse exceptionResponse = new ExceptionResponse(errormessage);

		return new ResponseEntity(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(MessageConversionException.class) 
	public final ResponseEntity<Object> handleErrorsOnStompMessageMapping(InternalErrorException ex, WebRequest request) {

		String language = request.getAttribute("LANGUAGE", WebRequest.SCOPE_REQUEST).toString();
		if (!global.languages.contains(language)) {
			language = "en";
		}
		String messageKey = ex.getMessage();
		
		System.out.println(ex.getMessage());
		ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getMessage());
		
		return new ResponseEntity(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}

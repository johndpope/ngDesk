package com.ngdesk.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ngdesk.Global;
import com.ngdesk.email.SendEmail;

@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private Global global;
	
	@Value("${email.host}")
	private String host;
	
	@Value("${env}")
	private String environment;
	

	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
		
		
		String url = ((ServletWebRequest)request).getRequest().getRequestURL().toString();
		
		String method = ((ServletWebRequest)request).getRequest().getMethod();
		
		String exception = ex.getCause().getClass().getSimpleName();
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		ex.printStackTrace(pw);
		String sStackTrace = sw.toString(); // stack trace as a string
		ex.printStackTrace();
		if (environment.equals("prd") && !sStackTrace.contains("ClientAbortException")) {
			
			String body = "URL: "+ url + "<br/>" +
			"TYPE: " + method + "<br/><br/><br/> <h1>Stack trace: </h1> <br/><br/>" + sStackTrace;
			
			SendEmail sendEmailToSpencer = new SendEmail("spencer@allbluesolutions.com",
					"support@ngdesk.com", "Internal Error: Stack Trace", body, host);
			sendEmailToSpencer.sendEmail();
			

			SendEmail sendEmailToShashank = new SendEmail(
					"shashank.shankaranand@allbluesolutions.com", "support@ngdesk.com",
					"Internal Error: Stack Trace", body , host);
			sendEmailToShashank.sendEmail();
		}

		return new ResponseEntity("Internal error, please contact support" ,HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ChannelNotFoundException.class)
	public final ResponseEntity<Object> handleChannelNotFoundException(ChannelNotFoundException ex,
			WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getMessage());
		return new ResponseEntity(exceptionResponse, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<Object> handleBadRequestException(BadRequestException ex, WebRequest request) {
		String language = request.getAttribute("LANGUAGE", WebRequest.SCOPE_REQUEST).toString();
		String messageKey = ex.getMessage();
		if (!global.languages.contains(language)) {
			language = "en";
		}
		
		if (messageKey.split("-").length > 1) {
			String message = global.errorMsg(language, messageKey.split("-")[1]);

			ExceptionResponse response = new ExceptionResponse(messageKey.split("-")[0] + " " + message);
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}

		String errormessage = global.errorMsg(language, messageKey);
		ExceptionResponse exceptionResponse = new ExceptionResponse(errormessage);

		return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InternalErrorException.class)
	public final ResponseEntity<Object> handleInternalErrorException(InternalErrorException ex, WebRequest request) {

		String language = request.getAttribute("LANGUAGE", WebRequest.SCOPE_REQUEST).toString();
		String messageKey = ex.getMessage();
		if (!global.languages.contains(language)) {
			language = "en";
		}

		String errormessage = global.errorMsg(language, messageKey);
		ExceptionResponse exceptionResponse = new ExceptionResponse(errormessage);

		return new ResponseEntity(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public final ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {

		String language = request.getAttribute("LANGUAGE", WebRequest.SCOPE_REQUEST).toString();
		String messageKey = ex.getMessage();
		if (!global.languages.contains(language)) {
			language = "en";
		}
		
		String errormessage = global.errorMsg(language, messageKey);
		ExceptionResponse exceptionResponse = new ExceptionResponse(errormessage);

		return new ResponseEntity(exceptionResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(ForbiddenException.class)
	public final ResponseEntity<Object> handleForbiddenException(ForbiddenException ex, WebRequest request) {

		ExceptionResponse exceptionResponse = null;
		String language = request.getAttribute("LANGUAGE", WebRequest.SCOPE_REQUEST).toString();
		String messageKey = ex.getMessage();
		if (!global.languages.contains(language)) {
			language = "en";
		}
		
		String errormessage = global.errorMsg(language, messageKey);
		exceptionResponse = new ExceptionResponse(errormessage);

		return new ResponseEntity(exceptionResponse, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler({ MethodArgumentTypeMismatchException.class })
	public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
			WebRequest request) {

		ExceptionResponse exceptionResponse = new ExceptionResponse("Argument type mismatch");

		return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {

		String language = request.getAttribute("LANGUAGE", WebRequest.SCOPE_REQUEST).toString();
		String messageKey = ex.getBindingResult().getFieldError().getDefaultMessage().toString();
		if (!global.languages.contains(language)) {
			language = "en";
		}
		
		String errormessage = global.errorMsg(language, messageKey);
		ExceptionResponse exceptionResponse = new ExceptionResponse(errormessage);
		
		return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
	}

}

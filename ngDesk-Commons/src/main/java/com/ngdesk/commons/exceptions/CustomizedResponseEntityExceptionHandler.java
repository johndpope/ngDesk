package com.ngdesk.commons.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Calendar;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.models.ErrorModel;
import com.twilio.Twilio;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@Autowired
	private Global global;

	@Value("${env}")
	private String environment;

	public String ACCOUNT_SID = "AC33f4c48eb80254d9949a76b1ef46ec01";
	public String AUTH_TOKEN = "544358a59ac3c0d9b15e75852883bffa";

	@Autowired
	SendMail sendMail;

	@Autowired
	AuthManager authManager;

	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {

		String url = ((ServletWebRequest) request).getRequest().getRequestURL().toString();
		String method = ((ServletWebRequest) request).getRequest().getMethod();

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		ex.printStackTrace(pw);
		String sStackTrace = sw.toString(); // stack trace as a string
		ex.printStackTrace();
		if (environment.equals("prd") && !sStackTrace.contains("Broken pipe")) {

			String body = "URL: " + url + "<br/>" + "TYPE: " + method + "<br/>" + "COMPANY: "
					+ authManager.getUserDetails().getCompanySubdomain()
					+ "<br/><br/><br/> <h1>Stack trace: </h1> <br/><br/>" + sStackTrace;
			String subject = "Internal Error: Stack Trace";

			sendMail.send("shashank@allbluesolutions.com", "error@ngdesk.com", subject, body);
			sendMail.send("spencer@allbluesolutions.com", "error@ngdesk.com", subject, body);
			sendMail.send("sharath.satish@allbluesolutions.com", "error@ngdesk.com", subject, body);
			sendMail.send("phaneendra.ar@subscribeit.com", "error@ngdesk.com", subject, body);
			sendMail.send("eanugula.charan@subscribeit.com", "error@ngdesk.com", subject, body);

//			if (body.contains("Command failed with error 112")) {
//				// Commented as per shashank
//				try {
//					
//					Calendar calendar = Calendar.getInstance();
//					Integer hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
//					
//					Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//					Call.creator(new PhoneNumber("+14692009202"), new PhoneNumber("+14695189179"),
//							new URI("https://prod.ngdesk.com/ngdesk-rest/ngdesk/TwilioCall?text=" + URLEncoder.encode(
//									"Command failed with error 112 (WriteConflict): 'WriteConflict error: this operation conflicted with another operation",
//									"utf-8")))
//							.setMethod(HttpMethod.GET).create();
//					
//					Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//					Call.creator(new PhoneNumber("+13126784446"), new PhoneNumber("+14695189179"),
//							new URI("https://prod.ngdesk.com/ngdesk-rest/ngdesk/TwilioCall?text=" + URLEncoder.encode(
//									"Command failed with error 112 (WriteConflict): 'WriteConflict error: this operation conflicted with another operation",
//									"utf-8")))
//							.setMethod(HttpMethod.GET).create();
//					
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				} catch (URISyntaxException e) {
//					e.printStackTrace();
//				}
//			}
		}
		return new ResponseEntity<Object>("Internal error, please contact support", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(BadRequestException.class)
	public final ResponseEntity<Object> handleBadRequestException(BadRequestException ex, WebRequest request) {
		ObjectMapper mapper = new ObjectMapper();
		boolean convertMessage = true;
		ErrorModel error = null;
		try {
			error = mapper.readValue(ex.getMessage(), ErrorModel.class);
			convertMessage = false;
		} catch (JsonMappingException e) {
		} catch (JsonProcessingException e) {
		}
		ExceptionResponse exceptionResponse;
		if (convertMessage) {
			exceptionResponse = new ExceptionResponse(
					Global.getvariableErrorMessage("en", ex.getMessage(), ex.getVariables()));
		} else {
			exceptionResponse = new ExceptionResponse(error.getError());
		}
		return new ResponseEntity<Object>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InternalErrorException.class)
	public final ResponseEntity<Object> handleInternalErrorException(InternalErrorException ex, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(global.errorMsg("en", ex.getMessage()));
		return new ResponseEntity<Object>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public final ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(global.errorMsg("en", ex.getMessage()));
		return new ResponseEntity<Object>(exceptionResponse, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(ForbiddenException.class)
	public final ResponseEntity<Object> handleForbiddenException(ForbiddenException ex, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(global.errorMsg("en", ex.getMessage()));
		return new ResponseEntity<Object>(exceptionResponse, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler({ MethodArgumentTypeMismatchException.class })
	public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
			WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse("Argument type mismatch");
		return new ResponseEntity<Object>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String msgKey = ex.getBindingResult().getFieldError().getDefaultMessage().toString();
		String variableMessage = null;
		String annotation = ex.getBindingResult().getFieldError().getCode();
		if (annotation.equals("CustomNotEmpty") || annotation.equals("CustomNotNull")
				|| annotation.equals("CustomTimeZoneValidation")) {
			variableMessage = Global.getvariableErrorMessage("en", msgKey,
					((String[]) ex.getBindingResult().getFieldError().getArguments()[1]));
		} else {
			variableMessage = Global.errorMsg("en", msgKey);
		}

		ExceptionResponse exceptionResponse = new ExceptionResponse(variableMessage);
		return new ResponseEntity<Object>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NotFoundException.class)
	public final ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
		ExceptionResponse exceptionResponse = new ExceptionResponse(
				Global.getvariableErrorMessage("en", ex.getMessage(), ex.getVariables()));
		return new ResponseEntity<Object>(exceptionResponse, HttpStatus.NOT_FOUND);
	}

}

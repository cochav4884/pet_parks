package pet.park.controller.error;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalControllerErrorHandler {
	private enum LogStatus {
		STACK_TRACE, MESSAGE_ONLY
	}

	@Data
	private class ExceptionMessage {
		private String message;
		private String statusReason;
		private int statusCode;
		private String timestamp;
		private String uri;
	} // end INNER CLASS ExceptionMessage

	// WebRequest is used for Spring Boot to inject an object to help us get the URI

	@ExceptionHandler(UnsupportedOperationException.class)
	@ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED) // 405 status
	public ExceptionMessage handleUnsupportedOperationException(UnsupportedOperationException ex,
			WebRequest webRequest) {
		return buildExceptionMessage(ex, HttpStatus.METHOD_NOT_ALLOWED, webRequest, LogStatus.MESSAGE_ONLY);
	} // end handleUnsupportedOperationException

	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(code = HttpStatus.NOT_FOUND) // 404 status
	public ExceptionMessage handleNoSuchElementException(NoSuchElementException ex, WebRequest webRequest) {
		return buildExceptionMessage(ex, HttpStatus.NOT_FOUND, webRequest, LogStatus.MESSAGE_ONLY);
	} // end handleNoSuchElementException

	@ExceptionHandler(DuplicateKeyException.class)
	@ResponseStatus(code = HttpStatus.CONFLICT) // 409 status
	public ExceptionMessage handleDuplicateKeyException(DuplicateKeyException ex, WebRequest webRequest) {
		return buildExceptionMessage(ex, HttpStatus.CONFLICT, webRequest, LogStatus.MESSAGE_ONLY);
	}
	
	@ExceptionHandler(IllegalStateException.class)
	@ResponseStatus(code = HttpStatus.BAD_REQUEST) // 400 status
	public ExceptionMessage handleIllegalStateException(IllegalStateException ex, WebRequest webRequest) {
		return buildExceptionMessage(ex, HttpStatus.BAD_REQUEST, webRequest, LogStatus.MESSAGE_ONLY);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR) // 500 status
	public ExceptionMessage handleException(Exception ex, WebRequest webRequest) {
		return buildExceptionMessage(ex, HttpStatus.INTERNAL_SERVER_ERROR, webRequest, LogStatus.STACK_TRACE);
	} // end handleException
	
	private ExceptionMessage buildExceptionMessage(Exception ex, HttpStatus status, WebRequest webRequest,
			LogStatus logStatus) {
		String message = ex.toString();
		String statusReason = status.getReasonPhrase();
		int statusCode = status.value();
		String uri = null;
		String timestamp = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);

		// Java will cast webRequest to ServletWebRequest if it is an instance of
		// ServletWebRequest
		// and give us the variable swr
		if (webRequest instanceof ServletWebRequest swr) {
			uri = swr.getRequest().getRequestURI();
		} // end IF

		if (logStatus == LogStatus.MESSAGE_ONLY) {
			log.error("Exception: {}", ex.toString());
		} else {
			log.error("Exception: ", ex);
		}

		ExceptionMessage excMsg = new ExceptionMessage();

		excMsg.setMessage(message);
		excMsg.setStatusCode(statusCode);
		excMsg.setStatusReason(statusReason);
		excMsg.setTimestamp(timestamp);
		excMsg.setUri(uri);

		return excMsg;
	} // end buildExceptionMessage

} // end CLASS
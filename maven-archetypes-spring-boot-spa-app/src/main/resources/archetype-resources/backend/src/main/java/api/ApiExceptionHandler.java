package ${groupId}.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(basePackageClasses = ApiExceptionHandler.class)
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final String UNAUTHORIZED_MESSAGE = "You must provide valid credentials to access this resource.";
	private static final String FORBIDDEN_MESSAGE = "You are not authorized to access this resource.";

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Object> handleAuthenticationException(Exception ex, WebRequest request) {
		HttpStatus status = HttpStatus.UNAUTHORIZED;
		ProblemDetail body = createProblemDetail(ex, status, UNAUTHORIZED_MESSAGE, null, null, request);
		return handleExceptionInternal(ex, body, null, status, request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
		HttpStatus status = HttpStatus.FORBIDDEN;
		ProblemDetail body = createProblemDetail(ex, status, FORBIDDEN_MESSAGE, null, null, request);
		return handleExceptionInternal(ex, body, null, status, request);
	}
}

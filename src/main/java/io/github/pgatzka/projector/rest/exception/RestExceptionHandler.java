package io.github.pgatzka.projector.rest.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Request body validation failed."
        );
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
            errors.put(err.getField(), err.getDefaultMessage())
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Request body could not be read."
        );
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String field = ife.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining("."));
            String reason = describeInvalidFormat(ife);
            if (!field.isEmpty()) {
                Map<String, String> errors = new LinkedHashMap<>();
                errors.put(field, reason);
                problem.setProperty("errors", errors);
            } else {
                problem.setDetail(reason);
            }
        }
        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Request parameter type mismatch."
        );
        String name = ex.getName();
        Class<?> required = ex.getRequiredType();
        String reason = required != null && required.isEnum()
            ? "Must be one of: " + enumValues(required) + "."
            : "Invalid value for parameter.";
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put(name, reason);
        problem.setProperty("errors", errors);
        return problem;
    }

    private static String describeInvalidFormat(InvalidFormatException ife) {
        Class<?> targetType = ife.getTargetType();
        if (targetType != null && targetType.isEnum()) {
            return "Must be one of: " + enumValues(targetType) + ".";
        }
        return "Invalid value.";
    }

    private static String enumValues(Class<?> enumType) {
        Object[] constants = enumType.getEnumConstants();
        if (constants == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < constants.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(((Enum<?>) constants[i]).name());
        }
        return sb.toString();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

    @ExceptionHandler(SetupAlreadyCompletedException.class)
    public ProblemDetail handleSetupCompleted(SetupAlreadyCompletedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailExists(EmailAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ProblemDetail handleProjectNotFound(ProjectNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IssueNotFoundException.class)
    public ProblemDetail handleIssueNotFound(IssueNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProjectKeyTakenException.class)
    public ProblemDetail handleProjectKeyTaken(ProjectKeyTakenException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LabelNotFoundException.class)
    public ProblemDetail handleLabelNotFound(LabelNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LabelNameTakenException.class)
    public ProblemDetail handleLabelNameTaken(LabelNameTakenException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LabelNotInProjectException.class)
    public ProblemDetail handleLabelNotInProject(LabelNotInProjectException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ProblemDetail handleCommentNotFound(CommentNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}

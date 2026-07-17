package be.hogent.baristajob2026.advice;

import be.hogent.baristajob2026.controller.ApiController;
import be.hogent.baristajob2026.dto.response.ErrorResponse;
import be.hogent.baristajob2026.exception.BaristaNotFoundException;
import be.hogent.baristajob2026.exception.VestigingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

// enkel voor ApiController; de MVC-controllers gebruiken GlobalExceptionAdvice (redirect i.p.v. JSON)
@RestControllerAdvice(assignableTypes = ApiController.class)
public class ApiExceptionAdvice {
    @ExceptionHandler(BaristaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleBaristaNotFound(BaristaNotFoundException ex) {
        return new ErrorResponse(404, ex.getMessage(), LocalDateTime.now().toString());
    }

    @ExceptionHandler(VestigingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleVestigingNotFound(VestigingNotFoundException ex) {
        return new ErrorResponse(404, ex.getMessage(), LocalDateTime.now().toString());
    }
}

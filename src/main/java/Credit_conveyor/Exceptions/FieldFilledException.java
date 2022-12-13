package Credit_conveyor.Exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class FieldFilledException {

    @ExceptionHandler
    public ErrorInformation handle(IncorrectFieldOrRefusedException exception) {
        log.error(exception.getMessage(), exception.getNumberError());
        return new ErrorInformation(exception.getMessage(), exception.getNumberError());
    }
    @ExceptionHandler
    public ErrorInformation handle(HttpMessageNotReadableException exception) {
        log.error(exception.getMessage(), 1500);
        return new ErrorInformation("The field is filled in incorrectly", 1500);
    }
}

@Getter
class ErrorInformation {
    private final HttpStatus status = HttpStatus.BAD_REQUEST;
    private final String message;
    private final int numberError;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime time = LocalDateTime.now();

    public ErrorInformation(String message, int error) {
        this.message = message;
        this.numberError = error;
    }
}
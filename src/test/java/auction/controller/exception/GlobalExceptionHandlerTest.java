// checked by qwen
package auction.controller.exception;

import auction.exception.ErrorResponse;
import auction.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    public void testHandleRuntimeException() {
        // Arrange
        RuntimeException ex = new RuntimeException("Something went wrong");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong", response.getBody().getMessage());
    }

    @Test
    public void testHandleIllegalArgumentException() { // методы можно писать test можно не писать
        // Arrange
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(ex);

        // Assert
        assertEquals(400, response.getStatusCodeValue()); // использовать константы
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    public void testHandleAccessDeniedException() {
        // Arrange
        org.springframework.security.access.AccessDeniedException ex = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccessDeniedException(ex);

        // Assert
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied", response.getBody().getMessage());
    }


}
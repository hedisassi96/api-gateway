package org.example;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
public class StorageServiceException extends Exception{
    @Getter
    private HttpStatus statusCode;

    @Getter
    private String errorMessage;
}

package com.projeto.raizesnordeste.presentation.exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
public class StandardError {

    private String requestId;
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;

    @Builder.Default
    private List<String> details = new ArrayList<>();

}

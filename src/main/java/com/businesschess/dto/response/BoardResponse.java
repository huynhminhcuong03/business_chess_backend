package com.businesschess.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BoardResponse {

    private Long id;

    private String name;

    private LocalDateTime createdAt;
}
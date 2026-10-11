package com.medicurex.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateMessageRequest {

    @NotBlank(message = "Message text is required")
    @Size(max = 2000, message = "Message must be at most 2000 characters")
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

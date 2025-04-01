package com.gptapi.polling.dto;

import lombok.Data;

@Data
public class StoryResponse {
    private String jobId;
    private String status;
    private String story;
}
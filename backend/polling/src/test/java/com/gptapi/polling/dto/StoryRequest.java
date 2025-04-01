package com.gptapi.polling.dto;

import lombok.Data;

@Data
public class StoryRequest {
    private String storyTitle;
    private String storyIdea;
    private String characterLength;
    private String language;
}
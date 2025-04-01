package com.gptapi.polling.controller;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gptapi.polling.dto.StoryRequest;
import com.gptapi.polling.dto.StoryResponse;
import com.gptapi.polling.service.StoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @Operation(summary = "Generate a story", description = "Starts the story generation process and returns a job ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job started successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/generate-story")
    public StoryResponse generateStory(@RequestBody StoryRequest request) {
        String jobId = storyService.startStoryGeneration(request);
        StoryResponse response = new StoryResponse();
        response.setJobId(jobId);
        response.setStatus("PENDING");
        return response;
    }

    @Operation(summary = "Check story generation status", description = "Returns the current status of the story generation job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/story-status/{jobId}")
    public StoryResponse getStoryStatus(@PathVariable String jobId) {
        StoryService.StoryJob job = storyService.getJobStatus(jobId);
        if (job == null) {
            StoryResponse response = new StoryResponse();
            response.setStatus("NOT_FOUND");
            return response;
        }

        StoryResponse response = new StoryResponse();
        response.setJobId(job.getJobId());
        response.setStatus(job.getStatus());
        response.setStory(job.getStory());
        return response;
    }
}
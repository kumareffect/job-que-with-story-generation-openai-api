package com.gptapi.polling.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gptapi.polling.dto.StoryRequest;

@Service
public class StoryService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final GptApi gptApi;
    private final Map<String, StoryJob> jobQueue = new HashMap<>();

    public StoryService(GptApi gptApi) {
        this.gptApi = gptApi;
    }

    public String startStoryGeneration(StoryRequest request) {
        String jobId = UUID.randomUUID().toString();
        StoryJob job = new StoryJob(jobId, request);
        jobQueue.put(jobId, job);

        // Start the job in a separate thread to simulate a 3-minute process
        new Thread(() -> processStoryGeneration(job)).start();

        return jobId;
    }

    public StoryJob getJobStatus(String jobId) {
        return jobQueue.get(jobId);
    }

    private void processStoryGeneration(StoryJob job) {
        try {
            job.setStatus("Processing: Initializing...");
            Thread.sleep(10000); // 10 seconds

            job.setStatus("Processing: Generating story idea...");
            Thread.sleep(10000); // 10 seconds

            // Construct the prompt for ChatGPT
            String prompt = String.format(
                    "Write a story titled '%s' based on the idea: '%s'. The story should be %s in length and written in %s.",
                    job.getRequest().getStoryTitle(),
                    job.getRequest().getStoryIdea(),
                    job.getRequest().getCharacterLength(),
                    job.getRequest().getLanguage()
            );

            job.setStatus("Processing: Calling AI to generate story...");
            Thread.sleep(10000); // 10 seconds

            // Call ChatGPT API
            String story = gptApi.generateStory(prompt, apiKey);
            if (story == null) {
                job.setStatus("FAILED");
                return;
            }

            // Simulate remaining time (total 3 minutes = 180 seconds, we've used 30 seconds so far)
            for (int i = 0; i < 15; i++) { // 15 intervals of 10 seconds = 150 seconds
                job.setStatus("Processing: Refining story... (" + (i + 1) + "/15)");
                Thread.sleep(10000); // 10 seconds
            }

            job.setStory(story);
            job.setStatus("COMPLETED");
        } catch (InterruptedException e) {
            job.setStatus("FAILED");
            Thread.currentThread().interrupt();
        }
    }

    public static class StoryJob {
        private final String jobId;
        private final StoryRequest request;
        private String status;
        private String story;

        public StoryJob(String jobId, StoryRequest request) {
            this.jobId = jobId;
            this.request = request;
            this.status = "PENDING";
        }

        public String getJobId() {
            return jobId;
        }

        public StoryRequest getRequest() {
            return request;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStory() {
            return story;
        }

        public void setStory(String story) {
            this.story = story;
        }
    }
}
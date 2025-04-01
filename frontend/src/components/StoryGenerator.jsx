import React, { useState } from 'react';
import axios from 'axios';
import './StoryGenerator.css';

const StoryGenerator = () => {
  const [storyTitle, setStoryTitle] = useState('');
  const [storyIdea, setStoryIdea] = useState('');
  const [characterLength, setCharacterLength] = useState('Medium (1000-2000 chars)');
  const [language, setLanguage] = useState('English');
  const [status, setStatus] = useState('');
  const [story, setStory] = useState('');

  const handleGenerateStory = async () => {
    setStatus('Processing... This will take around 3 minutes.');
    try {
      const response = await axios.post('http://localhost:8080/api/generate-story', {
        storyTitle,
        storyIdea,
        characterLength,
        language,
      });

      const jobId = response.data.jobId;

      // Polling every 10 seconds to check the status
      const interval = setInterval(async () => {
        const statusResponse = await axios.get(`http://localhost:8080/api/story-status/${jobId}`);
        const { status, story } = statusResponse.data;

        setStatus(status);
        if (status === 'COMPLETED') {
          setStory(story);
          clearInterval(interval);
        } else if (status === 'FAILED') {
          setStatus('Failed to generate story.');
          clearInterval(interval);
        }
      }, 10000); // Check every 10 seconds
    } catch (error) {
      setStatus('Error: Could not start story generation.');
    }
  };

  return (
    <div className="story-generator-container">
      <h2>Start Creating Now</h2>

      <div className="form-group">
        <label>Story Title</label>
        <input
          type="text"
          placeholder="Enter a title for your story"
          value={storyTitle}
          onChange={(e) => setStoryTitle(e.target.value)}
        />
      </div>

      <div className="form-group">
        <label>Your Story Idea</label>
        <textarea
          placeholder="Describe your story idea or concept..."
          value={storyIdea}
          onChange={(e) => setStoryIdea(e.target.value)}
        />
      </div>

      <div className="form-row">
        <div className="form-group">
          <label>Character Length</label>
          <select value={characterLength} onChange={(e) => setCharacterLength(e.target.value)}>
            <option>Short (200-300 chars)</option>
            <option>Medium (300-500 chars)</option>
            <option>Long (500-1000 chars)</option>
          </select>
        </div>

        <div className="form-group">
          <label>Language</label>
          <select value={language} onChange={(e) => setLanguage(e.target.value)}>
            <option>English</option>
            <option>Chinese</option>
            <option>Spanish</option>
          </select>
        </div>
      </div>

      <div className="button-group">
        <button className="generate-btn" onClick={handleGenerateStory}>
          Generate Story
        </button>
      </div>

      {status && <p className="status">{status}</p>}
      {story && (
        <div className="story-result">
          <h3>Generated Story:</h3>
          <p>{story}</p>
        </div>
      )}
    </div>
  );
};

export default StoryGenerator;
package com.audio.AudioTranscriber;

import org.springframework.ai.audio.transcription.*;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

import java.io.*;

@RestController
@RequestMapping("/api")
public class TranscriptionController {

    private final OpenAiAudioTranscriptionModel transcriptionModel;


    public TranscriptionController(@Value("${spring.ai.openai.api-key}") String openaiAPIKey) {

       // System.out.println("Using API Key: " + openaiAPIKey);
        OpenAiAudioApi openAiAudioApi
                =new OpenAiAudioApi(openaiAPIKey);
        this.transcriptionModel = new OpenAiAudioTranscriptionModel(openAiAudioApi);
    }

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribeAudio(@RequestParam("audioFile") MultipartFile audioFile) throws IOException {

        File tempFile = File.createTempFile("audio",".wav");
        audioFile.transferTo(tempFile);

        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .withLanguage("en")
                .withTemperature(0f)
                .build();

        FileSystemResource audioFileWrappedInResource = new FileSystemResource(tempFile);

        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFileWrappedInResource, transcriptionOptions);
        AudioTranscriptionResponse response = transcriptionModel.call(transcriptionRequest);

        tempFile.delete();

        return new ResponseEntity<>(response.getResult().getOutput(),HttpStatus.OK);
    }

}

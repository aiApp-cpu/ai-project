package com.example.aiassistant.app.controller;


import com.example.aiassistant.app.model.NotesRequest;
import com.example.aiassistant.app.model.NotesResponse;
import com.example.aiassistant.app.service.AiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class NotesController {

    private final AiService aiService;

    public NotesController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/notes")
    public NotesResponse getNotes(@RequestBody NotesRequest request) {
        return aiService.generateNotes(request);
    }
}
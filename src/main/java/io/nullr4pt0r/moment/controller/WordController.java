package io.nullr4pt0r.moment.controller;

import io.nullr4pt0r.moment.Service.WordService;
import io.nullr4pt0r.moment.dto.response.WordResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
public class WordController {

    @Autowired
    private  WordService wordService;

    @GetMapping("/hello")
    public ResponseEntity<String> getWord() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping()
    public ResponseEntity<WordResponse> getRandomWord(
            HttpServletRequest request,
            @RequestHeader(name = "sessId") String userSessionId,
            @RequestParam(name = "location", required = false) String location,
                                           @RequestParam(name = "lang", required = false, defaultValue = "English") String lang){
        WordResponse wordResponse = wordService.fetchWord(userSessionId, lang);
        return ResponseEntity.ok(wordResponse);
    }
}

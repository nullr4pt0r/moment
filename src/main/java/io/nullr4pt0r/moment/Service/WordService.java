package io.nullr4pt0r.moment.Service;

import io.nullr4pt0r.moment.dto.response.WordResponse;
import io.nullr4pt0r.moment.model.Words;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordService {

    private final WordFactory wordFactory;

    private final LanguageService languageService;

    public WordResponse fetchWord(String userSessionId, String language) {
        if(!languageService.isValidLanguage(language)){
            language = "english";
        }
        //fetch the strategy  -> currently support only mongodb strategy
        WordFetchStrategy strategy = wordFactory.getRedisStrategy();
        //find the word
        Words resultWord = strategy.fetchWord(userSessionId, language);
        if(resultWord == null){
            //go to mongodb
            strategy = wordFactory.getMongoFetchStrategy();
            resultWord = strategy.fetchWord(userSessionId, language);
        }
        log.info("Fetched word : "+resultWord.getId());
        return new WordResponse(resultWord.getWord(), resultWord.getLanguage(), resultWord.getMeanings(), resultWord.getPhonetics(), resultWord.getEnglishTranslation(), resultWord.getRemarks());
    }

}

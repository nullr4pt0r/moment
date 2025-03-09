package io.nullr4pt0r.moment.Service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LanguageService {

    private Set<String> languageSet = Set.of("english","tamil","hindi","french","dutch"
    ,"portuguese","malayalam","telugu");

    public boolean isValidLanguage(String lang){
        return languageSet.contains(lang);
    }
}

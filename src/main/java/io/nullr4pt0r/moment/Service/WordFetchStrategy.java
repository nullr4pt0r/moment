package io.nullr4pt0r.moment.Service;

import io.nullr4pt0r.moment.model.Words;

public interface WordFetchStrategy {
    Words fetchWord(String userSessionId, String language);
}

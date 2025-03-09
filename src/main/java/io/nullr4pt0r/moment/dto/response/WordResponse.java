package io.nullr4pt0r.moment.dto.response;

import java.util.List;

public record WordResponse(String word, String language, List<String> meanings, String englishTranslation, List<String> remarks) {
}

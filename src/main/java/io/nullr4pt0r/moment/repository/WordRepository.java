package io.nullr4pt0r.moment.repository;

import io.nullr4pt0r.moment.model.Words;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WordRepository extends MongoRepository<Words, String> {

    /*
    It fetch the word matched by language and random 1 word.
     */
    @Aggregation(pipeline = {
            "{ '$match' : { 'language' : ?0  } }",
            "{ '$sample' : { 'size' : 1 } }"
    })
    Words findRandomWordByLanguage(String language);
}

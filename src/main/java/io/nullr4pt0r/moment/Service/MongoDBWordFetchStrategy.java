package io.nullr4pt0r.moment.Service;

import io.nullr4pt0r.moment.model.Words;
import io.nullr4pt0r.moment.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MongoDBWordFetchStrategy implements WordFetchStrategy{

    private final WordRepository wordRepository;

    private final MongoTemplate mongoTemplate;

    private final RedisTemplate redisTemplate;


    @Override
    public Words fetchWord(String userSessionId, String language) {

        //get the seen word id list from redis
        String userKey = userSessionId+":"+language;
        Set<String> seenWords = redisTemplate.opsForSet().members(userKey);

        MatchOperation matchOperation = Aggregation.match(Criteria.where("language").is(language));
        if(seenWords != null && !seenWords.isEmpty()){
            matchOperation = Aggregation.match(Criteria.where("language").is(language).and("_id").nin(seenWords));
        }
        Aggregation aggregation =  Aggregation.newAggregation(
                matchOperation,
                Aggregation.sample(1) // Random selection
        );
        System.out.println(aggregation.toString());
        System.out.println(mongoTemplate.getDb().getName());
        Words resultWord = mongoTemplate.aggregate(aggregation, "words", Words.class).getUniqueMappedResult();

        if (resultWord == null) {
            // Reset the seen words list (optional: clear the Redis set)
            redisTemplate.opsForZSet().removeRange(userKey, 0, -1); // Clear the ZSet
            seenWords = Collections.emptySet(); // Reset seenWords to empty

            // Retry fetching a word (without excluding any)
            matchOperation = Aggregation.match(Criteria.where("language").is(language));
            aggregation = Aggregation.newAggregation(
                    matchOperation,
                    Aggregation.sample(1)
            );
            resultWord = mongoTemplate.aggregate(aggregation, "words", Words.class).getUniqueMappedResult();
        }


        return resultWord;
    }
}

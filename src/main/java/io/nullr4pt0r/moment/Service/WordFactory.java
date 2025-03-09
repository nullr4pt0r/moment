package io.nullr4pt0r.moment.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WordFactory {

    private final MongoDBWordFetchStrategy mongoDBWordFetchStrategy;
    private final RedisWordFetchStrategy redisWordFetchStrategy;

    WordFetchStrategy getRedisStrategy(){
        return redisWordFetchStrategy;
    }

    WordFetchStrategy getMongoFetchStrategy(){
        return mongoDBWordFetchStrategy;
    }
}

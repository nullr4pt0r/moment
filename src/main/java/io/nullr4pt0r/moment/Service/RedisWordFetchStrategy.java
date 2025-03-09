package io.nullr4pt0r.moment.Service;

import io.nullr4pt0r.moment.model.Words;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service

public class RedisWordFetchStrategy implements WordFetchStrategy {
    private static final int CACHE_SIZE = 3;
    private static final long CACHE_TTL_HOURS = 1;

    private static final String UPDATE_SCRIPT =
            "local userKey = KEYS[1]\n" +
                    "local langKey = KEYS[2]\n" +
                    "local wordId = ARGV[1]\n" +
                    "local currentTime = tonumber(ARGV[2])\n" +
                    "local cacheSize = tonumber(ARGV[3])\n" +
                    "\n" +
                    "redis.call('ZADD', langKey, currentTime, wordId)\n" +
                    "local excess = redis.call('ZCARD', langKey) - cacheSize\n" +
                    "if excess > 0 then\n" +
                    "    local evictedWordIds = redis.call('ZRANGE', langKey, 0, excess - 1)\n" +
                    "    redis.call('ZREMRANGEBYRANK', langKey, 0, excess - 1)\n" +
                    "    if #evictedWordIds > 0 then\n" +
                    "        redis.call('ZREM', userKey, unpack(evictedWordIds))\n" +
                    "    end\n" +
                    "    for _, evictedWordId in ipairs(evictedWordIds) do\n" +
                    "        redis.call('DEL', 'word:' .. evictedWordId)\n" +
                    "    end\n" +
                    "end\n" +
                    "redis.call('ZADD', userKey, currentTime, wordId)\n" +
                    "redis.call('EXPIRE', userKey, 3600)\n" +
                    "return 1";

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoDBWordFetchStrategy mongoDBWordFetchStrategy;
    private final RedisScript<Long> updateScript;

    public RedisWordFetchStrategy(RedisTemplate<String, Object> redisTemplate,
                                  MongoDBWordFetchStrategy mongoDBWordFetchStrategy) {
        this.redisTemplate = redisTemplate;
        this.mongoDBWordFetchStrategy = mongoDBWordFetchStrategy;
        this.updateScript = RedisScript.of(UPDATE_SCRIPT, Long.class);
    }

    @Override
    public Words fetchWord(String userSessionId, String language) {
        String userKey = userSessionId + ":" + language;
        String langKey = language + ":wordIds";

        List<Object> results = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.opsForZSet().range(langKey, 0, -1);
                operations.opsForZSet().range(userKey, 0, -1);
                return null;
            }
        });

        Set<String> allWords = results.get(0) != null ? (Set<String>) results.get(0) : Set.of();
        Set<String> seenWords = results.get(1) != null ? (Set<String>) results.get(1) : Set.of();

        Set<String> unseenWords = new HashSet<>(allWords);
        unseenWords.removeAll(seenWords);

        if (unseenWords.isEmpty()) {
            return handleCacheMiss(userSessionId, language);
        }

        List<String> unseenWordList = new ArrayList<>(unseenWords);
        String randomWordId = unseenWordList.get(ThreadLocalRandom.current().nextInt(unseenWordList.size()));
        Words word = fetchWordFromRedis(randomWordId);

        if (word == null) {
            return handleCacheMiss(userSessionId, language);
        }

        updateCache(userSessionId, language, randomWordId);
        return word;
    }

    private Words handleCacheMiss(String sessionId, String language) {
        Words word = fetchRandomWordFromMongoDB(language);
        if (word != null) {
            updateCache(sessionId, language, word.getId());
        }
        return word;
    }

    private Words fetchWordFromRedis(String wordId) {
        return (Words) redisTemplate.opsForValue().get("word:" + wordId);
    }

    private Words fetchRandomWordFromMongoDB(String language) {
        Words word = mongoDBWordFetchStrategy.fetchWord(null, language);
        if (word != null) {
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(RedisOperations operations) {
                    operations.opsForZSet().add(language + ":wordIds", word.getId(), System.currentTimeMillis());
                    operations.opsForValue().set("word:" + word.getId(), word);
                    return null;
                }
            });
            log.debug("Refreshed cache from MongoDB: {}", word.getId());
        }
        return word;
    }

    @Async
    public void updateCache(String sessionId, String language, String wordId) {
        String userKey = sessionId + ":" + language;
        String langKey = language + ":wordIds";

        redisTemplate.execute(
                updateScript,
                List.of(userKey, langKey), // KEYS[1] and KEYS[2]
                wordId,                    // ARGV[1]
                System.currentTimeMillis(), // ARGV[2]
                CACHE_SIZE                 // ARGV[3] (cacheSize)
        );

    }

    // Pre-load and verify script during startup
    @PostConstruct
    public void validateScript() {
        try {
            redisTemplate.getConnectionFactory().getConnection().scriptExists(
                    updateScript.getSha1()
            );
        } catch (Exception e) {
            log.error("Lua script validation failed", e);
            throw new IllegalStateException("Invalid Lua script");
        }
    }
}
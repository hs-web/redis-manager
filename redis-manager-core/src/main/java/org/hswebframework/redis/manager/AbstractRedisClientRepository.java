package org.hswebframework.redis.manager;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.redis.manager.codec.CodecClassLoader;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.config.Config;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractRedisClientRepository implements RedisClientRepository {

    protected Map<String, Cache> clientMap = new ConcurrentHashMap<>();

    private ClassLoader classLoader;

    @SneakyThrows
    public AbstractRedisClientRepository() {
        File file = new File("./lib");
        if (!file.exists()) {
            file.mkdir();
        }
        classLoader = new CodecClassLoader(file.toURI().toURL());
    }

    @Override
    public RedissonClient getRedissonClient(String id) {
        RedisClient client = findById(id);
        Objects.requireNonNull(client, "客户端不存在");

        Cache cache = clientMap.get(id);
        if (cache == null || cache.hash != client.hashCode()) {
            if (cache != null) {
                try {
                    cache.client.shutdown();
                } catch (Exception e) {
                    //ignore errors
                    log.error("停止redis客户端失败", e);
                }
            }
            cache = createCache(client);
            clientMap.put(id, cache);
        }
        return cache.client;
    }

    protected Cache createCache(RedisClient client) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(client.getAddress())
                .setDatabase(client.getDatabase());

        Cache cache = new Cache();
        cache.client = Redisson.create(config);
        cache.init(client);
        return cache;
    }

    @Override
    public Codec getCodec(String clientId, String key) {
        return Optional.ofNullable(clientMap.get(clientId))
                .map(client -> client.getCodec(key))
                .orElseThrow(NullPointerException::new);
    }

    class Cache {
        private long hash;

        private Map<String, Codec> codecMap = new HashMap<>();

        private RedissonClient client;

        private Codec defaultCodec = createCodec(new RedisClient.CodecConfig());

        public void init(RedisClient redisClient) {
            this.hash = redisClient.hashCode();
            if (redisClient.getEncodeDecodeConfig() != null) {
                for (Map.Entry<String, RedisClient.CodecConfig> entry : redisClient.getEncodeDecodeConfig().entrySet()) {
                    codecMap.put(entry.getKey(), createCodec(entry.getValue()));
                }
            }
        }

        private Codec getCodec(String key) {
            return codecMap.getOrDefault(key, defaultCodec);
        }
    }

    public Codec createCodec(RedisClient.CodecConfig config) {
        Codec valueCodec = config.getValueCodec().createCodec(classLoader);
        Codec keyCodec = config.getKeyCodec().createCodec(classLoader);
        return new Codec() {
            @Override
            public Decoder<Object> getMapValueDecoder() {
                return valueCodec.getMapValueDecoder();
            }

            @Override
            public Encoder getMapValueEncoder() {
                return valueCodec.getValueEncoder();
            }

            @Override
            public Decoder<Object> getMapKeyDecoder() {
                return keyCodec.getMapKeyDecoder();
            }

            @Override
            public Encoder getMapKeyEncoder() {
                return keyCodec.getMapKeyEncoder();
            }

            @Override
            public Decoder<Object> getValueDecoder() {
                return valueCodec.getValueDecoder();
            }

            @Override
            public Encoder getValueEncoder() {
                return valueCodec.getValueEncoder();
            }

            @Override
            public ClassLoader getClassLoader() {
                return classLoader;
            }
        };
    }


}

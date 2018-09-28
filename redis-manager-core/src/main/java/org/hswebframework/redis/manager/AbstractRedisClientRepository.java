package org.hswebframework.redis.manager;

import io.vavr.Lazy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.redis.manager.codec.CodecClassLoader;
import org.redisson.Redisson;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.config.Config;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractRedisClientRepository implements RedisClientRepository {

    protected Map<String, Cache> clientCache = new ConcurrentHashMap<>();

    protected ClassLoader classLoader;

    @SneakyThrows
    public AbstractRedisClientRepository() {
        File file = new File(System.getProperty("redis.manager.codec.lib.dir", "./lib"));
        if (!file.exists()) {
            file.mkdir();
        }
        classLoader = new CodecClassLoader(file);
    }

    @Override
    public RedissonClient getRedissonClient(String id, int database) {
        return getCache(id).getClient(database);
    }

    protected void updateCodec(String clientId, Map<String, RedisClient.CodecConfig> configMap) {
        if (clientCache.get(clientId) != null) {
            getCache(clientId).reloadCodec(configMap);
        }
    }

    protected Cache getCache(String id) {
        RedisClient client = findById(id);
        Objects.requireNonNull(client, "客户端不存在");
        Cache cache = clientCache.get(id);
        if (cache == null || !cache.clientConf.equals(client)) {
            if (cache != null) {
                cache.close();
            }
            cache = createCache(client);
            clientCache.put(id, cache);
        }
        return cache;
    }

    protected Cache createCache(RedisClient client) {
        Cache cache = new Cache();
        cache.init(client);
        return cache;
    }

    @Override
    public Codec getCodec(String clientId, String key) {
        return Optional.ofNullable(clientCache.get(clientId))
                .map(client -> client.getCodec(key))
                .orElseThrow(NullPointerException::new);
    }

    @Override
    public int databases(String id) {
        return getCache(id).databases;
    }

    class Cache {

        private RedisClient clientConf;

        private int databases;

        private Set<Integer> initDatabases = new HashSet<>();

        private Map<String, Codec> codecMap = new HashMap<>();

        private List<Supplier<RedissonClient>> clients = new ArrayList<>();

        private Codec defaultCodec = createCodec(new RedisClient.CodecConfig());

        public void close() {
            for (Integer database : initDatabases) {
                try {
                    getClient(database).shutdown();
                } catch (Exception e) {
                    log.error("停止redis[{}]:[{}]客户端失败", clientConf, database, e);
                }
            }
        }

        public RedissonClient getClient(int databases) {
            return clients.get(databases).get();
        }

        public void reloadCodec(Map<String, RedisClient.CodecConfig> configMap) {
            codecMap.clear();
            for (Map.Entry<String, RedisClient.CodecConfig> entry : configMap.entrySet()) {
                codecMap.put(entry.getKey(), createCodec(entry.getValue()));
            }
        }

        public void init(RedisClient redisClient) {
            if (redisClient.getCodecConfig() != null) {
                reloadCodec(redisClient.getCodecConfig());
            }
            this.clientConf = redisClient;
            Config firstDatabaseConfig = new Config();
            firstDatabaseConfig.useSingleServer()
                    .setPassword(redisClient.getPassword())
                    .setAddress(redisClient.getAddress())
                    .setConnectionMinimumIdleSize(2)
                    .setConnectionPoolSize(32)
                    .setDatabase(0);
            initDatabases.add(0);
            RedissonClient redissonClient = Redisson.create(firstDatabaseConfig);
            clients.add(0, () -> redissonClient);
            List<Object> data = redissonClient
                    .getScript()
                    .eval(RScript.Mode.READ_ONLY,
                            StringCodec.INSTANCE,
                            "return redis.call('config','get','databases')",
                            RScript.ReturnType.MULTI);
            this.databases = Integer.parseInt(String.valueOf(data.get(1)));

            for (int i = 1; i < this.databases - 1; i++) {
                int fi = i;
                @SuppressWarnings("all")
                Supplier<RedissonClient> supplier = Lazy.val(() -> {
                    Config config = new Config();
                    config.useSingleServer()
                            .setPassword(redisClient.getPassword())
                            .setAddress(redisClient.getAddress())
                            .setConnectionMinimumIdleSize(2)
                            .setConnectionPoolSize(32)
                            .setConnectTimeout(20000)
                            .setDatabase(fi);
                    RedissonClient client = Redisson.create(config);
                    initDatabases.add(fi);
                    return (Supplier<RedissonClient>) () -> client;
                }, Supplier.class);
                clients.add(i, supplier);
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

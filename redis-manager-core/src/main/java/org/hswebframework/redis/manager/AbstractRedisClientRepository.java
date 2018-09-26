package org.hswebframework.redis.manager;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractRedisClientRepository implements RedisClientRepository {

    protected Map<String, Cache> clientMap = new ConcurrentHashMap<>();

    @Override
    public RedissonClient getRedissonClientClient(String id) {
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
        cache.hash = client.hashCode();

        return cache;
    }

    class Cache {
        private long hash;

        private RedissonClient client;
    }
}

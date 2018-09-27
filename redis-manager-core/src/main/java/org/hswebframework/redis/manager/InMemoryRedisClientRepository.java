package org.hswebframework.redis.manager;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public class InMemoryRedisClientRepository extends AbstractRedisClientRepository {

    @Getter
    @Setter
    private Map<String, RedisClient> repository = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (Map.Entry<String, RedisClient> entry : repository.entrySet()) {
            entry.getValue().setId(entry.getKey());
        }
    }

    @Override
    public RedisClient saveOrUpdate(RedisClient client) {
        if (client.getId() == null || client.getId().isEmpty()) {
            client.setId(UUID.randomUUID().toString());
        }
        repository.put(client.getId(), client);

        return client;
    }

    @Override
    public List<RedisClient> allClients() {
        return new ArrayList<>(repository.values());
    }

    @Override
    public RedisClient findById(String clientId) {
        return repository.get(clientId);
    }
}

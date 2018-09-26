package org.hswebframework.redis.manager;

import org.redisson.api.RedissonClient;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface RedisClientRepository {

    RedisClient saveOrUpdate(RedisClient client);

    List<RedisClient> allClients();

    RedisClient findById(String clientId);

    RedissonClient getRedissonClientClient(String id);

}

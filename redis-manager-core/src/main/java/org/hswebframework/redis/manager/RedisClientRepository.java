package org.hswebframework.redis.manager;

import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;

import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface RedisClientRepository {

    RedisClient saveOrUpdate(RedisClient client);

    RedisClient remove(String clientId);

    List<RedisClient> allClients();

    RedisClient findById(String clientId);

    int databases(String id);

    RedissonClient getRedissonClient(String id,int database);

    Codec getCodec(String clientId,String key);
}

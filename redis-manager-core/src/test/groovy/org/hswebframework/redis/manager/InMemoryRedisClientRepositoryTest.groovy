package org.hswebframework.redis.manager

import com.alibaba.fastjson.JSON
import io.netty.buffer.ByteBuf
import org.hswebframework.redis.manager.codec.CodecType
import org.redisson.Redisson
import org.redisson.client.codec.Codec
import org.redisson.client.handler.State
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class InMemoryRedisClientRepositoryTest extends Specification {

    @Shared
    def repository = new InMemoryRedisClientRepository();

    @Shared
    def redisClientId = "default";

    def setup() {

        repository.repository
                .put(redisClientId, new RedisClient(name: "test",
                group: "default",
                comments: "测试",
                address: System.getProperty("redis.manager.repository." + redisClientId + ".address", "redis://localhost:6379"),
                password: System.getProperty("redis.manager.repository." + redisClientId + ".password", null),
                codecConfig: ["test-data": new RedisClient.CodecConfig(keyCodec: CodecType.string, valueCodec: CodecType.fst)]))

        repository.init();
    }

    def cleanup() {
        repository.remove(redisClientId)
    }

    def doEncodeDecode(Codec c, Object o) {
        ByteBuf buf = c.getValueEncoder().encode(o)
        def decode = c.getValueDecoder().decode(buf, new State(false))
        return decode == o
    }


    def "测试配置新增删除查询"() {
        given: "保存一个没有设置id的客户端"
        def client = repository.saveOrUpdate(new RedisClient(name: "save-not-set-id"))
        when: "自动生成了id"
        client.id != null
        then: "删除保存的客户端"
        repository.remove(client.id)
        def old = repository.findById(client.id);
        expect: "删除成功"
        old == null
    }

    boolean initRedissonClientSuccess(String clientId, int database) {
        return ((Redisson) repository.getRedissonClient(clientId, database)) != null
    }

    def "测试自定义序列化"() {
        def bucket = repository.getRedissonClient(redisClientId, 0)
                .getBucket("test-data", repository.getCodec(redisClientId, "test-data"));

        given: "从自定义的jar中初始化类并放入redis"
        def beanClass = repository.classLoader.loadClass("org.hswebframework.redis.manager.beans.TestBean");
        def bean = beanClass.newInstance()
        bean.with {
            id = "test"
            name = "test"
            age = 20
        }
        bucket.set(bean)
        print(JSON.toJSONString(bean))
        expect: "设置成功"
        bucket.get() != null
        bucket.get() != bean

    }

    def "测试初始化redis客户端"() {
        expect: "初始化客户端"
        initRedissonClientSuccess(clientId, database) == success
        where: "初始化结果"
        clientId      | database | success
        redisClientId | 0        | true
        redisClientId | 1        | true
        redisClientId | 2        | true
        redisClientId | 3        | true
        redisClientId | 4        | true
        redisClientId | 5        | true
        redisClientId | 6        | true
        redisClientId | 7        | true
        redisClientId | 8        | true
        redisClientId | 9        | true
        redisClientId | 10       | true
        redisClientId | 11       | true
        redisClientId | 12       | true
        redisClientId | 13       | true
        redisClientId | 14       | true
        redisClientId | 15       | true
    }
}

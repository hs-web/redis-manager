package org.hswebframework.redis.manager

import io.netty.buffer.ByteBuf
import org.hswebframework.redis.manager.codec.CodecType
import org.redisson.client.codec.Codec
import org.redisson.client.handler.State
import spock.lang.Specification

class InMemoryRedisClientRepositoryTest extends Specification {

    def repository = new InMemoryRedisClientRepository();

    def setup() {
        repository.init();

        repository.saveOrUpdate(new RedisClient(id: "test", name: "test",
                group: "default",
                comments: "test",
                address: "redis://localhost:6379",
                password: null,
                codecConfig: ["test-data": new RedisClient.CodecConfig(keyCodec: CodecType.string, valueCodec: CodecType.fst)]))


    }

    def cleanup() {
        repository.remove("test")
    }

    def doEncodeDecode(Codec c, Object o) {
        ByteBuf buf = c.getValueEncoder().encode(o)
        def decode = c.getValueDecoder().decode(buf, new State(false))
        return decode == o
    }

    def "测试redis客户端获取和删除"() {
        given: "获取配置的客户端"
        def redisClient = repository.findById("test");
        when: "获取成功"
        redisClient != null
        then: "删除客户端"
        def old = repository.remove("test");
        expect: "删除成功"
        old != null
        repository.findById("test") == null

    }

    def initRedissonClientSuccess(String clientId, int database) {
        return repository.getRedissonClient(clientId, database) != null;
    }

    def "测试自定义序列化"() {
        def bucket = repository.getRedissonClient("test", 0)
                .getBucket("test-data", repository.getCodec("test", "test-data"));

        given: "从自定义的jar中初始化类并放入redis"
        def testBean = repository.classLoader.loadClass("org.hswebframework.redis.manager.beans.TestBean");
        testBean.with {
            id: "test"
            name: "test"
            age: 20
        }
        bucket.set(testBean)
        expect: "设置成功"
        bucket.get() != null
        bucket.get() == testBean

    }

    def "测试初始化redis客户端"() {
        given: "初始化客户端"
        initRedissonClientSuccess(clientId, database) == success
        where: "初始化结果"
        clientId | database | success
        "test"   | 0        | true
        "test"   | 1        | true
        "test"   | 2        | true
        "test"   | 3        | true
        "test"   | 4        | true
        "test"   | 5        | true
        "test"   | 6        | true
        "test"   | 7        | true
        "test"   | 8        | true
        "test"   | 9        | true
        "test"   | 10       | true
        "test"   | 11       | true
        "test"   | 12       | true
        "test"   | 13       | true
        "test"   | 14       | true
    }
}

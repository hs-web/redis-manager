package org.hswebframework.redis.manager.restful

import com.alibaba.fastjson.JSON

import java.util.concurrent.TimeUnit

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


/**
 * @author zhouhao
 * @since 1.0.0
 */
class HashOperationApiTest extends AbstractTestSupport {

    def database = 11;

    def total = 1000;

    def clientId = "default";
    def key = "redis-manager:hash-test";


    def setup() {
        def client = repository.getRedissonClient(clientId, database);
        for (i in 1..total) {
            client.getMap(key, repository.getCodec(clientId, key)).put("data-" + i, i);
        }
    }

    def cleanup() {
        def client = repository.getRedissonClient(clientId, database);
        client.getMap(key, repository.getCodec(clientId, key)).delete();
    }

    def "测试hlen功能"() {
        given: "执行hlen redis-manager:hash-test操作"
        def hlen = get("/redis/manager/{clientId}/{database}/hlen/{key}", clientId, database, key)
        def result = mockMvc
                .perform(hlen)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        expect: "数量一致"
        JSON.parseObject(result).getInteger("result") == total;
    }

    def "测试hkeys功能"() {
        given: "执行hkeys redis-manager:hash-test操作"
        def hlen = get("/redis/manager/{clientId}/{database}/hkeys/{key}", clientId, database, key)
        def result = mockMvc
                .perform(hlen)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        expect: "数量一致"
        JSON.parseObject(result).getJSONArray("result").size() == total;
    }

    def "测试hget功能"() {
        given: "执行hget redis-manager:hash-test data-10 操作"
        def hlen = get("/redis/manager/{clientId}/{database}/hget/{key}/{field}", clientId, database, key, "data-10")
        def result = mockMvc
                .perform(hlen)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        expect: "值存在并且为10"
        JSON.parseObject(result).getInteger("result") == 10;
    }

    def "测试hdel功能"() {
        given: "执行hdel redis-manager:hash-test data-10 操作"
        def hlen = delete("/redis/manager/{clientId}/{database}/hdel/{key}/{field}", clientId, database, key, "data-10")
        def result = mockMvc
                .perform(hlen)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        when: "返回成功"
        JSON.parseObject(result).getInteger("status") == 200;
        then: "获取hash key data-10是否存在"
        def client = repository.getRedissonClient(clientId, database);
        expect: "data-10已被删除"
        !client.getMap(key, repository.getCodec(clientId, key)).containsKey("data-10")
    }

    def "测试hgetall功能"() {
        given: "执行hgetall redis-manager:hash-test操作"
        def hlen = get("/redis/manager/{clientId}/{database}/hgetall/{key}", clientId, database, key)
        def result = mockMvc
                .perform(hlen)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        expect: "数量一致"
        JSON.parseObject(result).getJSONObject("result").size() == total;
    }
}

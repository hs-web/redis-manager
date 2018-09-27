package org.hswebframework.redis.manager.restful

import com.alibaba.fastjson.JSON
import spock.lang.Shared

import java.util.concurrent.TimeUnit

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author zhouhao
 * @since 1.0.0
 */
class KeysOperationApiTest extends AbstractTest {

    @Shared
    def database = 10;

    @Shared
    def total = 1000;

    def setup() {
        def client = repository.getRedissonClient("default", 10);
        for (i in 1..total) {
            client.getBucket("redis-manager:test:" + i).set(i, 100, TimeUnit.SECONDS);
        }
    }

    def cleanup() {
        def client = repository.getRedissonClient("default", 10);
        long total = client.keys.deleteByPattern("redis-manager:test:*");
        println("delete keys ${total}")
    }

    def "Test Expire Key"() {
        setup:
        def result = mockMvc
                .perform(delete("/redis/manager/{clientId}/{database}/keys/expire/{key}/{seconds}",
                "default",
                database,
                "redis-manager:test:1",
                -1))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def success = JSON.parseObject(result).getBoolean("result");
        def keyInfo = mockMvc
                .perform(get("/redis/manager/{clientId}/{database}/keys/redis-manager:test:1", "default", database))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def ttl = JSON.parseObject(keyInfo).getJSONArray("result").getJSONObject(0).getInteger("ttl");
        expect:
        success
        ttl == -1;

    }

    def "Test Get Keys"() {
        setup:
        def result = mockMvc
                .perform(get("/redis/manager/default/{database}/keys/redis-manager:test:*", database))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def resultSize = JSON.parseObject(result).getJSONArray("result").size();

        result = mockMvc
                .perform(get("/redis/manager/default/{database}/keys/total", database))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def totalResult = JSON.parseObject(result).getLong("result");

        expect:
        totalResult == this.total
        resultSize == this.total
    }
}

package org.hswebframework.redis.manager.restful

import com.alibaba.fastjson.JSON
import spock.lang.Shared
import spock.lang.Subject
import spock.lang.Title

import java.util.concurrent.TimeUnit

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Title("redis key相关操作测试")
@Subject(KeysOperationApi)

class KeysOperationApiTest extends AbstractTestSupport {

    def database = 10;

    def total = 1000;

    def setup() {
        def client = repository.getRedissonClient("default", database);
        for (i in 1..total) {
            client.getBucket("redis-manager:test:" + i).set(i, 100, TimeUnit.SECONDS);
        }
    }

    def cleanup() {
        def client = repository.getRedissonClient("default", database);
        long total = client.keys.deleteByPattern("redis-manager:test:*");
        println("delete keys ${total}")
    }

    def "测试del功能"() {
        given: "执行del redis-manager:test:* 操作"
        def hlen = delete("/redis/manager/{clientId}/{database}/keys/{pattern}", "default", database, "redis-manager:test:*")
        def result = mockMvc
                .perform(hlen)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        when: "返回成功"
        JSON.parseObject(result).getInteger("status") == 200;
        then: "获取现有数据"
        def client = repository.getRedissonClient("default", database);
        expect: "数量为0"
        client.getKeys().getKeysByPattern("redis-manager:test:*").size() == 0
    }


    def "测试Key过期操作"() {
        def clientId = "default"
        def key = "redis-manager:test:1"
        def seconds = 200
        def expireHttpRequest = delete("/redis/manager/{clientId}/{database}/keys/expire/{key}/{seconds}",
                clientId, database, key, seconds)
        def getKeyHttpRequest = get("/redis/manager/{clientId}/{database}/keys/{key}", clientId, database, key)

        given: "设置key:(redis-manager:test:1)过期时间为200"

        def result = mockMvc
                .perform(expireHttpRequest)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        when: "设置成功"
        JSON.parseObject(result).getBoolean("result")

        then: "获取key信息"

        def keyInfo = mockMvc
                .perform(getKeyHttpRequest)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def ttl = JSON.parseObject(keyInfo).getJSONArray("result").getJSONObject(0).getInteger("ttl")
        expect: "key过期时间大于-1小于等于200"
        ttl > -1
        ttl <= 200

    }

    def "测试Key不过期操作"() {
        def clientId = "default"
        def key = "redis-manager:test:1"
        def seconds = -1
        def expireHttpRequest = delete("/redis/manager/{clientId}/{database}/keys/expire/{key}/{seconds}",
                clientId, database, key, seconds)
        def getKeyHttpRequest = get("/redis/manager/{clientId}/{database}/keys/{key}", clientId, database, key)

        given: "设置key:(redis-manager:test:1)过期时间为-1"

        def result = mockMvc
                .perform(expireHttpRequest)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        when: "设置成功"
        JSON.parseObject(result).getBoolean("result")

        then: "获取key信息"

        def keyInfo = mockMvc
                .perform(getKeyHttpRequest)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def ttl = JSON.parseObject(keyInfo).getJSONArray("result").getJSONObject(0).getInteger("ttl");
        expect: "key过期时间为-1"
        ttl == -1

    }

    def "测试获取KEY"() {
        given: "根据pattern:(redis-manager:test:*)获取全部key"
        //获取全部,返回200认为成功
        mockMvc.perform(get("/redis/manager/default/{database}/keys", database)).andExpect(status().is(200))
        //根据pattern获取
        def result = mockMvc
                .perform(get("/redis/manager/default/{database}/keys/{pattern}", database, "redis-manager:test:*"))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def resultSize = JSON.parseObject(result).getJSONArray("result").size();
        when: "获取成功"
        resultSize > 0

        then: "根据pattern:(redis-manager:test:*)获取总数"
        def totalResult = mockMvc
                .perform(get("/redis/manager/default/{database}/keys/total", database))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def total = JSON.parseObject(totalResult).getLong("result");

        expect: "获取成功,数量一致"
        total == this.total
        resultSize == this.total
    }
}

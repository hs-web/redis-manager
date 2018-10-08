package org.hswebframework.redis.manager.restful

import com.alibaba.fastjson.JSON
import org.hswebframework.redis.manager.RedisClient
import org.hswebframework.redis.manager.restful.model.RedisInfo
import org.springframework.http.MediaType

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author zhouhao
 * @since 1.0.0
 */
class RedisClientManagerApiTest extends AbstractTestSupport {

    def "Test Query"() {
        setup:
        def result = mockMvc
                .perform(get("/redis/manager/clients"))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def redisClient = JSON.parseObject(result)
                .getJSONArray("result")
                .getJSONObject(0)
                .toJavaObject(RedisClient.class);

        expect:
        println result
        redisClient == repository.findById(redisClient.getId())
    }

    def "Test Add"() {
        setup:
        def client = new RedisClient(id: "test", name: "测试", address: "tcp://localhost:6379");
        def result = mockMvc
                .perform(patch("/redis/manager/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(client)))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def redisClient = JSON.parseObject(result)
                .getJSONObject("result")
                .toJavaObject(RedisClient.class);
        expect:
        println result
        client == redisClient
        redisClient == repository.findById(redisClient.getId())
    }

    def "Test GetDatabases"() {
        setup:
        def result = mockMvc
                .perform(get("/redis/manager/default/databases"))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def database = JSON.parseObject(result)
                .getInteger("result")
        expect:
        println database
        database != null
        database != 0
    }

    def "Test getInfo"() {
        setup:
        def result = mockMvc
                .perform(get("/redis/manager/default/info"))
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        def info = JSON.parseObject(result)
                .getJSONObject("result")
                .toJavaObject(RedisInfo.class)
        expect:
        println info
        info != null
    }
}

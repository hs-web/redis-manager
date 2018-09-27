package org.hswebframework.redis.manager.restful

import com.alibaba.fastjson.JSON
import org.hswebframework.redis.manager.RedisClient
import org.hswebframework.redis.manager.RedisClientRepository
import org.hswebframework.redis.manager.TestApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author zhouhao
 * @since 1.0.0
 */
class RedisClientManagerApiTest extends AbstractTest {

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
}

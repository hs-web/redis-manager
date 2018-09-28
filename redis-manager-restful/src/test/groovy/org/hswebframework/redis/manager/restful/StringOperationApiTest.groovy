package org.hswebframework.redis.manager.restful

import com.alibaba.fastjson.JSON
import org.springframework.http.MediaType
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * @author zhouhao
 * @since 1.0.0
 */
class StringOperationApiTest extends AbstractTestSupport {


    def "测试get set功能"() {
        given: "执行set test-data data-0 操作"
        def set = patch("/redis/manager/{clientId}/{database}/set/{key}", "default", 12, "test-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content("data-0");

        def result = mockMvc
                .perform(set)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()
        when: "执行成功"
        JSON.parseObject(result).getInteger("status") == 200
        then: "执行get test-data 操作"
        def get = get("/redis/manager/{clientId}/{database}/get/{key}", "default", 12, "test-data")
        def getResult = mockMvc
                .perform(get)
                .andExpect(status().is(200))
                .andReturn()
                .getResponse()
                .getContentAsString()

        expect: "值为data-0"
        JSON.parseObject(getResult).getString("result") == "data-0"
    }
}

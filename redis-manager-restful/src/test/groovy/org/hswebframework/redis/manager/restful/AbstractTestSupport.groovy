package org.hswebframework.redis.manager.restful

import org.hswebframework.redis.manager.RedisClientRepository
import org.hswebframework.redis.manager.TestApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author zhouhao
 * @since 1.0.0
 */
@WebAppConfiguration
@ContextConfiguration
@SpringBootTest(classes = [TestApplication.class], properties = ["classpath:application.yml"])
class AbstractTestSupport extends Specification {
    @Autowired
    protected ConfigurableApplicationContext context;

    @Shared
    protected MockMvc mockMvc;

    @Shared
    protected RedisClientRepository repository;

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository = context.getBean(RedisClientRepository.class);
    }

    def "启动测试类"() {
        expect: "启动正常"
        context != null
        mockMvc != null
        repository != null
    }
}

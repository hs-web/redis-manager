package org.hswebframework.redis.manager;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootApplication
@WebAppConfiguration
@Configuration
public class TestApplication {

    @Bean
    @ConfigurationProperties(prefix = "redis.manager")
    public RedisClientRepository redisClientRepository(){
        return new InMemoryRedisClientRepository();
    }

}

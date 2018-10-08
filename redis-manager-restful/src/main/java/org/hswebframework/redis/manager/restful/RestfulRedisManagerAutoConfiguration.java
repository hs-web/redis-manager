package org.hswebframework.redis.manager.restful;

import org.hswebframework.redis.manager.InMemoryRedisClientRepository;
import org.hswebframework.redis.manager.RedisClientRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Configuration
@ComponentScan("org.hswebframework.redis.manager.restful")
public class RestfulRedisManagerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedisClientRepository.class)
    @ConfigurationProperties(prefix = "redis.manager")
    public RedisClientRepository inMemoryRedisClientRepository() {
        return new InMemoryRedisClientRepository();
    }
}

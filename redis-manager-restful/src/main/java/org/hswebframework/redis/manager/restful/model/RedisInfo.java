package org.hswebframework.redis.manager.restful.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtilsBean;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@Slf4j
public class RedisInfo {

    private Map<String, Object> server = new LinkedHashMap<>();

    private Map<String, Object> clients = new LinkedHashMap<>();

    private Map<String, Object> memory = new LinkedHashMap<>();

    private Map<String, Object> cpu = new LinkedHashMap<>();

    private Map<String, Object> cluster = new LinkedHashMap<>();

    private Map<String, Object> keyspace = new LinkedHashMap<>();

    private Map<String, Object> persistence = new LinkedHashMap<>();

    private Map<String, Object> stats = new LinkedHashMap<>();

    private Map<String, Object> replication = new LinkedHashMap<>();

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
    }

    @SuppressWarnings("all")
    public static RedisInfo fromRedisInfoCommandString(String info) {
        RedisInfo redisInfo = new RedisInfo();

        String[] lines = info.split("[\n]");
        Map<String, Object> tmp = redisInfo.server;

        for (String line : lines) {
            if (line.startsWith("#") && line.length() > 2) {
                String name = line.substring(1, line.length()).trim().toLowerCase();
                try {
                    tmp = (Map<String, Object>) BeanUtilsBean.getInstance()
                            .getPropertyUtils()
                            .getProperty(redisInfo, name);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
                continue;
            }
            String[] kv = line.split("[:]");
            if (kv.length > 1) {
                tmp.put(kv[0], kv[1].replace("\r", ""));
            }
        }
        return redisInfo;
    }
}

package org.hswebframework.redis.manager;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.redis.manager.codec.CodecType;

import java.util.Map;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Getter
@Setter
@EqualsAndHashCode
public class RedisClient {

    //id
    private String id;

    //客户端名称
    private String name;

    //客户端分组
    private String group;

    //备注
    private String comments;

    //服务端地址
    private String address = "redis://localhost:6379";

    private int database = 0;

    private Map<String, CodecConfig> encodeDecodeConfig;

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class CodecConfig {
        private CodecType keyCodec = CodecType.string;

        private CodecType valueCodec = CodecType.string;
    }



}

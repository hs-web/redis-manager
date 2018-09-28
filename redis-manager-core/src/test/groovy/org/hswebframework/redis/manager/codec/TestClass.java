package org.hswebframework.redis.manager.codec;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zhouhao
 * @since 1.0
 */
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class TestClass implements Serializable {
    private String id;

    private String name;

    private int age;

    private boolean enabled;
}

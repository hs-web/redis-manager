package org.hswebframework.redis.manager.restful.mode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KeyModel {

    private String key;

    private String type;

    private long ttl;
}

package org.hswebframework.redis.manager.restful.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyModel {

    private String key;

    private String type;

    private long ttl;

}

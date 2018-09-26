package org.hswebframework.redis.manager.restful;

import org.hswebframework.redis.manager.RedisClientRepository;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis/manager")
public class HashOperationApi {

    @Autowired
    private RedisClientRepository repository;


    @GetMapping("/{clientId}/hget/{key:.*}/{field:.*}")
    public ResponseMessage<Object> hget(@PathVariable String clientId, @PathVariable String key, @PathVariable String field) {
        return ResponseMessage.ok(repository.getRedissonClient(clientId)
                .getMap(key, repository.getCodec(clientId, key))
                .get(field));
    }
}

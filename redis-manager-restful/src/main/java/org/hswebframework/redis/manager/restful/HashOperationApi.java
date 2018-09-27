package org.hswebframework.redis.manager.restful;

import org.hswebframework.redis.manager.RedisClientRepository;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/redis/manager")
public class HashOperationApi {

    @Autowired
    private RedisClientRepository repository;

    @GetMapping("/{clientId}/{database}/hlen/{key:.*}")
    public ResponseMessage<Integer> hlen(@PathVariable String clientId,
                                         @PathVariable int database,
                                         @PathVariable String key) {
        return ResponseMessage.ok(repository.getRedissonClient(clientId, database)
                .getMap(key, repository.getCodec(clientId, key))
                .size());
    }


    @GetMapping("/{clientId}/{database}/hkeys/{key:.*}")
    public ResponseMessage<Set<Object>> hkeys(@PathVariable String clientId,
                                              @PathVariable int database,
                                              @PathVariable String key) {
        return ResponseMessage.ok(repository.getRedissonClient(clientId, database)
                .getMap(key, repository.getCodec(clientId, key))
                .keySet());
    }

    @GetMapping("/{clientId}/{database}/hget/{key:.*}/{field:.*}")
    public ResponseMessage<Object> hget(@PathVariable String clientId,
                                        @PathVariable int database,
                                        @PathVariable String key,
                                        @PathVariable String field) {
        return ResponseMessage.ok(repository.getRedissonClient(clientId, database)
                .getMap(key, repository.getCodec(clientId, key))
                .get(field));
    }

    @DeleteMapping("/{clientId}/{database}/hdel/{key:.*}/{field:.*}")
    public ResponseMessage<Object> hdel(@PathVariable String clientId,
                                        @PathVariable int database,
                                        @PathVariable String key,
                                        @PathVariable String field) {
        return ResponseMessage.ok(repository.getRedissonClient(clientId, database)
                .getMap(key, repository.getCodec(clientId, key))
                .remove(field));
    }

    @GetMapping("/{clientId}/{database}/hgetall/{key:.*}")
    public ResponseMessage<Map<Object, Object>> hgetall(@PathVariable String clientId,
                                                        @PathVariable int database,
                                                        @PathVariable String key) {
        return ResponseMessage.ok(repository.getRedissonClient(clientId, database)
                .getMap(key, repository.getCodec(clientId, key)));
    }
}

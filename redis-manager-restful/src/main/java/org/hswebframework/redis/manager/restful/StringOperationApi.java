package org.hswebframework.redis.manager.restful;

import org.hswebframework.redis.manager.RedisClientRepository;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.hswebframework.web.controller.message.ResponseMessage.*;

@RestController
@RequestMapping("/redis/manager")
@Authorize(permission = "redis-manager")
public class StringOperationApi {


    @Autowired
    RedisClientRepository repository;

    @GetMapping("/{clientId}/get/{key}")
    public ResponseMessage<Object> getValue(@PathVariable String clientId, @PathVariable String key) {
        return ok(repository.getRedissonClient(clientId)
                .getBucket(key, repository.getCodec(clientId, key))
                .get());
    }

    @PutMapping("/{clientId}/set/{key}")
    public ResponseMessage<Void> setValue(@PathVariable String clientId, @PathVariable String key, @RequestBody String jsonRequest) {
        repository.getRedissonClient(clientId)
                .getBucket(key, repository.getCodec(clientId, key))
                .set(jsonRequest);
        return ok();
    }
}

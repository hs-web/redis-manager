package org.hswebframework.redis.manager.restful;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.redis.manager.RedisClientRepository;
import org.hswebframework.redis.manager.restful.mode.KeyModel;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.redisson.api.RKeys;
import org.redisson.api.RScript;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@RequestMapping("/redis/manager")
@Api(tags = "redis-key操作接口", description = "redis-key操作接口")
@Authorize(permission = "redis-manager")
public class KeysOperationApi {

    @Autowired
    private RedisClientRepository repository;


    private RKeys getRkeys(String clientId, int database) {
        return repository.getRedissonClient(clientId, database)
                .getKeys();
    }

    @GetMapping("/{clientId}/{database}/keys/total")
    @Authorize(action = {"keys", "*"}, logical = Logical.OR)
    @SuppressWarnings("all")
    @ApiOperation("获取key总数")
    public ResponseMessage<Long> totalKeys(@PathVariable String clientId,
                                           @PathVariable int database) {
        return ok(getRkeys(clientId, database).count());
    }

    @GetMapping("/{clientId}/{database}/keys")
    @Authorize(action = {"keys", "*"}, logical = Logical.OR)
    @SuppressWarnings("all")
    @ApiOperation("获取指定数量的keys")
    public ResponseMessage<List<KeyModel>> keys(@PathVariable String clientId,
                                                @PathVariable int database) {
        return ok(StreamSupport.stream(getRkeys(clientId, database)
                .getKeys()
                .spliterator(), true)
                .map(key -> createModel(clientId, database, key))
                .collect(Collectors.toList()));
    }

    @GetMapping("/{clientId}/{database}/keys/{pattern:.*}")
    @Authorize(action = {"keys", "*"}, logical = Logical.OR)
    @SuppressWarnings("all")
    @ApiOperation("获取指定数量以及规则的keys")
    public ResponseMessage<List<KeyModel>> keysByPattern(@PathVariable String clientId,
                                                         @PathVariable int database,
                                                         @PathVariable String pattern) {
        return ok(StreamSupport.stream(getRkeys(clientId, database)
                .getKeysByPattern(pattern)
                .spliterator(), true)
                .map(key -> createModel(clientId, database, key))
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/{clientId}/{database}/keys/{pattern:.*}")
    @Authorize(action = {"delete", "*"}, logical = Logical.OR)
    @SuppressWarnings("all")
    @ApiOperation("删除指定规则的key")
    public ResponseMessage<Long> deleteKey(@PathVariable String clientId,
                                           @PathVariable int database,
                                           @PathVariable String pattern) {
        return ok(getRkeys(clientId, database).deleteByPattern(pattern));
    }


    @DeleteMapping("/{clientId}/{database}/keys/expire/{key:.*}/{seconds}")
    @Authorize(action = {"delete", "*"}, logical = Logical.OR)
    @SuppressWarnings("all")
    @ApiOperation("设置key过期")
    public ResponseMessage<Boolean> expireKey(@PathVariable String clientId,
                                              @PathVariable int database,
                                              @PathVariable String key,
                                              @PathVariable long seconds) {
        if (seconds < 0) {
            return ok(repository.getRedissonClient(clientId, database)
                    .getKeys()
                    .clearExpire(key));
        }
        return ok(repository.getRedissonClient(clientId, database)
                .getKeys()
                .expire(key, seconds, TimeUnit.SECONDS));
    }

    private KeyModel createModel(String clientId, int database, String key) {
        List<String> typeAndTTl = repository.getRedissonClient(clientId, database)
                .getScript()
                .eval(RScript.Mode.READ_ONLY,
                        StringCodec.INSTANCE,
                        "return {redis.call('type',ARGV[1]),redis.call('ttl',ARGV[1])}",
                        RScript.ReturnType.MULTI,
                        Collections.emptyList(),
                        key);
        return new KeyModel(key, typeAndTTl.get(0), Long.valueOf(String.valueOf(typeAndTTl.get(1))));
    }

}

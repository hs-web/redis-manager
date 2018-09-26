package org.hswebframework.redis.manager.restful;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.IteratorUtils;
import org.hswebframework.redis.manager.RedisClientRepository;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.id.IDGenerator;
import org.redisson.api.RKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hswebframework.web.controller.message.ResponseMessage.*;

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

    @Autowired
    private Map<String, RKeys> rKeysMap = new HashMap<>();

    private RKeys getRkeys(String sessionId) {
        return Optional.ofNullable(rKeysMap.get(sessionId))
                .orElseThrow(NotFoundException::new);
    }

    @PostMapping("{clientId}/keys/open-session")
    @Authorize(action = {"keys", "*"}, logical = Logical.OR)
    @ApiOperation("打开一个session")
    public ResponseMessage<String> openSession(@PathVariable String clientId) {
        String sessionId = IDGenerator.MD5.generate();
        rKeysMap.put(sessionId, repository.getRedissonClientClient(clientId).getKeys());
        return ok(sessionId);
    }

    @DeleteMapping("/keys/{sessionId}/close-session")
    @Authorize(action = {"keys", "*"}, logical = Logical.OR)
    @ApiOperation("结束一个session")
    public ResponseMessage<Void> closeSession(@PathVariable String sessionId) {
        rKeysMap.remove(sessionId);
        return ok();
    }

    @GetMapping("/keys/{sessionId}/total")
    @Authorize(action = {"keys", "*"}, logical = Logical.OR)
    @SuppressWarnings("all")
    @ApiOperation("获取key总数")
    public ResponseMessage<Long> totalKeys(@PathVariable String sessionId) {
        return ok(getRkeys(sessionId).count());
    }

    @GetMapping("/keys/{sessionId}/{count}")
    @Authorize(action = {"keys", "*"}, logical = Logical.OR)
    @SuppressWarnings("all")
    @ApiOperation("获取指定数量的keys")
    public ResponseMessage<List<String>> keys(@PathVariable String sessionId,
                                              @PathVariable int count) {
        return ok(IteratorUtils.toList(getRkeys(sessionId).getKeys(count).iterator()));
    }

    @GetMapping("/keys/{sessionId}/{count}/{pattern}")
    @Authorize(action = {"keys", "*"}, logical = Logical.OR)
    @SuppressWarnings("all")
    @ApiOperation("获取指定数量以及规则的keys")
    public ResponseMessage<List<String>> keysByPattern(@PathVariable String sessionId,
                                                       @PathVariable String pattern,
                                                       @PathVariable int count) {
        return ok(IteratorUtils.toList(
                getRkeys(sessionId)
                        .getKeysByPattern(pattern, count)
                        .iterator()));
    }

    @DeleteMapping("/keys/{sessionId}/{pattern}")
    @Authorize(action = {"delete", "*"}, logical = Logical.OR)
    @SuppressWarnings("all")
    @ApiOperation("删除指定规则的key")
    public ResponseMessage<Long> deleteKey(@PathVariable String sessionId,
                                           @PathVariable String pattern) {
        return ok(getRkeys(sessionId)
                .deleteByPattern(pattern));
    }


    @DeleteMapping("/keys/{sessionId}/expire/{key}/{milliseconds}")
    @Authorize(action = {"delete", "*"}, logical = Logical.OR)
    @SuppressWarnings("all")
    @ApiOperation("设置key过期")
    public ResponseMessage<Boolean> expireKey(@PathVariable String clientId,
                                              @PathVariable String key,
                                              @PathVariable long milliseconds) {
        return ok(repository.getRedissonClientClient(clientId)
                .getKeys()
                .expire(key, milliseconds, TimeUnit.MILLISECONDS));
    }

}

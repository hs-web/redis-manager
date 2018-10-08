package org.hswebframework.redis.manager.restful;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.redis.manager.RedisClient;
import org.hswebframework.redis.manager.RedisClientRepository;
import org.hswebframework.redis.manager.restful.model.RedisInfo;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.redisson.Redisson;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@RestController
@Api(tags = "redis-客户端管理", description = "redis客户端管理")
@Authorize(permission = "redis-client-manager")
@RequestMapping("/redis/manager")
public class RedisClientManagerApi {

    @Autowired
    private RedisClientRepository redisClientRepository;


    @GetMapping("/{clientId}/info")
    @ApiOperation("获取客户端详细信息")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<RedisInfo> getRedisInfo(@PathVariable String clientId) {
        RedissonClient client = redisClientRepository.getRedissonClient(clientId, 0);

        String info = client.getScript()
                .eval(RScript.Mode.READ_ONLY, StringCodec.INSTANCE,
                        "return redis.call('info')",
                        RScript.ReturnType.VALUE, Collections.emptyList());

        return ResponseMessage.ok(RedisInfo.fromRedisInfoCommandString(info));
    }


    @GetMapping("/clients")
    @ApiOperation("获取全部客户端信息")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<List<RedisClient>> getAllRedisClient() {
        return ResponseMessage.ok(redisClientRepository.allClients());
    }


    @GetMapping("/{clientId}/databases")
    @ApiOperation("获取数据库数量")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<Integer> getDatabases(@PathVariable String clientId) {
        return ResponseMessage.ok(redisClientRepository.databases(clientId));
    }

    @PatchMapping("/client")
    @ApiOperation("新增或者修改客户端信息")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<RedisClient> saveOrUpdate(@RequestBody RedisClient client) {
        return ResponseMessage.ok(redisClientRepository.saveOrUpdate(client));
    }
}

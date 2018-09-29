package org.hswebframework.redis.manager.codec

import io.netty.buffer.ByteBuf
import org.redisson.client.codec.Codec
import org.redisson.client.handler.State
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author zhouhao
 * @since 1.0.0
 */
class CodecTypeTest extends Specification {

    @Shared
    def string = CodecType.string.createCodec(this.class.getClassLoader());
    @Shared
    def jdk = CodecType.jdk.createCodec(this.class.getClassLoader());
    @Shared
    def fst = CodecType.fst.createCodec(this.class.getClassLoader());
    @Shared
    def kryo = CodecType.kryo.createCodec(this.class.getClassLoader());

    @Shared
    def bean = new TestClass(id: "test", name: "测试", age: 18, enabled: true);

    def doEncodeDecode(Codec c, Object o) {
        ByteBuf buf = c.getValueEncoder().encode(o)
        def decode = c.getValueDecoder().decode(buf, new State(false))
        return decode
    }

    def "测试序列化"() {
        expect: "准备执行"
        doEncodeDecode(codec, data) == res
        where: "序列化,反序列结果"
        codec  | data | res
        string | bean | bean.toString()
        fst    | bean | bean
        jdk    | bean | bean
        kryo   | bean | bean
    }
}

package org.hswebframework.redis.manager.codec;

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.FstCodec;
import org.redisson.codec.KryoCodec;
import org.redisson.codec.SerializationCodec;

public enum CodecType {
    string() {
        @Override
        public Codec createCodec(ClassLoader classLoader) {
            return StringCodec.INSTANCE;
        }
    },
    jdk() {
        @Override
        public Codec createCodec(ClassLoader classLoader) {
            return new SerializationCodec(classLoader);
        }
    },
    fst() {
        @Override
        public Codec createCodec(ClassLoader classLoader) {
            return new FstCodec(classLoader);
        }
    },
    kryo() {
        @Override
        public Codec createCodec(ClassLoader classLoader) {
            return new KryoCodec(classLoader);
        }
    };

    public abstract Codec createCodec(ClassLoader classLoader);
}
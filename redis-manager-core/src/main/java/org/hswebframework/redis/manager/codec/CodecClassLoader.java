package org.hswebframework.redis.manager.codec;

import java.net.URL;
import java.net.URLClassLoader;

public class CodecClassLoader extends URLClassLoader {


    public CodecClassLoader(URL... urls) {
        super(urls);
    }
}

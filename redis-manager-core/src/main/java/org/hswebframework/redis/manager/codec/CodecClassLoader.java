package org.hswebframework.redis.manager.codec;

import io.vavr.API;
import io.vavr.CheckedFunction1;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;

public class CodecClassLoader extends URLClassLoader {

    public CodecClassLoader(File... urls) {
        super(Stream.of(urls)
                .flatMap(API.unchecked(file -> {
                    if (file.isDirectory()) {
                        File[] children = file.listFiles();
                        return children == null ? Stream.empty() : Stream.of(children);
                    }
                    return Stream.of(file);
                }))
                .map(API.unchecked(file -> file.toURI().toURL()))
                .toArray(URL[]::new));
    }

    public CodecClassLoader(String... urls) {
        super(Stream.of(urls)
                .map(API.<String, URL>unchecked(URL::new))
                .toArray(URL[]::new));
    }

    public CodecClassLoader(URL... urls) {
        super(urls);
    }
}

package org.hswebframework.redis.manager.codec

import spock.lang.Specification

/**
 * @author zhouhao
 * @since 1.0.0
 */
class CodecClassLoaderTest extends Specification {

    def "测试自定义类加载"() {
        given:
        def classLoader = new CodecClassLoader(new File(System.getProperty("codec.lib.dir", "./lib")))

        and:
        def testBean = classLoader.loadClass("org.hswebframework.redis.manager.beans.TestBean");

        expect:
        testBean != null
    }
}

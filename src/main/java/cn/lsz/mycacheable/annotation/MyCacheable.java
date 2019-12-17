package cn.lsz.mycacheable.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 用于缓存,目前key
 *
 * @author LSZ 2019/12/11 10:03:00
 * @contact 648748030@qq.com
 */
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MyCacheable {

    /**
     * key实例
     * 当前注解用于需要设置过期时间的缓存替代 @Cacheable
     * 1、自动生成key,例如方法getList("param1","param2"),@RedisTemplateCache(key = "redis_key")生成的key为: redis_key:param1:param2
     * 2、占位符key,例如方法getList("param1","param2"),@RedisTemplateCache(key = "redis_key_%s_%s")生成的key为: redis_key_param1_param2 ,注意占位符与参数个数对应
     * */
    String value();

    String[] key();

    long timeOut() default 7200L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}

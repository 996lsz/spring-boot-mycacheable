package cn.lsz.mycacheable.aop;

import cn.lsz.mycacheable.annotation.MyCacheable;
import cn.lsz.mycacheable.config.MyConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
/*import com.github.pagehelper.Page;
import com.github.pagehelper.SqlUtil;*/
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * 自定义注解@RedisCache处理
 * 目前只支持参数为String(基本类型)...,opsForValue的缓存
 * 
 * @author LSZ 2019/10/15 15:29
 * @contact 648748030@qq.com
 */
@Aspect
@Component
public class MyCacheableAspect {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RedisTemplate redisTemplate;

    @Around("@annotation(myCacheable)")
    public Object around(ProceedingJoinPoint joinpoint, MyCacheable myCacheable) throws Throwable {

        MethodSignature msig = (MethodSignature) joinpoint.getSignature();
        Method currentMethod = joinpoint.getTarget().getClass().getMethod(msig.getName(), msig.getParameterTypes());
        List<MyConfig.MyClass> list = MyConfig.myMap.get(currentMethod.toString());

        StringBuilder redisKey = new StringBuilder(myCacheable.value());
        Object[] args = joinpoint.getArgs();

        for (MyConfig.MyClass myClass : list) {
            Object arg = args[myClass.getIndex()];
            if(myClass.getMethod() == null){
                //内容为下标，直接拼接
                redisKey.append(":"+getRedisKeyByClassType(arg));
            } else {
                for(int i = 0; i < myClass.getMethod().size(); i++) {
                    Method method = myClass.getMethod().get(i);
                    if(myClass.getParams().size() == 0){
                        arg = method.invoke(arg);
                    }else{
                        Object param = myClass.getParams().get(i);
                        if(param == null ){
                            arg = method.invoke(arg);
                        }else{
                            arg = method.invoke(arg, param);
                        }
                    }
                }
               redisKey.append(":"+getRedisKeyByClassType(arg));
           }
        }
        //redisKey拼接分页参数,redisKey:(pageNum,pageSize)
/*        Page<Object> localPage = SqlUtil.getLocalPage();
        if(localPage != null){
            redisKey.append(":("+localPage.getPageNum()+","+localPage.getPageSize()+")");
        }*/

        Object result = redisTemplate.opsForValue().get(redisKey.toString());
        if(result != null){
            LOGGER.info("获取缓存："+redisKey);
/*            if(localPage != null){
                SqlUtil.clearLocalPage();
            }*/
            return result;
        }
        result = joinpoint.proceed();
        redisTemplate.opsForValue().set(redisKey.toString(), result, myCacheable.timeOut(), myCacheable.timeUnit());
        LOGGER.info("设置缓存："+redisKey);
        return result;
    }

    private Object getRedisKeyByClassType(Object arg){
        if(JSON.class.isAssignableFrom(arg.getClass())){
            return JSONObject.parseObject(JSONObject.toJSONString(arg),Map.class);
        }else{
            return arg;
        }
    }
    
}

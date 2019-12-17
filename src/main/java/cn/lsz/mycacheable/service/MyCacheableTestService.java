package cn.lsz.mycacheable.service;

import cn.lsz.mycacheable.annotation.MyCacheable;
import cn.lsz.mycacheable.entity.Person;
import com.alibaba.fastjson.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * description
 * 
 * @author LSZ 2019/12/06 12:47
 * @contact 648748030@qq.com
 */
@Component
public class MyCacheableTestService {


    @MyCacheable(value = "testIndex", key = {"#p1","#p2","#p0"},timeOut = 1,timeUnit = TimeUnit.DAYS)
    public String testIndex(Map map,JSONObject jsonObject,String id){
        System.out.println("testIndex没有缓存");
        return "testIndex success";
    }

    @MyCacheable(value = "testObject", key = {"#id","#jsonObject"})
    public String testObject(Map map,JSONObject jsonObject,String id){
        System.out.println("testObject没有缓存");
        return "testObject success";
    }

    //还有点问题需要继续优化
    //@MyCacheable(value = "testMethod",key={"#map.get('person').getPerson().getId()","#person.getPerson().getName()"})
    @MyCacheable(value = "testMethod",key={"#person.getPerson().getName()"})
    public String testMethod(Person person,Map map){
        System.out.println("testMethod没有缓存");
        return "testMethod success";
    }

}

package cn.lsz.mycacheable;

import cn.lsz.mycacheable.entity.Person;
import cn.lsz.mycacheable.service.MyCacheableTestService;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SpringBootTest
class MycacheableApplicationTests {

    @Autowired
    MyCacheableTestService service;

    @Test
    void contextLoads() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id","jsonId");
        jsonObject.put("name","jsonName");

        Map map = new HashMap();
        map.put("id","mapId");
        map.put("name","mapName");

        System.out.println("测试下标缓存："+service.testIndex(map,jsonObject,"233"));
        System.out.println("测试对象缓存："+service.testObject(map,jsonObject,"233"));

        Person p = new Person("lsz",18,null);
        Person p2 = new Person("lll",19,p);
        map.put("person",p2);
        System.out.println("测试方法缓存："+service.testMethod(p2,map));


    }

}

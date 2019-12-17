package cn.lsz.mycacheable.config;

import cn.lsz.mycacheable.annotation.MyCacheable;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description
 * 
 * @author LSZ 2019/12/14 11:28
 * @contact 648748030@qq.com
 */
@Component
public class MyConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    //截取字符串如’#p123‘ 获取数字值’123‘
    private static final Pattern SUBSCRIPT_PATTERN = Pattern.compile("(?<=#[p,P])\\d+");
    //截取方法中方法名，如get('test')截取get
    private static final Pattern METHOD_NAME_PATTERN = Pattern.compile("\\w+(?=\\()");
    //截取方法中参数值,如get('test')截取test
    private static final Pattern METHOD_VALUE_PATTERN = Pattern.compile("(?<=\\(').+(?='\\))");

    //map存的为Method或参数下标
    public static Map<String,List<MyClass>> myMap = new HashMap();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOGGER.debug("=====自定义注解扫描Start=====");
        // 根容器为Spring容器
        if(event.getApplicationContext().getParent()==null){
            //这里只扫描了Component 的Service类，有需要再新增
            Map<String,Object> beans = event.getApplicationContext().getBeansWithAnnotation(Component.class);
            for(Object bean : beans.values()){
                //Method[] declaredMethods = bean.getClass().getDeclaredMethods();
                Method[] methods = bean.getClass().getMethods();
                for (Method method : methods) {
                    //获取使用了自定义注解的方法
                    MyCacheable annotation = AnnotationUtils.findAnnotation(method, MyCacheable.class);
                    if(annotation != null){
                        //保存key中解析的结果
                        Method sourceMethod = null;
                        List<MyClass> paramList = null;
                        try {
                            //这里的bean为代理类，需要转换成原对象
                            Class<?> targetClass = AopUtils.getTargetClass(bean);
                            sourceMethod = targetClass.getMethod(method.getName(), method.getParameterTypes());
                            //解析map key
                            paramList = keyHandler(annotation.key(), sourceMethod);
                        } catch (NoSuchMethodException e) {
                            LOGGER.error(ExceptionUtils.getMessage(e));
                            continue;
                        }
                        myMap.put(sourceMethod.toString(), paramList);
                    }
                }
            }
            LOGGER.debug("=====自定义注解扫描END=====");
        }
    }

    private List<MyClass> keyHandler(String[] keys, Method sourceMethod) throws NoSuchMethodException {
        List<MyClass> paramList = new ArrayList();
        //String[] keys = sourceKey.split(",");
        outer: for(int i = 0; i < keys.length; i++){
            String key = keys[i];
            //匹配是否符合参数下标格式并获取下标
            Matcher matcher = SUBSCRIPT_PATTERN.matcher(key);
            if(matcher.find()){
                paramList.add(new MyClass(Integer.parseInt(matcher.group()),null,null));
                continue;
            }else{
                //切割字符串，如将方法'#person.getChirdren().getId()'切割成'[#person，getChirdren()，getId()]'
                String[] paramNames = key.split("\\.");
                Parameter[] parameters = sourceMethod.getParameters();
                Class<?>[] parameterTypes = sourceMethod.getParameterTypes();
                //获取对象名，如'#person'截取为'person'
                String objectKey = paramNames[0].substring(1);
                //获取对象信息
                Parameter paramObject = null;
                Class parameterType = null;
                int index = 0;
                for(int b = 0; b < parameters.length; b++) {
                    if(objectKey.equals(parameters[b].getName())){
                        index = b;
                        paramObject = parameters[b];
                        parameterType = parameterTypes[b];
                    }
                }

                if(paramNames.length > 1){
                    //方法,有可能是多重方法，例如person.getChirdren().getId()里面有getChirdren()以及getId()两个方法
                    List<Method> methods = new ArrayList<>();
                    List<Object> params = new ArrayList<>();
                    for(int a = 1; a < paramNames.length; a++) {
                        String paramName = paramNames[a];
                        Matcher methodValueMatcher = METHOD_VALUE_PATTERN.matcher(paramName);
                        Matcher methodNameMatcher = METHOD_NAME_PATTERN.matcher(paramName);
                        methodNameMatcher.find();
                        String methodName = methodNameMatcher.group();
                        if(methodValueMatcher.find()){
                            Method method = parameterType.getMethod(methodName, Object.class);
                            methods.add(method);
                            params.add(methodValueMatcher.group());
                        }else{
                            Method method = parameterType.getMethod(methodName);
                            methods.add(method);
                            params.add(null);
                        }
                    }
                    paramList.add(new MyClass(index,methods,params));
                }else{
                    //整个对象，保存下标
                    paramList.add(new MyClass(index,null,null));
                }
            }
        }
        return paramList;
    }

    @Data
    @AllArgsConstructor
    public class MyClass{
        private int index;
        private List<Method> method;
        private List<Object> params;
    }
}

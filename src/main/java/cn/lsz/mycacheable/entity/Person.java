package cn.lsz.mycacheable.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * description
 * 
 * @author LSZ 2019/12/13 13:48
 * @contact 648748030@qq.com
 */
@Data
@AllArgsConstructor
public class Person {

    private String name;

    private int id;

    private Person person;
}

package org.ppj.pp.event.sample.domain.entity;

import org.ppj.pp.event.core.entity.BaseModel;
import org.ppj.pp.event.sample.domain.event.UserCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseModel {

    private Long id;
    private Long number;
    private String name;
    private Integer age;
    private Integer sex;
    private Date joinTime;

    public User(Long number, String name, Integer age, Integer sex) {
        this.number = number;
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.joinTime = new Date();
    }

    public void applyCreateEvent() {
        this.apply(new UserCreatedEvent(number, name, age, sex));
    }
}

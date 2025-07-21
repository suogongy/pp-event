package org.ppj.pp.event.sample.service;

import org.ppj.pp.event.sample.domain.entity.User;
import org.ppj.pp.event.sample.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findById(long userId) {
        return userMapper.findById(userId);
    }

    @Transactional
    public int save(long number,String name,int age,int sex) {

        User user = new User(number,name,age,sex);
        user.applyCreateEvent();

        return userMapper.insert(user);
    }
}

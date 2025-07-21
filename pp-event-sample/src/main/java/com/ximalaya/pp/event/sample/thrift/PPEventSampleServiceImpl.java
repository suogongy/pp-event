package org.ppj.pp.event.sample.thrift;

import com.alibaba.fastjson.JSON;
import org.ppj.eros.mainstay.context.annotation.MainstayServer;
import org.ppj.mainstay.rpc.thrift.TException;
import org.ppj.pp.event.core.threadcontext.XdcsTool;
import org.ppj.pp.event.sample.domain.entity.User;
import org.ppj.pp.event.sample.mapper.UserMapper;
import org.ppj.pp.event.sample.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@MainstayServer(group = "pp-event-sample")
public class PPEventSampleServiceImpl implements PPEventSampleService.Iface {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Override
    public CommonResponse findUserById(long userId) throws TException {

        User user = userMapper.findById(userId);

        return new CommonResponse.Builder()
                .setCode(200)
                .setMessage("success")
                .setResult(Objects.nonNull(user) ? JSON.toJSONString(user) : "none")
                .build();
    }

    @Override
    public CommonResponse addUser(long number, String name, int age, int sex) throws TException {
        int save = userService.save(number, name, age, sex);
        return new CommonResponse.Builder()
                .setCode(200)
                .setMessage("success")
                .setResult(save+"")
                .build();
    }
}

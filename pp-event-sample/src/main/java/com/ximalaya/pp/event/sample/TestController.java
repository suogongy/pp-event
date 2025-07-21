package org.ppj.pp.event.sample;

import org.ppj.pp.event.sample.service.UserService;
import org.ppj.pp.event.sample.vo.RequestUserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public Object home() {
        return userService.findById(96l);
    }

    @PostMapping("/save")
    public Object insert(@RequestBody RequestUserVo requestUserVo){

        return userService.save(requestUserVo.getNumber(),requestUserVo.getName(),requestUserVo.getAge(),requestUserVo.getSex());
    }

}

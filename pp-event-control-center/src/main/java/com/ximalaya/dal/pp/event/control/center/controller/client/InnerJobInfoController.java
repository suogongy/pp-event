package org.ppj.dal.pp.event.control.center.controller.client;

import org.ppj.dal.pp.event.control.center.controller.annotation.PermissionLimit;
import org.ppj.dal.pp.event.control.center.core.model.XxlJobInfo;
import org.ppj.dal.pp.event.control.center.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/inner/jobinfo")
public class InnerJobInfoController {
    private static Logger logger = LoggerFactory.getLogger(InnerJobInfoController.class);

    @Resource
    private XxlJobService xxlJobService;

    @RequestMapping("/register")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> register(@RequestBody XxlJobInfo jobInfo) {
        return xxlJobService.save(jobInfo);
    }
}

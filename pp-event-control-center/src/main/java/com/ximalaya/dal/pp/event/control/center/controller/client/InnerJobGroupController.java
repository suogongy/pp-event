package org.ppj.dal.pp.event.control.center.controller.client;

import org.ppj.dal.pp.event.control.center.controller.annotation.PermissionLimit;
import org.ppj.dal.pp.event.control.center.core.model.XxlJobGroup;
import org.ppj.dal.pp.event.control.center.dao.XxlJobGroupDao;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * job group controller
 *
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/inner/jobgroup")
public class InnerJobGroupController {

    @Resource
    public XxlJobGroupDao xxlJobGroupDao;

    @PostMapping("/save")
    @ResponseBody
    @PermissionLimit(limit = false)
    public ReturnT<String> save(@RequestBody XxlJobGroup xxlJobGroup) {

        // valid
        if (xxlJobGroup.getAppname() == null || xxlJobGroup.getAppname().trim().length() == 0) {
            return new ReturnT<String>(500, "请输入AppName");
        }

        XxlJobGroup foundXxlJobGroup = xxlJobGroupDao.findByAppname(xxlJobGroup.getAppname());

        if (Objects.nonNull(foundXxlJobGroup)) {
            return new ReturnT<String>(500, "执行器已存在");
        }

        if (xxlJobGroup.getAppname().length() < 4 || xxlJobGroup.getAppname().length() > 100) {
            return new ReturnT<String>(500, "AppName长度限制为4~100");
        }
        if (xxlJobGroup.getAppname().contains(">") || xxlJobGroup.getAppname().contains("<")) {
            return new ReturnT<String>(500, "AppName非法");
        }

        if (xxlJobGroup.getTitle().contains(">") || xxlJobGroup.getTitle().contains("<")) {
            return new ReturnT<String>(500, "名称非法");
        }

        // process
        xxlJobGroup.setUpdateTime(new Date());

        int ret = xxlJobGroupDao.save(xxlJobGroup);
        return (ret > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
    }
}

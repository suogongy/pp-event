package org.ppj.dal.pp.event.control.center.controller;

import org.ppj.dal.pp.event.control.center.core.exception.XxlJobException;
import org.ppj.dal.pp.event.control.center.core.model.XxlJobGroup;
import org.ppj.dal.pp.event.control.center.core.model.XxlJobUser;
import org.ppj.dal.pp.event.control.center.core.route.ExecutorRouteStrategyEnum;
import org.ppj.dal.pp.event.control.center.core.scheduler.MisfireStrategyEnum;
import org.ppj.dal.pp.event.control.center.core.scheduler.ScheduleTypeEnum;
import org.ppj.dal.pp.event.control.center.core.util.I18nUtil;
import org.ppj.dal.pp.event.control.center.dao.XxlJobGroupDao;
import org.ppj.dal.pp.event.control.center.service.LoginService;
import org.ppj.dal.pp.event.control.center.service.PPEventService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/eventinfo")
public class EventInfoController {

    @Resource
    private XxlJobGroupDao xxlJobGroupDao;

    @Resource
    private PPEventService ppEventService;

    public static List<XxlJobGroup> filterJobGroupByRole(HttpServletRequest request, List<XxlJobGroup> jobGroupList_all) {
        List<XxlJobGroup> jobGroupList = new ArrayList<>();
        if (jobGroupList_all != null && jobGroupList_all.size() > 0) {
            XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
            if (loginUser.getRole() == 1) {
                jobGroupList = jobGroupList_all;
            } else {
                List<String> groupIdStrs = new ArrayList<>();
                if (loginUser.getPermission() != null && loginUser.getPermission().trim().length() > 0) {
                    groupIdStrs = Arrays.asList(loginUser.getPermission().trim().split(","));
                }
                for (XxlJobGroup groupItem : jobGroupList_all) {
                    if (groupIdStrs.contains(String.valueOf(groupItem.getId()))) {
                        jobGroupList.add(groupItem);
                    }
                }
            }
        }
        return jobGroupList;
    }

    @RequestMapping
    public String index(HttpServletRequest request, Model model, @RequestParam(required = false, defaultValue = "-1") int jobGroup) {

        // 枚举-字典
        model.addAttribute("ExecutorRouteStrategyEnum", ExecutorRouteStrategyEnum.values());        // 路由策略-列表
        model.addAttribute("GlueTypeEnum", GlueTypeEnum.values());                                // Glue类型-字典
        model.addAttribute("ExecutorBlockStrategyEnum", ExecutorBlockStrategyEnum.values());        // 阻塞处理策略-字典
        model.addAttribute("ScheduleTypeEnum", ScheduleTypeEnum.values());                        // 调度类型
        model.addAttribute("MisfireStrategyEnum", MisfireStrategyEnum.values());                    // 调度过期策略

        // 执行器列表
        List<XxlJobGroup> jobGroupList_all = xxlJobGroupDao.findAll();

        // filter group
        List<XxlJobGroup> jobGroupList = filterJobGroupByRole(request, jobGroupList_all);
        if (jobGroupList == null || jobGroupList.size() == 0) {
            throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
        }

        model.addAttribute("JobGroupList", jobGroupList);
        model.addAttribute("jobGroup", jobGroup);

        return "eventinfo/eventinfo.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        int jobGroup) {

        XxlJobGroup xxlJobGroup = xxlJobGroupDao.load(jobGroup);

        return ppEventService.pageList(xxlJobGroup.getAppname(), start,length);
    }

    @PutMapping("/event/reset")
    @ResponseBody
    public ReturnT<String> reset(int jobGroup, long eventId) {

        XxlJobGroup xxlJobGroup = xxlJobGroupDao.load(jobGroup);

        return xiEventService.failedEventReset(xxlJobGroup.getAppname(), eventId);
    }

    @PutMapping("/event/reset/all")
    @ResponseBody
    public ReturnT<String> reset(int jobGroup) {

        XxlJobGroup xxlJobGroup = xxlJobGroupDao.load(jobGroup);

        return xiEventService.resetAll(xxlJobGroup.getAppname());
    }

    @PutMapping("/failedevent/remove")
    @ResponseBody
    public ReturnT<String> failedEventRemove(int jobGroup, long eventId) {

        XxlJobGroup xxlJobGroup = xxlJobGroupDao.load(jobGroup);

        return xiEventService.failedEventRemove(xxlJobGroup.getAppname(), eventId);
    }
}

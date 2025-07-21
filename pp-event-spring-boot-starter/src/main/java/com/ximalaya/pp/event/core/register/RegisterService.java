package org.ppj.pp.event.core.register;

import org.ppj.pp.event.core.configure.PPEventProperties;
import org.ppj.pp.event.core.http.JobRegisterApi;
import org.ppj.pp.event.core.job.FailedEventWarnJob;
import org.ppj.pp.event.core.utils.HttpUtil;
import org.ppj.pp.event.core.xxljob.model.XxlJobGroup;
import org.ppj.pp.event.core.xxljob.model.XxlJobInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Date;

public class RegisterService {

    public static final long JOB_PRE_READ_MS = 5000;
    private static final Logger LOGGER = LoggerFactory.getLogger(FailedEventWarnJob.class);

    @Value("${spring.profiles.active}")
    private String env;

    @Autowired
    private PPEventProperties PPEventProperties;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${PPEvent.job.group.title:}")
    private String jobGroupTitle;

    @Value("${PPEvent.job.author:system}")
    private String jobAuthor;

    @Autowired(required = false)
    private JobRegisterApi httpApi;

    public void initJob() {

        saveJobGroup();

        saveEventHandleRecoverJobInfo();

        saveEventFailedWarnJobInfo();
    }

    public Object saveJobGroup() {

        XxlJobGroup xxlJobGroup = buildXxlJobGroup();

        Call<Object> saveJobGroupCall = httpApi.saveJobGroup(HttpUtil.getXxlAdminUrl(env) + "/inner/jobgroup/save", xxlJobGroup);

        Response<Object> response = null;
        try {
            response = saveJobGroupCall.execute();
            Object body = response.body();
            LOGGER.info("xxl-job group save. result: {}", body);
            return body;
        } catch (IOException e) {
            throw new RuntimeException("saveJobGroup error", e);
        }
    }

    public Object saveEventHandleRecoverJobInfo() {

        XxlJobInfo xxlJobInfo = buildEventHandleRecoverJobInfo();

        Call<Object> saveJobInfoCall = httpApi.registerJobInfo(HttpUtil.getXxlAdminUrl(env) + "/inner/jobinfo/register", xxlJobInfo);

        Response<Object> response = null;
        try {
            response = saveJobInfoCall.execute();
            Object body = response.body();
            LOGGER.info("saveEventHandleRecoverJobInfo. result: {}", body);
            return body;
        } catch (IOException e) {
            throw new RuntimeException("saveEventHandleRecoverJobInfo error", e);
        }
    }

    public Object saveEventFailedWarnJobInfo() {

        XxlJobInfo xxlJobInfo = buildEventFailedWarnJobInfo();

        Call<Object> saveJobInfoCall = httpApi.registerJobInfo(HttpUtil.getXxlAdminUrl(env) + "/inner/jobinfo/register", xxlJobInfo);

        Response<Object> response = null;
        try {
            response = saveJobInfoCall.execute();
            Object body = response.body();
            LOGGER.info("saveEventFailedWarnJobInfo. result: {}", body);
            return body;
        } catch (IOException e) {
            throw new RuntimeException("saveEventFailedWarnJobInfo error", e);
        }
    }

    private XxlJobInfo buildEventHandleRecoverJobInfo() {

        XxlJobInfo xxlJobInfo = new XxlJobInfo();

        long jobScheduleConfig = PPEventProperties.getRecoverJobPeriodInSeconds();

        xxlJobInfo.setAppname(applicationName);
        xxlJobInfo.setAuthor(jobAuthor);
        xxlJobInfo.setExecutorHandler("eventHandleRecoverJobHandler");
        xxlJobInfo.setJobDesc("事件异步重试job");
        xxlJobInfo.setScheduleConf(jobScheduleConfig + "");
        xxlJobInfo.setTriggerNextTime(buildTriggerNextTime(jobScheduleConfig));
        return xxlJobInfo;
    }

    private XxlJobInfo buildEventFailedWarnJobInfo() {

        XxlJobInfo xxlJobInfo = new XxlJobInfo();

        long jobScheduleConfig = PPEventProperties.getFailedEventWarnJobPeriodInSeconds();

        xxlJobInfo.setAppname(applicationName);
        xxlJobInfo.setAuthor(jobAuthor);
        xxlJobInfo.setExecutorHandler("failedEventWarnJobHandler");
        xxlJobInfo.setJobDesc("失败事件告警job");
        xxlJobInfo.setScheduleConf(jobScheduleConfig + "");
        xxlJobInfo.setTriggerNextTime(buildTriggerNextTime(jobScheduleConfig));

        return xxlJobInfo;
    }

    private XxlJobGroup buildXxlJobGroup() {

        XxlJobGroup xxlJobGroup = new XxlJobGroup();

        xxlJobGroup.setAppname(applicationName);

        String finalJobGroupTitle = applicationName;

        if (StringUtils.isNotEmpty(jobGroupTitle)) {
            finalJobGroupTitle = jobGroupTitle;
        }

        xxlJobGroup.setTitle(finalJobGroupTitle);

        return xxlJobGroup;
    }

    public Long buildTriggerNextTime(long scheduleConfig) {

        Date fromTime = new Date(System.currentTimeMillis() + JOB_PRE_READ_MS);

        Date nextTriggerDate = new Date(fromTime.getTime() + scheduleConfig * 1000);

        return nextTriggerDate.getTime();
    }
}

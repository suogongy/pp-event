package org.ppj.pp.event.core.job;

import org.ppj.pp.event.core.configure.PPEventProperties;
import org.ppj.pp.event.core.entity.PPEvent;
import org.ppj.pp.event.core.http.DingDingAlertApi;
import org.ppj.pp.event.core.mapper.PPEventMapper;
import org.ppj.pp.event.core.threadcontext.ThreadContextSynchronizationManager;
import org.ppj.pp.event.core.utils.HttpUtil;
import org.ppj.pp.event.core.xxljob.context.XxlJobHelper;
import org.ppj.pp.event.core.xxljob.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FailedEventWarnJob {

    private static final String FAILED_EVENT_WARN_MSG_TEMPLATE = "有失败的PP事件报警，请尽快处理。详情查阅PP事件调度中心: %s";
    private static final String DINGDING_ALERT_FROM = "pp-event";

    private static final Logger LOGGER = LoggerFactory.getLogger(FailedEventWarnJob.class);
    @Autowired
    private PPEventProperties PPEventProperties;
    @Resource
    private PPEventMapper ppEventMapper;

    @Resource
    private DingDingAlertApi xdcsDingDingAlertApi;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active}")
    private String env;

    @XxlJob("failedEventWarnJobHandler")
    public void warn() {

        doEventFailedWarn();

        XxlJobHelper.log("app: {}, jobHandler: {}", applicationName, "failedEventWarnJobHandler");
    }

    private void doEventFailedWarn() {

        LOGGER.info("pp-event failed warn job called!");

        long preId = 0l;

        boolean needSendDingAlert = false;

        while (true) {
            List<PPEvent> failedPPEvents = ppEventMapper.findFailedEventsByPreIdWithPaging(preId, PPEventProperties.getPageSize());

            if (!CollectionUtils.isEmpty(failedPPEvents)) {
                needSendDingAlert = true;
            }

            for (PPEvent ppEvent : failedPPEvents) {
                LOGGER.error("failed event need handle. event info: {}", ppEvent.getRawMethodInvocationContent());
            }

            if (!Objects.equals(failedPPEvents.size(), PPEventProperties.getPageSize())) {
                if (needSendDingAlert) {
                    sendDingDingAlert();
                }
                return;
            }
            preId = failedPPEvents.get(failedPPEvents.size() - 1).getId();
        }
    }

    private void sendDingDingAlert() {

        Call<Object> alertCall = xdcsDingDingAlertApi.alert(buildAlertBaseUrl(), DINGDING_ALERT_FROM, buildAlertMsg(), applicationName, "");

        Response<Object> response = null;
        try {
            response = alertCall.execute();
            Object body = response.body();
            LOGGER.info("failed alert by dingding. result: {}", body);

        } catch (IOException e) {
            throw new RuntimeException("dingAlert error", e);
        }
    }

    private String buildAlertBaseUrl() {

        String alertUrl = "http://your_test_alert_api/alert/ding";

        if (Objects.equals("prd", env) || Objects.equals("pro", env)) {
            alertUrl = "http://your_alert_api/alert/ding";
        }

        return alertUrl;
    }

    private String buildAlertMsg() {

        return String.format(FAILED_EVENT_WARN_MSG_TEMPLATE, HttpUtil.getPPEventAdminAddress(env));
    }
}

package org.ppj.pp.event.core.configure;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitScan;
import org.ppj.pp.event.core.controller.PPEventController;
import org.ppj.pp.event.core.controller.XxlJobClientController;
import org.ppj.pp.event.core.eventhandle.PPEventHandler;
import org.ppj.pp.event.core.initializer.PPEventFrameworkInitializer;
import org.ppj.pp.event.core.job.EventHandleRecoverJob;
import org.ppj.pp.event.core.job.FailedEventWarnJob;
import org.ppj.pp.event.core.processor.AnnotationEventListenerBeanPostProcessor;
import org.ppj.pp.event.core.processor.EventMethodProcessor;
import org.ppj.pp.event.core.register.RegisterService;
import org.ppj.pp.event.core.utils.HttpUtil;
import org.ppj.pp.event.core.utils.NetUtil;
import org.ppj.pp.event.core.xxljob.executor.impl.XxlJobSpringExecutor;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PPEventProperties.class)
@MapperScan("org.ppj.pp.event.core.mapper")
@RetrofitScan("org.ppj.pp.event.core.http")
public class PPEventAutoConfiguration {

    public static final String JOB_LOG_PATH_TEMPLATE = "%s/%s/%s";
    private static final int JOB_LOG_RETENTION_DAYS = 7;
    private final Logger LOGGER = LoggerFactory.getLogger(PPEventAutoConfiguration.class);

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${ppevent.job.accessToken:}")
    private String jobAccessToken;

    @Value("${server.port}")
    private int serverPort;

    @Value("${server.servlet.context-path:/}")
    private String servletContext;

    @Value("${spring.profiles.active}")
    private String env;

    @Bean
    public AnnotationEventListenerBeanPostProcessor annotationEventListenerBeanPostProcessor() {
        AnnotationEventListenerBeanPostProcessor annotationEventListenerBeanPostProcessor = new AnnotationEventListenerBeanPostProcessor();
        return annotationEventListenerBeanPostProcessor;
    }

    
    @Bean
    public EventMethodProcessor eventMethodProcessor() {
        EventMethodProcessor eventMethodProcessor = new EventMethodProcessor();
        return eventMethodProcessor;
    }

    @Bean
    public PPEventHandler eventHandler() {
        return new PPEventHandler();
    }

    @Bean
    public EventHandleRecoverJob eventHandleRecoverJob() {
        EventHandleRecoverJob eventHandleRecoverJob = new EventHandleRecoverJob();
        return eventHandleRecoverJob;
    }

    @Bean
    public FailedEventWarnJob failedEventWarnJob() {
        FailedEventWarnJob failedEventWarnJob = new FailedEventWarnJob();
        return failedEventWarnJob;
    }

    @Bean
    public PPEventFrameworkInitializer ppEventFrameworkInitializer() {
        PPEventFrameworkInitializer ppEventFrameworkInitializer = new PPEventFrameworkInitializer();
        return ppEventFrameworkInitializer;
    }

    @Bean
    public RegisterService registerService() {
        return new RegisterService();
    }

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        LOGGER.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(HttpUtil.getXxlAdminUrl(env));
        xxlJobSpringExecutor.setAppname(applicationName);
        xxlJobSpringExecutor.setIp(NetUtil.getLocalIp());
        xxlJobSpringExecutor.setPort(serverPort);
        xxlJobSpringExecutor.setServletContext(servletContext);
        xxlJobSpringExecutor.setAccessToken(jobAccessToken);

        xxlJobSpringExecutor.setLogPath(buildJobLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(JOB_LOG_RETENTION_DAYS);

        return xxlJobSpringExecutor;
    }

    @Bean
    public XxlJobClientController xxlJobController() {
        return new XxlJobClientController();
    }

    @Bean
    public PPEventController ppEventController() {
        return new PPEventController();
    }

    private String buildJobLogPath() {

        String jobLogBase;

        switch (env) {
            case "dev":
                jobLogBase = "/var/logs";
                break;
            case "test":
            case "fat":
                jobLogBase = "/logs";
                break;
            case "uat":
                jobLogBase = "/var/log";
                break;
            case "prd":
            case "pro":
                jobLogBase = "/var/log";
                break;
            default:
                throw new RuntimeException("buildJobLogPath failed. env is illegal");
        }

        return String.format(JOB_LOG_PATH_TEMPLATE, jobLogBase, applicationName, "pp-event/jobhandler");
    }

}


package org.ppj.pp.event.core.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ppevent")
public class PPEventProperties {

    private int pageSize = 100;
    private int retryThreshold = 36;
    private int recoverJobPeriodInSeconds = 30;
    private int failedEventWarnJobPeriodInSeconds = 120;
}

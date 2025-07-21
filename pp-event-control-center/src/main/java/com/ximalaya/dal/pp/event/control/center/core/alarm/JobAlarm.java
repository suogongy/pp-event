package org.ppj.dal.pp.event.control.center.core.alarm;

import org.ppj.dal.pp.event.control.center.core.model.XxlJobInfo;
import org.ppj.dal.pp.event.control.center.core.model.XxlJobLog;

/**
 * @author xuxueli 2020-01-19
 */
public interface JobAlarm {

    /**
     * job alarm
     *
     * @param info
     * @param jobLog
     * @return
     */
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog);

}

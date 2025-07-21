package org.ppj.dal.pp.event.control.center.service;


import com.xxl.job.core.biz.model.ReturnT;

import java.util.Map;

public interface PPEventService {

    Map<String, Object> pageList(String appname, long preId,int pageSize);

    ReturnT<String> failedEventReset(String appname, long eventId);

    ReturnT<String> failedEventRemove(String appname, long eventId);

    ReturnT<String> resetAll(String appname);
}

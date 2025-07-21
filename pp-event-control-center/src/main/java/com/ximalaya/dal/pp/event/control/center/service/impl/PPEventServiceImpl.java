package org.ppj.dal.pp.event.control.center.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.ppj.dal.pp.event.control.center.core.model.XxlJobGroup;
import org.ppj.dal.pp.event.control.center.dao.XxlJobGroupDao;
import org.ppj.dal.pp.event.control.center.http.BusinessHttpApi;
import org.ppj.dal.pp.event.control.center.http.vo.PageVo;
import org.ppj.dal.pp.event.control.center.http.vo.PPEventVo;
import org.ppj.dal.pp.event.control.center.service.PPEventService;
import com.xxl.job.core.biz.model.ReturnT;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class PPEventServiceImpl implements PPEventService {

    @Resource
    private BusinessHttpApi businessHttpApi;

    @Resource
    private XxlJobGroupDao xxlJobGroupDao;

    @Override
    public Map<String, Object> pageList(String appname, long offset, int pageSize) {


        // page list
        PageVo<PPEventVo> pageResult = buildPPEventVos(appname, offset, pageSize);

        int list_count = pageResult.getTotalCount();

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);        // 总记录数
        maps.put("recordsFiltered", list_count);     // 过滤后的总记录数
        maps.put("data", pageResult.getPageList());  // 分页列表
        return maps;
    }

    @Override
    public ReturnT<String> failedEventReset(String appname, long eventId) {

        XxlJobGroup xxlJobGroup = xxlJobGroupDao.findByAppname(appname);

        String baseUrl = xxlJobGroup.getRegistryList().get(0);

        Call<ReturnT<String>> resetEventCall = businessHttpApi.failedEventReset(baseUrl + "event/reset", eventId);

        Response<ReturnT<String>> response = null;
        try {
            response = resetEventCall.execute();
            ReturnT<String> body = response.body();
            log.info("failedEventReset. result: {}", body.getContent());
            return body;
        } catch (IOException e) {
            throw new RuntimeException("failedEventReset error", e);
        }
    }

    @Override
    public ReturnT<String> failedEventRemove(String appname, long eventId) {

        XxlJobGroup xxlJobGroup = xxlJobGroupDao.findByAppname(appname);

        String baseUrl = xxlJobGroup.getRegistryList().get(0);

        Call<ReturnT<String>> removeEventCall = businessHttpApi.failedEventRemove(baseUrl + "failedevent/remove", eventId);

        Response<ReturnT<String>> response = null;
        try {
            response = removeEventCall.execute();
            ReturnT<String> body = response.body();
            log.info("failedEventRemove. result: {}", body.getContent());
            return body;
        } catch (IOException e) {
            throw new RuntimeException("failedEventRemove error", e);
        }
    }

    @Override
    public ReturnT<String> resetAll(String appname) {

        XxlJobGroup xxlJobGroup = xxlJobGroupDao.findByAppname(appname);

        if (Objects.isNull(xxlJobGroup) || CollectionUtils.isEmpty(xxlJobGroup.getRegistryList())) {
            return new ReturnT<>(500,"非活跃的执行器无法重置");
        }

        String baseUrl = xxlJobGroup.getRegistryList().get(0);

        Call<ReturnT<String>> resetAllEventCall = businessHttpApi.resetAllFailedEvent(baseUrl + "event/reset/all");

        Response<ReturnT<String>> response = null;
        try {
            response = resetAllEventCall.execute();
            ReturnT<String> body = response.body();
            log.info("resetAll. result: {}", body.getContent());
            return body;
        } catch (IOException e) {
            throw new RuntimeException("resetAll error", e);
        }
    }

    private PageVo<PPEventVo> buildPPEventVos(String appName, long offset, int pageSize) {

        XxlJobGroup xxlJobGroup = xxlJobGroupDao.findByAppname(appName);

        if (Objects.isNull(xxlJobGroup) || CollectionUtils.isEmpty(xxlJobGroup.getRegistryList())) {
            return new PageVo<>(Lists.newArrayList(), 0);
        }

        String baseUrl = xxlJobGroup.getRegistryList().get(0);

        Call<PageVo<PPEventVo>> eventListQueryCall = businessHttpApi.getEventPageList(baseUrl + "event", offset, pageSize);

        Response<PageVo<PPEventVo>> response = null;
        try {
            response = eventListQueryCall.execute();
            PageVo<PPEventVo> body = response.body();
            log.info("xxl-job group save. result: {}", JSON.toJSONString(body));

            List<PPEventVo> PPEventVos = body.getPageList();

            PPEventVos.stream().forEach(PPEventVo -> PPEventVo.setJobGroup(xxlJobGroup.getId()));

            return body;

        } catch (IOException e) {
            throw new RuntimeException("getEventPageByPreId error", e);
        }
    }
}

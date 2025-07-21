package org.ppj.pp.event.core.controller;

import org.ppj.pp.event.core.configure.PPEventProperties;
import org.ppj.pp.event.core.controller.vo.PageVo;
import org.ppj.pp.event.core.controller.vo.PPEventVo;
import org.ppj.pp.event.core.entity.PPEvent;
import org.ppj.pp.event.core.enums.PPEventStatus;
import org.ppj.pp.event.core.mapper.PPEventMapper;
import org.ppj.pp.event.core.xxljob.biz.model.ReturnT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
public class PPEventController {

    @Autowired
    private PPEventProperties PPEventProperties;

    @Resource
    private PPEventMapper ppEventMapper;

    @GetMapping("/event")
    public PageVo<PPEventVo> queryEvents(@RequestParam long offset, @RequestParam int pageSize) {

        List<PPEvent> ppEvents = ppEventMapper.findWithPaging(offset, pageSize);

        int totalCount = ppEventMapper.count();

        return new PageVo(buildPPEventVos(ppEvents), totalCount);
    }

    @PutMapping("/event/reset")
    public ReturnT<String> resetEvent(@RequestParam long eventId) {

        PPEvent ppEvent = ppEventMapper.findById(eventId);

        ppEvent.reset();

        ppEventMapper.update(ppEvent);

        return ReturnT.SUCCESS;
    }

    @PutMapping("/event/reset/all")
    public ReturnT<String> resetAllFailedEvent() {

        CompletableFuture.runAsync(() -> doResetAllFailedEvents());

        return ReturnT.SUCCESS;
    }

    /**
     * 失败事件删除，
     *
     * @param eventId
     * @return
     */
    @PutMapping("/failedevent/remove")
    public ReturnT<String> removeFailedEvent(@RequestParam long eventId) {

        PPEvent ppEvent = ppEventMapper.findById(eventId);

        if (!Objects.equals(ppEvent.getStatus(), PPEventStatus.FAILED.getId())) {
            return ReturnT.FAIL;
        }

        ppEventMapper.delete(eventId);

        return ReturnT.SUCCESS;
    }

    private List<PPEventVo> buildPPEventVos(List<PPEvent> ppEvents) {

        return ppEvents.stream().map(ppEvent -> new PPEventVo(
                ppEvent.getId(),
                ppEvent.getEventNo(),
                PPEventStatus.getDescById(ppEvent.getStatus()),
                ppEvent.getRetriedCount(),
                ppEvent.getRawMethodInvocationContent()
        )).collect(Collectors.toList());
    }

    private void doResetAllFailedEvents() {

        long preId = 0l;

        while (true) {

            List<PPEvent> failedPPEvents = ppEventMapper.findFailedEventsByPreIdWithPaging(preId, PPEventProperties.getPageSize());

            if (CollectionUtils.isEmpty(failedPPEvents)) {
                return;
            }

            for (PPEvent ppEvent : failedPPEvents) {
                ppEvent.reset();
            }

            ppEventMapper.batchUpdate(failedPPEvents);

            if (!Objects.equals(failedPPEvents.size(), PPEventProperties.getPageSize())) {
                return;
            }
            preId = failedPPEvents.get(failedPPEvents.size() - 1).getId();
        }
    }
}

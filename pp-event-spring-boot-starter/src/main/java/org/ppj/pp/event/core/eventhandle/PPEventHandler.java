package org.ppj.pp.event.core.eventhandle;

import org.ppj.pp.event.core.entity.PPEvent;
import org.ppj.pp.event.core.mapper.PPEventMapper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

public class PPEventHandler {

    @Resource
    private PPEventMapper ppEventMapper;

    @Transactional(rollbackFor = Exception.class)
    public void handleEvent(PPEvent ppEvent) {

        if (Objects.nonNull(ppEvent) && ppEvent.isTrying()) {

            ppEvent.markAsDoing();
            boolean deleteSuccess = false;
            try {
                if (ppEventMapper.update(ppEvent) > 0) {

                    if (ppEvent.getMethodInvocation().proceed()) {
                        deleteSuccess = ppEventMapper.delete(ppEvent.getId()) > 0;
                    }

                } else {
                    /**
                     * 若更新失败，则事件在db中还是trying状态，
                     * 此处的标记，结合finally里的判断，可减少一次db更新操作
                     */
                    ppEvent.resetAsTrying();
                }
            } finally {
                if (deleteSuccess || ppEvent.isTrying()) {
                    return;
                }
                ppEvent.resetAsTrying();
                ppEventMapper.update(ppEvent);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleEvent(Long ppEventId) {

        PPEvent ppEvent = ppEventMapper.findById(ppEventId);

        handleEvent(ppEvent);
    }
}

package org.ppj.pp.event.core.job;

import org.ppj.pp.event.core.configure.PPEventProperties;
import org.ppj.pp.event.core.entity.PPEvent;
import org.ppj.pp.event.core.enums.PPEventStatus;
import org.ppj.pp.event.core.mapper.PPEventMapper;
import org.ppj.pp.event.core.processor.EventMethodProcessor;
import org.ppj.pp.event.core.threadcontext.ThreadContextSynchronizationManager;
import org.ppj.pp.event.core.xxljob.context.XxlJobHelper;
import org.ppj.pp.event.core.xxljob.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class EventHandleRecoverJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandleRecoverJob.class);

    private int threadCount = Runtime.getRuntime().availableProcessors();

    @Autowired
    private PPEventProperties PPEventProperties;
    @Resource
    private PPEventMapper ppEventMapper;
    @Autowired
    private EventMethodProcessor eventMethodProcessor;

    @Value("${spring.application.name}")
    private String applicationName;

    private ExecutorService executorService;

    @XxlJob("eventHandleRecoverJobHandler")
    public void eventHandleRecoverJobHandler() {

        ensureExecutorInitialized(threadCount);

        CompletableFuture.runAsync(() -> doRecoverEvent(false));
        CompletableFuture.runAsync(() -> doRecoverEvent(true));

        XxlJobHelper.log("app: {}, jobHandler: {}", applicationName, "eventHandleRecoverJobHandler");
    }

    private void ensureExecutorInitialized(int threadCount) {
        if (Objects.isNull(executorService)) {
            synchronized (EventHandleRecoverJob.class) {
                if (Objects.isNull(executorService)) {
                    executorService = new ThreadPoolExecutor(threadCount, threadCount, 0l, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10000));
                }
            }
        }
    }

    private void doRecoverEvent(boolean isPeakRecover) {
        LOGGER.info("pp-event retry job called! isPeakRecover: {}", isPeakRecover);

        ThreadContextSynchronizationManager threadContextSynchronizationManager = new ThreadContextSynchronizationManager(isPeakRecover);

        threadContextSynchronizationManager.executeWithBindThreadContext(() -> {

            long preId = 0l;
            long currentTimeMillis = System.currentTimeMillis();
            Date createTimeThreshold = new Date(currentTimeMillis - 1000l * PPEventProperties.getRecoverJobPeriodInSeconds());

            while (true) {
                List<PPEvent> ppEvents = ppEventMapper.findByStatusAndPreIdAndCreateTimeThresholdWithPaging(PPEventStatus.TRYING.getId(), preId, createTimeThreshold, PPEventProperties.getPageSize());

                if (!CollectionUtils.isEmpty(ppEvents)) {
                    concurrentRecover(ppEvents, isPeakRecover);
                }

                if (!Objects.equals(ppEvents.size(), PPEventProperties.getPageSize())) {
                    return;
                }
                preId = ppEvents.get(ppEvents.size() - 1).getId();
            }
        });
    }

    private void concurrentRecover(List<PPEvent> ppEvents, boolean isPeakRecover) {

        List<RecoverTask> recoverTasks = ppEvents.stream()
                .map(ppEvent -> new RecoverTask(ppEvent, isPeakRecover))
                .collect(Collectors.toList());

        try {
            List<Future<Void>> futures = executorService.invokeAll(recoverTasks, 10l, TimeUnit.SECONDS);

            for (Future future : futures) {
                future.get();
            }
        } catch (Exception ex) {
            LOGGER.error("error caught when concurrentRecover", ex);
        }
    }

    class RecoverTask implements Callable<Void> {

        private PPEvent ppEvent;
        private boolean isPeakRelatedTask;

        public RecoverTask(PPEvent ppEvent, boolean isPeakRelatedTask) {
            this.ppEvent = ppEvent;
            this.isPeakRelatedTask = isPeakRelatedTask;
        }

        @Override
        public Void call() throws Exception {

            ThreadContextSynchronizationManager threadContextSynchronizationManager = new ThreadContextSynchronizationManager(isPeakRelatedTask);

            threadContextSynchronizationManager.executeWithBindThreadContext(() -> {
                eventMethodProcessor.handle(ppEvent);
            });
            return null;
        }
    }
}

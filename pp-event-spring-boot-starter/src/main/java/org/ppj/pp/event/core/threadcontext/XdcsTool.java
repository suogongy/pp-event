package org.ppj.pp.event.core.threadcontext;

import org.ppj.common.model.XmlySpan;
import org.ppj.xdcs.client.config.AppTraceConfig;
import org.ppj.xdcs.client.service.TraceGenerater2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Agg 支持调用链工具
 * <p>
 * 注： 强依赖 xdcs-client 0.0.20+版本
 *
 * @author caorong
 */
public class XdcsTool {
    private static Logger logger = LoggerFactory.getLogger(XdcsTool.class);

    /**
     * 从xdcs上下文获取peak头
     *
     * @return
     */
    public static String getPeakRequestVal() {
        try {
            if (TraceGenerater2.getTracer() != null && TraceGenerater2.getTracer().getParentSpan() != null &&
                    TraceGenerater2.getTracer().getParentSpan().getProps() != null) {
                Map restParam = TraceGenerater2.getTracer().getParentSpan().getProps();
                if (restParam != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getPeakRequestVal props = {}", restParam);
                    }
                    return (String) restParam.get("peak-request");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("not in xdcs context! getPeakRequestVal failed!");
                }
            }
        } catch (Throwable e) {
            logger.warn("check peak req error!", e);
        }
        return null;
    }

    /**
     * 将上下文获取peak头写入
     *
     * @return
     */
    public static void setPeakRequestVal(String val) {
        try {

            String peakValue = val == null ? "" : val;

            XmlySpan span1 =
                    TraceGenerater2.getTracer().newSpan(AppTraceConfig.getLocalConfig().getAppName(), null, null, null);
            Map restParam = span1.getProps();
            restParam.put("peak-request", peakValue);
            if (logger.isDebugEnabled()) {
                logger.debug("setPeakRequestVal props = {}", restParam);
            }
            TraceGenerater2.getTracer().setParentSpan(span1);
        } catch (Throwable e) {
            logger.warn("set peak req error!", e);
        }
    }

    /**
     * 清理peak头
     *
     * @return
     */
    public static void clearPeakRequestVal() {
        try {
            if (TraceGenerater2.getTracer() != null && TraceGenerater2.getTracer().getParentSpan() != null) {
                TraceGenerater2.getTracer().removeParentSpan();
                if (logger.isDebugEnabled()) {
                    logger.debug("clearPeakRequestVal success!");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("not in xdcs context! clearPeakRequestVal failed!");
                }
            }
        } catch (Throwable e) {
            logger.warn("clear peak req error!", e);
        }
    }
}

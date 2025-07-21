package org.ppj.pp.event.core.xxljob.enums;

/**
 * Created by xuxueli on 17/5/9.
 */
public enum ExecutorBlockStrategyEnum {

    SERIAL_EXECUTION("Serial execution"),
    /*CONCURRENT_EXECUTION("并行"),*/
    DISCARD_LATER("Discard Later"),
    COVER_EARLY("Cover Early");

    private String title;
    private ExecutorBlockStrategyEnum (String title) {
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public static org.ppj.pp.event.core.xxljob.enums.ExecutorBlockStrategyEnum match(String name, org.ppj.pp.event.core.xxljob.enums.ExecutorBlockStrategyEnum defaultItem) {
        if (name != null) {
            for (org.ppj.pp.event.core.xxljob.enums.ExecutorBlockStrategyEnum item: org.ppj.pp.event.core.xxljob.enums.ExecutorBlockStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}

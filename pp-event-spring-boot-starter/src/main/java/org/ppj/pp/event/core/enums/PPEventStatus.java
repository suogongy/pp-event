package org.ppj.pp.event.core.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum PPEventStatus {

    TRYING(1, "待处理"),
    DOING(2, "处理中"),
    FAILED(3, "已失败"),
    ;

    private static Map<Integer, PPEventStatus> idToPPEventStatusMap;

    static {
        idToPPEventStatusMap = Arrays.stream(PPEventStatus.values()).collect(Collectors.toMap(PPEventStatus::getId, Function.identity()));
    }

    private Integer id;
    private String desc;


    PPEventStatus(Integer id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public static PPEventStatus getById(Integer id) {
        return idToPPEventStatusMap.get(id);
    }

    public static String getDescById(Integer id) {
        return idToPPEventStatusMap.get(id).getDesc();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

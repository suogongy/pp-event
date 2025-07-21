package org.ppj.dal.pp.event.control.center.http.vo;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageVo<T> implements Serializable {

    public static final long serialVersionUID = 42L;
    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;
    public static final PageVo<String> SUCCESS = new PageVo(Lists.newArrayList(),0);
    public static final PageVo<String> FAIL = new PageVo(500, null);

    private int code;
    private String msg;
    private int totalCount;
    private List<T> pageList = Lists.newArrayList();

    public PageVo() {
    }

    public PageVo(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public PageVo(List<T> pageList,int totalCount) {
        this.code = 200;
        this.totalCount = totalCount;
        this.pageList = pageList;
    }
}

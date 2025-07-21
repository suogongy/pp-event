package org.ppj.dal.pp.event.control.center.http.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PPEventVo {

    private long id;
    private int jobGroup;
    private String eventNo;
    private String status;
    private int retriedCount;
    private String methodInvocationContent;
}

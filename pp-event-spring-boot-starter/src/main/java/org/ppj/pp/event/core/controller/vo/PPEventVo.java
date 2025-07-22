package org.ppj.pp.event.core.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PPEventVo {

    private long id;
    private String eventNo;
    private String status;
    private int retriedCount;
    private String methodInvocationContent;
}

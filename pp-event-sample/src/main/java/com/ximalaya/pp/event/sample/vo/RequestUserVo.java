package org.ppj.pp.event.sample.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestUserVo {

    private Long number;
    private String name;
    private Integer age;
    private Integer sex;
}

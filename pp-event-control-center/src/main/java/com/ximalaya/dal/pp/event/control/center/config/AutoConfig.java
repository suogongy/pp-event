package org.ppj.dal.pp.event.control.center.config;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@MapperScan("org.ppj.dal.pp.event.control.center.mapper")
@RetrofitScan("org.ppj.dal.pp.event.control.center.http")
public class AutoConfig {


}

package org.ppj.dal.pp.event.control.center;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Eros codegen
 */
@SpringBootApplication
@RetrofitScan("org.ppj.dal.pp.event.control.center.register")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

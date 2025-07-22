package org.ppj.pp.event.core.http;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import org.ppj.pp.event.core.xxljob.model.XxlJobGroup;
import org.ppj.pp.event.core.xxljob.model.XxlJobInfo;
import retrofit2.Call;
import retrofit2.http.*;

@RetrofitClient(baseUrl = "http://your_admin_center/pp-event-control-center")
public interface JobRegisterApi {

    @POST
    Call<Object> saveJobGroup(@Url String url,@Body XxlJobGroup xxlJobGroup);

    @POST
    Call<Object> registerJobInfo(@Url String url,@Body XxlJobInfo xxlJobInfo);
}

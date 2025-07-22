package org.ppj.pp.event.core.http;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

@RetrofitClient(baseUrl = "http://www.your_alter_api.com")
public interface DingDingAlertApi {

    @POST
    @FormUrlEncoded
    Call<Object> alert(@Url String url, @Field("from") String from, @Field("msg") String msg, @Field("appName") String appName, @Field("phoneNumbers") String phoneNumbers);
}

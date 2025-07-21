package org.ppj.dal.pp.event.control.center.http;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import org.ppj.dal.pp.event.control.center.http.vo.PageVo;
import org.ppj.dal.pp.event.control.center.http.vo.PPEventVo;
import com.xxl.job.core.biz.model.ReturnT;
import retrofit2.Call;
import retrofit2.http.*;

@RetrofitClient(baseUrl = "http://www.youradminweb.com")
public interface BusinessHttpApi {

    @GET
    Call<PageVo<PPEventVo>> getEventPageList(@Url String url, @Query("offset") long offset, @Query("pageSize") int pageSize);

    @PUT
    @FormUrlEncoded
    Call<ReturnT<String>> failedEventReset(@Url String url, @Field("eventId") long eventId);

    @PUT
    @FormUrlEncoded
    Call<ReturnT<String>> failedEventRemove(@Url String url, @Field("eventId") long eventId);

    @PUT
    Call<ReturnT<String>> resetAllFailedEvent(@Url String url);
}

package org.ppj.pp.event.core.mapper;

import org.ppj.pp.event.core.entity.PPEvent;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface PPEventMapper {

    @Select("SELECT * FROM XIMA_EVENT WHERE id = #{id}")
    @Results(id = "ppEventMap",value = {
            @Result(property = "eventNo", column = "event_no"),
            @Result(property = "status", column = "status"),
            @Result(property = "retriedCount", column = "retried_count"),
            @Result(property = "methodInvocationContent", column = "method_invocation_content"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "updateTime", column = "update_time"),
            @Result(property = "version", column = "version"),
    })
    PPEvent findById(@Param("id") long id);

    @Select({"<script>" +
            "SELECT * FROM XIMA_EVENT WHERE id in" +
            "<foreach item='item' index='index' collection='list' open='(' close=')' separator=','>" +
            "#{item}" +
            "</foreach>" +
            "</script>"})
    @ResultMap(value = "ppEventMap")
    List<PPEvent> findByIds(List<Long> eventIds);

    @Select("SELECT * FROM XIMA_EVENT WHERE status = #{status} AND id > #{preId} AND create_time < #{createTimeThreshold} ORDER BY ID ASC LIMIT #{pageSize}")
    @ResultMap(value = "ppEventMap")
    List<PPEvent> findByStatusAndPreIdAndCreateTimeThresholdWithPaging(@Param("status") int status, @Param("preId") long preId, @Param("createTimeThreshold") Date createTimeThreshold, @Param("pageSize") int pageSize);

    @Select("SELECT * FROM XIMA_EVENT WHERE id > 0 ORDER BY ID DESC LIMIT #{offset},#{pageSize}")
    @ResultMap(value = "ppEventMap")
    List<PPEvent> findWithPaging(@Param("offset") long offset, @Param("pageSize") int pageSize);

    @Select("SELECT * FROM XIMA_EVENT WHERE status = 3 AND id > #{preId} ORDER BY ID ASC LIMIT #{pageSize}")
    List<PPEvent> findFailedEventsByPreIdWithPaging(@Param("preId") long preId, @Param("pageSize") int pageSize);

    @Insert({"<script>" +
            "insert into XIMA_EVENT(event_no,status, retried_count,method_invocation_content,create_time,update_time,version) values " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.eventNo},#{item.status}, #{item.retriedCount},#{item.methodInvocationContent},#{item.createTime},#{item.updateTime},#{item.version})" +
            "</foreach>" +
            "</script>"})
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(List<PPEvent> ppEvents);

    @Update({"update XIMA_EVENT set status = #{status},retried_count=#{retriedCount},update_time = now(),version=#{version} where id=#{id} and version = #{version}-1"})
    int update(PPEvent ppEvent);

    @Delete("delete from XIMA_EVENT where id = #{id}")
    int delete(long id);

    @Update({"<script>" +
            "<foreach collection='ppEvents' item='item' index='index' separator=';'>",
            "update XIMA_EVENT " +
            "set status = #{item.status},retried_count=#{item.retriedCount},update_time = now(),version=#{item.version} where id=#{item.id} and version = #{item.version}-1",
            "</foreach>" +
            "</script>"}
    )
    void batchUpdate(@Param("ppEvents") List<PPEvent> ppEvents);

    @Select("SELECT COUNT(1) FROM XIMA_EVENT WHERE id > 0")
    int count();

}

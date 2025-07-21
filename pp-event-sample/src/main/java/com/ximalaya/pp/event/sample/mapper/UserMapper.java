package org.ppj.pp.event.sample.mapper;

import org.ppj.pp.event.sample.domain.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM USER WHERE id = #{userId}")
    User findById(@Param("userId") long userId);

    @Insert({"insert into USER(number, name,age,sex,join_time) values(#{number}, #{name},#{age},#{sex},#{joinTime})"})
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}

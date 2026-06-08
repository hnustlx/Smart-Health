package com.smarthealth.mapper;

import com.smarthealth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByUsername(@Param("username") String username);

    List<User> findAll();

    int insert(User user);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}

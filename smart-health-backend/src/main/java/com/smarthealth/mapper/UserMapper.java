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

    List<User> findByPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    long countByKeyword(@Param("keyword") String keyword);

    int insert(User user);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateRoleAndVipExpire(@Param("id") Long id, @Param("role") String role,
                               @Param("vipExpireTime") java.time.LocalDateTime vipExpireTime);
}

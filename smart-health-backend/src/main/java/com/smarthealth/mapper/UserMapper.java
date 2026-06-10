package com.smarthealth.mapper;

import com.smarthealth.entity.User;
import com.smarthealth.dto.response.DashboardRecentUserResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByUsername(@Param("username") String username);

    List<User> findAll();

    List<User> findByPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    long countByKeyword(@Param("keyword") String keyword);

    long countAll();

    long countByRole(@Param("role") String role);

    long countByStatus(@Param("status") Integer status);

    long countCreatedSince(@Param("startTime") LocalDateTime startTime);

    long countActiveSince(@Param("startTime") LocalDateTime startTime);

    List<DashboardRecentUserResponse> findRecentActive(@Param("onlineStart") LocalDateTime onlineStart,
                                                       @Param("limit") int limit);

    int insert(User user);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateLastActiveTime(@Param("id") Long id, @Param("lastActiveTime") LocalDateTime lastActiveTime);

    int updateRoleAndVipExpire(@Param("id") Long id, @Param("role") String role,
                               @Param("vipExpireTime") java.time.LocalDateTime vipExpireTime);
}

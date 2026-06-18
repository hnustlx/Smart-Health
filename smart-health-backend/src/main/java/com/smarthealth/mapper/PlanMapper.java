package com.smarthealth.mapper;

import com.smarthealth.entity.Plan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PlanMapper {

    Plan findById(@Param("id") Long id);

    List<Plan> findByUserId(@Param("userId") Long userId);

    List<Plan> findByUserIdPage(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    long countByUserId(@Param("userId") Long userId);

    long countCreatedSince(@Param("startTime") LocalDateTime startTime);

    long countByLevelSince(@Param("planLevel") String planLevel, @Param("startTime") LocalDateTime startTime);

    int insert(Plan plan);

    int deleteById(@Param("id") Long id);
}

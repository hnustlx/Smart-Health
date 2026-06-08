package com.smarthealth.mapper;

import com.smarthealth.entity.Plan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PlanMapper {

    Plan findById(@Param("id") Long id);

    List<Plan> findByUserId(@Param("userId") Long userId);

    int insert(Plan plan);
}

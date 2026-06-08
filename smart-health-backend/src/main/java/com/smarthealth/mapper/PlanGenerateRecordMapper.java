package com.smarthealth.mapper;

import com.smarthealth.entity.PlanGenerateRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;

@Mapper
public interface PlanGenerateRecordMapper {

    PlanGenerateRecord findByUserIdAndDate(@Param("userId") Long userId, @Param("generateDate") LocalDate generateDate);

    int insert(PlanGenerateRecord record);

    int updateCount(@Param("id") Long id, @Param("generateCount") Integer generateCount);
}

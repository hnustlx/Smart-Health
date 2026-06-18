package com.smarthealth.mapper;

import com.smarthealth.entity.WeightRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WeightRecordMapper {

    List<WeightRecord> findByUserId(@Param("userId") Long userId);

    List<WeightRecord> findByUserIdPage(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);

    long countByUserId(@Param("userId") Long userId);

    WeightRecord findById(@Param("id") Long id);

    int insert(WeightRecord record);

    int deleteById(@Param("id") Long id);

    int countByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);
}

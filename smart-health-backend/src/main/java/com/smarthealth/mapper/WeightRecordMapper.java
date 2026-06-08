package com.smarthealth.mapper;

import com.smarthealth.entity.WeightRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WeightRecordMapper {

    List<WeightRecord> findByUserId(@Param("userId") Long userId);

    WeightRecord findById(@Param("id") Long id);

    int insert(WeightRecord record);

    int deleteById(@Param("id") Long id);
}

package com.smarthealth.mapper;

import com.smarthealth.entity.DailyCheckin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyCheckinMapper {
    int insert(@Param("userId") Long userId, @Param("checkinDate") LocalDate checkinDate);

    int countByUserId(@Param("userId") Long userId);

    int countByUserIdAndDate(@Param("userId") Long userId, @Param("checkinDate") LocalDate checkinDate);

    List<DailyCheckin> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}

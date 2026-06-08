package com.smarthealth.mapper;

import com.smarthealth.entity.HealthProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProfileMapper {

    HealthProfile findByUserId(@Param("userId") Long userId);

    int insert(HealthProfile profile);

    int update(HealthProfile profile);
}

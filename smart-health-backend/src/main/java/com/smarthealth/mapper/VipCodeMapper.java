package com.smarthealth.mapper;

import com.smarthealth.entity.VipActivationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface VipCodeMapper {

    int insert(VipActivationCode code);

    VipActivationCode findByCode(@Param("code") String code);

    List<VipActivationCode> findAll();

    int updateStatus(@Param("id") Long id, @Param("status") Integer status,
                     @Param("usedBy") Long usedBy, @Param("usedAt") java.time.LocalDateTime usedAt);
}

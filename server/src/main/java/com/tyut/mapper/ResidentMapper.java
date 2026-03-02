package com.tyut.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tyut.entity.ResidentProfile;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

public interface ResidentMapper extends BaseMapper<ResidentProfile> {
    @MapKey("name")
    List<Map<String,Object>> getResidentAgeCount();
}

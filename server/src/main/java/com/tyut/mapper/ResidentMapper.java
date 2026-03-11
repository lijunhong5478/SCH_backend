package com.tyut.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tyut.entity.ResidentProfile;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ResidentMapper extends BaseMapper<ResidentProfile> {
    @MapKey("name")
    List<Map<String,Object>> getResidentAgeCount();
    @Select("select name from resident_profile rp left join sys_user su on rp.user_id=su.id " +
            "where su.id=#{id}")
    String selectRealNameById(Long id);
}

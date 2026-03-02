package com.tyut.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tyut.dto.DoctorQueryDTO;
import com.tyut.entity.DoctorProfile;
import com.tyut.vo.DoctorDetailVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DoctorProfileMapper extends BaseMapper<DoctorProfile> {
    IPage<DoctorDetailVO> list(@Param("page")Page<DoctorDetailVO> page,@Param("query") DoctorQueryDTO query);
    @MapKey("name")
    List<Map<String,Object>> getDoctorDeptCount();
}

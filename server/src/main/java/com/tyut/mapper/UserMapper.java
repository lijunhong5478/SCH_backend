package com.tyut.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tyut.entity.SysUser;
import com.tyut.vo.DoctorDetailVO;
import com.tyut.vo.ResidentDetailVO;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<SysUser> {
    // 根据用户名查询用户
    @Select("select * from sys_user where username=#{username}")
    SysUser selectByUsername(String username);
    // 根据手机号查询用户
    @Select("select * from sys_user where phone= #{phone}")
    SysUser selectByPhone(String phone);
    // 根据身份证号查询用户
    @Select("select * from sys_user where id_card= #{idCard}")
    SysUser selectByIdCard(String idCard);
    // 根据id 查询医生信息
    DoctorDetailVO selectDoctorById(Long id);
    // 根据id 查询居民信息
    ResidentDetailVO selectResidentById(Long id);
    @Select("select username from sys_user where id=#{id}")
    String selectUsernameById(Long id);
}

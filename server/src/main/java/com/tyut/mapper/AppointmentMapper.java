package com.tyut.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tyut.entity.Appointment;
import org.apache.ibatis.annotations.MapKey;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppointmentMapper extends BaseMapper<Appointment> {
    /**
     * 获取最近一周（含今日）每天的预约数量
     * 返回格式：[{date: LocalDate对象, count: Integer}, ...]
     */
    @MapKey("date")
    List<Map<String, Object>> getWeeklyAppointmentCount(LocalDate startDate, LocalDate endDate);
}

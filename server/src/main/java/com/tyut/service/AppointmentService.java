package com.tyut.service;

import com.tyut.dto.AppointmentQueryDTO;
import com.tyut.dto.ExactTimeAppointmentDTO;
import com.tyut.result.PageResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppointmentService {
    String saveAppointment(ExactTimeAppointmentDTO dto);
    PageResult list(AppointmentQueryDTO dto);
    void cancelAppointment(Long appointmentId, String cancelReason);
    void call(Long appointmentId);
    void startConsult(Long appointmentId);
    void skip(Long appointmentId);
    void finish(Long appointmentId);
    Boolean isAppointed(AppointmentQueryDTO dto);

    /**
     * 获取今日预约状态统计
     * @return 包含排队中、就诊中、已完成数量的 Map
     */
    Map<String, Integer> getTodayAppointmentStatusCount();

    /**
     * 根据日期和医生 ID 查询有排班的时段
     * @param date 日期（yyyy-MM-dd）
     * @param doctorId 医生 ID
     * @return 时段列表（AM/PM）
     */
    List<String> getAvailableTimeSlots(LocalDate date, Long doctorId);
}

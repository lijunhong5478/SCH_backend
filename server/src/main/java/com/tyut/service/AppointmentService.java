package com.tyut.service;

import com.tyut.dto.AppointmentQueryDTO;
import com.tyut.dto.ExactTimeAppointmentDTO;
import com.tyut.result.PageResult;

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
}

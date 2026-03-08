package com.tyut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tyut.annotation.DataBackUp;
import com.tyut.constant.*;
import com.tyut.context.BaseContext;
import com.tyut.context.UserContext;
import com.tyut.dto.AppointmentQueryDTO;
import com.tyut.dto.ExactTimeAppointmentDTO;
import com.tyut.entity.Appointment;
import com.tyut.entity.DoctorSchedule;
import com.tyut.exception.BaseException;
import com.tyut.mapper.AppointmentMapper;
import com.tyut.mapper.DoctorScheduleMapper;
import com.tyut.result.PageResult;
import com.tyut.service.AppointmentService;
import com.tyut.utils.AppointmentUtil;
import com.tyut.utils.QueueManagerUtil;
import com.tyut.utils.TriageUtil;
import com.tyut.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.websocket.Session;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppointmentServiceImpl implements AppointmentService {
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private DoctorScheduleMapper doctorScheduleMapper;
    @Autowired
    private TriageUtil triageUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    @DataBackUp(module = ModuleConstant.APPOINTMENT_BOOK)
    @Override
    public void saveAppointment(ExactTimeAppointmentDTO dto) {
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(dto, appointment);
        appointment.setAppointmentStatus(AppointConstant.BOOKED);
        appointment.setVisitStatus(VisitConstant.WAITING);
        appointment.setCreateTime(LocalDateTime.now());
        DoctorSchedule doctorSchedule = doctorScheduleMapper.selectById(dto.getScheduleId());
        if (doctorSchedule == null) throw new BaseException("当前医生排班不存在");
        if (doctorSchedule.getStatus() == 0) throw new BaseException("该医生已停诊");
        if (doctorSchedule.getCurrentNumber() >= doctorSchedule.getMaxNumber())
            throw new BaseException("当前医生已满号");
        AppointmentQueryDTO query = new AppointmentQueryDTO();
        BeanUtils.copyProperties(dto, query);
        Integer weekDay = doctorSchedule.getWeekDay();
        Integer triageLevel = triageUtil.determineTriageLevel(appointment.getSymptom());
        appointment.setTriageLevel(triageLevel);
        String queueNo = AppointmentUtil.generateQueueNo(weekDay, doctorSchedule.getTimeSlot()
                , dto.getDoctorId(), dto.getResidentId());
        appointment.setQueueNo(queueNo);
        appointment.setIsDeleted(AccountConstant.NOT_DELETE);
        appointmentMapper.insert(appointment);

        //发送通知给医生
        sendNotification("doctor", dto.getDoctorId(), "新预约通知", appointment.getQueueNo());
        //发送通知给管理员
        pushAppointmentStatsToAdmins();
    }

    @Override
    public PageResult list(AppointmentQueryDTO dto) {
        Page<Appointment> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        if (dto.getDoctorId() != null) {
            queryWrapper.eq(Appointment::getDoctorId, dto.getDoctorId());
        }
        if (dto.getResidentId() != null) {
            queryWrapper.eq(Appointment::getResidentId, dto.getResidentId());
        }
        if (dto.getAppointmentStatus() != null) {
            queryWrapper.eq(Appointment::getAppointmentStatus, dto.getAppointmentStatus());
        }
        if (dto.getVisitStatus() != null) {
            queryWrapper.eq(Appointment::getVisitStatus, dto.getVisitStatus());
        }
        if (dto.getAppointmentDate() != null) {
            queryWrapper.eq(Appointment::getAppointmentDate, dto.getAppointmentDate());
        }
        if (dto.getAppointmentTime() != null) {
            queryWrapper.eq(Appointment::getAppointmentTime, dto.getAppointmentTime());
        }
        // 根据用户角色决定排序策略
        Integer currentUserRole = BaseContext.getCurrentRole();

        // 如果是居民用户(角色为2)，按照预约日期和时间降序排序
        if (currentUserRole != null && currentUserRole == AccountConstant.ROLE_RESIDENT) {
            queryWrapper.orderByDesc(Appointment::getAppointmentDate)
                    .orderByDesc(Appointment::getAppointmentTime);
        }
        // 如果是医生用户或其他情况，保持原有逻辑
        else {
            // 按照创建时间降序排列，确保最新的预约在前面
            queryWrapper.orderByDesc(Appointment::getCreateTime);
        }
        IPage<Appointment> appointmentIPage = appointmentMapper.selectPage(page, queryWrapper);
        List<Appointment> appointments = appointmentIPage.getRecords();
        if (shouldApplyQueueSorting(dto)) {
            List<QueueManagerUtil.WaitingPatient> waitingPatients = appointments.stream()
                    .map(QueueManagerUtil.WaitingPatient::new)
                    .collect(Collectors.toList());

            List<Appointment> sortedAppointments = QueueManagerUtil.getCallOrder(waitingPatients);
            appointmentIPage.setRecords(sortedAppointments);
        }
        return PageResult.builder()
                .total(appointmentIPage.getTotal())
                .dataList(appointmentIPage.getRecords())
                .build();
    }

    @DataBackUp(module = ModuleConstant.APPOINTMENT_CANCEL)
    @Override
    public void cancelAppointment(Long appointmentId, String cancelReason) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        //预约状态和就诊状态必须为预约中且未开始
        if (!Objects.equals(appointment.getAppointmentStatus(), AppointConstant.BOOKED)
                || !Objects.equals(appointment.getVisitStatus(), VisitConstant.WAITING)
        ) {
            throw new BaseException("当前预约不可取消！");
        }
        appointment.setAppointmentStatus(AppointConstant.CANCELLED);
        appointment.setCancelReason(cancelReason);
        appointment.setCancelTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        //发送通知给医生
        sendNotification("doctor", appointment.getDoctorId(), "取消预约通知", appointment.getQueueNo());
        pushAppointmentStatsToAdmins();
    }

    @DataBackUp(module = ModuleConstant.APPOINTMENT_CALL)
    @Override
    public void call(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (!Objects.equals(appointment.getAppointmentStatus(), AppointConstant.BOOKED)
                || Objects.equals(appointment.getVisitStatus(), VisitConstant.IN_VISIT)) {
            throw new BaseException("当前预约不可叫号！");
        }
        appointment.setVisitStatus(VisitConstant.CALLED);

        appointmentMapper.updateById(appointment);
        sendNotification("resident", appointment.getResidentId(), "叫号通知", appointment.getQueueNo());
        pushAppointmentStatsToAdmins();
    }

    @DataBackUp(module = ModuleConstant.APPOINTMENT_START)
    @Override
    public void startConsult(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment.getAppointmentStatus() != AppointConstant.BOOKED) {
            throw new BaseException("当前预约不可开始咨询！");
        }
        if (appointment.getVisitStatus() != VisitConstant.CALLED) {
            throw new BaseException("请先叫号！");
        }
        appointment.setVisitStatus(VisitConstant.IN_VISIT);
        appointmentMapper.updateById(appointment);
        pushAppointmentStatsToAdmins();
    }

    @DataBackUp(module = ModuleConstant.APPOINTMENT_SKIP)
    @Override
    public void skip(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (!Objects.equals(appointment.getAppointmentStatus(), AppointConstant.BOOKED)
                || !Objects.equals(appointment.getVisitStatus(), VisitConstant.CALLED)
        ) {
            throw new BaseException("当前预约不可跳过！");
        }
        appointment.setVisitStatus(VisitConstant.SKIPPED);
        appointmentMapper.updateById(appointment);
        sendNotification("resident", appointment.getResidentId(), "过号通知", appointment.getQueueNo());
        pushAppointmentStatsToAdmins();
    }

    @DataBackUp(module = ModuleConstant.APPOINTMENT_FINISH)
    @Override
    public void finish(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (!Objects.equals(appointment.getAppointmentStatus(), AppointConstant.BOOKED)
                || !Objects.equals(appointment.getVisitStatus(), VisitConstant.IN_VISIT)
        ) {
            throw new BaseException("当前预约不可结束！");
        }
        appointment.setAppointmentStatus(AppointConstant.FINSHED);
        appointment.setVisitStatus(VisitConstant.DONE);
        appointmentMapper.updateById(appointment);
        sendNotification("resident", appointment.getResidentId(), "结束咨询通知", appointment.getQueueNo());
        pushAppointmentStatsToAdmins();
    }

    @Override
    public Boolean isAppointed(AppointmentQueryDTO dto) {
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appointment::getResidentId, dto.getResidentId())
                .eq(Appointment::getDoctorId, dto.getDoctorId())
                .eq(Appointment::getAppointmentDate, dto.getAppointmentDate())
                .eq(Appointment::getAppointmentTime, dto.getAppointmentTime());
        Appointment appointment = appointmentMapper.selectOne(queryWrapper);
        return appointment != null;
    }

    @Override
    public Map<String, Integer> getTodayAppointmentStatusCount() {
        LocalDate today = LocalDate.now();

        // 查询排队中的数量（visitStatus: WAITING=0 或 CALLED=1）
        LambdaQueryWrapper<Appointment> waitingWrapper = new LambdaQueryWrapper<>();
        waitingWrapper.eq(Appointment::getAppointmentDate, today)
                .eq(Appointment::getAppointmentStatus, AppointConstant.BOOKED)
                .in(Appointment::getVisitStatus,
                        VisitConstant.WAITING,
                        VisitConstant.CALLED);
        Integer waitingCount = Math.toIntExact(appointmentMapper.selectCount(waitingWrapper));

        // 查询就诊中的数量（visitStatus: IN_VISIT=2）
        LambdaQueryWrapper<Appointment> inVisitWrapper = new LambdaQueryWrapper<>();
        inVisitWrapper.eq(Appointment::getAppointmentDate, today)
                .eq(Appointment::getAppointmentStatus, AppointConstant.BOOKED)
                .eq(Appointment::getVisitStatus, VisitConstant.IN_VISIT);
        Integer inVisitCount = Math.toIntExact(appointmentMapper.selectCount(inVisitWrapper));

        // 查询已完成的数量（appointmentStatus: FINISHED=3）
        LambdaQueryWrapper<Appointment> finishedWrapper = new LambdaQueryWrapper<>();
        finishedWrapper.eq(Appointment::getAppointmentDate, today)
                .eq(Appointment::getAppointmentStatus, AppointConstant.FINSHED);
        Integer finishedCount = Math.toIntExact(appointmentMapper.selectCount(finishedWrapper));

        Map<String, Integer> result = new HashMap<>();
        result.put("waiting", waitingCount);
        result.put("inVisit", inVisitCount);
        result.put("finished", finishedCount);
        return result;
    }

    /**
     * 向所有在线管理员推送预约统计数据
     */
    private void pushAppointmentStatsToAdmins() {
        try {
            Map<String, Integer> stats = getTodayAppointmentStatusCount();
            String message = buildStatsJson(stats);

            // 群发给所有在线的管理员
            Collection<Session> adminSessions = webSocketServer.getSessionMap().values();
            for (Session session : adminSessions) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            }
        } catch (Exception e) {
            log.error("推送预约统计数据失败", e);
        }
    }

    /**
     * 判断是否需要应用队列排序
     */
    private boolean shouldApplyQueueSorting(AppointmentQueryDTO dto) {
        // 当查询条件包含医生ID且访问状态为等待中或已叫号时，应用队列排序
        return dto.getDoctorId() != null &&
                (dto.getVisitStatus() == null ||
                        dto.getVisitStatus() == VisitConstant.WAITING ||
                        dto.getVisitStatus() == VisitConstant.CALLED);
    }


    /**
     * 通用通知发送方法
     *
     * @param userType 用户类型：doctor/resident
     * @param userId   用户ID
     * @param title    通知标题
     * @param queueNo  排队序号
     */
    private void sendNotification(String userType, Long userId, String title, String queueNo) {
        try {
            String sid = userType + "_" + userId;
            String message = title + "：请关注您的排队序号 " + queueNo;

            if (webSocketServer.isUserOnline(userType + "_", userId)) {
                webSocketServer.sendToClient(sid, buildNotificationJson(title, queueNo));
            }
        } catch (Exception e) {
            log.error("发送通知失败", e);
        }
    }

    /**
     * 构建简化通知JSON格式
     */
    private String buildNotificationJson(String title, String queueNo) {
        return String.format(
                "{"
                        + "\"type\":\"notification\","
                        + "\"title\":\"%s\","
                        + "\"queueNo\":\"%s\","
                        + "\"timestamp\":\"%s\""
                        + "}",
                escapeJsonString(title),
                escapeJsonString(queueNo),
                LocalDateTime.now().toString()
        );
    }


    /**
     * JSON字符串转义
     */
    private String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    /**
     * 构建统计 JSON 消息
     */
    private String buildStatsJson(Map<String, Integer> stats) {
        return String.format(
                "{"
                        + "\"type\":\"appointment_stats\","
                        + "\"data\":{"
                        + "\"waiting\":%d,"
                        + "\"inVisit\":%d,"
                        + "\"finished\":%d"
                        + "},"
                        + "\"timestamp\":\"%s\""
                        + "}",
                stats.getOrDefault("waiting", 0),
                stats.getOrDefault("inVisit", 0),
                stats.getOrDefault("finished", 0),
                LocalDateTime.now().toString()
        );
    }
}

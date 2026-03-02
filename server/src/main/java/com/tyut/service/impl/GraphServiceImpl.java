package com.tyut.service.impl;

import com.tyut.mapper.AppointmentMapper;
import com.tyut.mapper.DoctorProfileMapper;
import com.tyut.mapper.ResidentMapper;
import com.tyut.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GraphServiceImpl implements GraphService {
    @Autowired
    private ResidentMapper residentMapper;
    @Autowired
    private DoctorProfileMapper doctorProfileMapper;
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Override
    public List<Map<String, Object>> getResidentAgeCount() {
        return residentMapper.getResidentAgeCount();
    }

    @Override
    public List<Map<String, Object>> getDoctorDeptCount() {
        // 调用Mapper获取统计数据
        List<Map<String, Object>> rawData = doctorProfileMapper.getDoctorDeptCount();

        // 转换数据格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : rawData) {
            Map<String, Object> formattedItem = new HashMap<>();
            formattedItem.put("value", ((Long) item.get("value")).intValue());
            formattedItem.put("name", item.get("name"));
            result.add(formattedItem);
        }

        return result;

    }
    @Override
    public List<Map<String, Object>> getAppointmentCount() {
        // 计算最近一周的日期范围（包含今天）
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // 往前推6天，总共7天

        // 调用Mapper获取统计数据
        List<Map<String, Object>> rawData = appointmentMapper.getWeeklyAppointmentCount(startDate, endDate);

        // 创建日期到数量的映射
        Map<LocalDate, Integer> dateCountMap = new HashMap<>();

        // 初始化最近7天的数据（确保每天都有数据，没有预约的显示0）
        for (int i = 0; i <= 6; i++) {
            LocalDate date = endDate.minusDays(i);
            dateCountMap.put(date, 0);
        }

        // 填充实际数据，处理java.sql.Date到LocalDate的转换
        for (Map<String, Object> item : rawData) {
            // 从java.sql.Date转换为LocalDate
            Date sqlDate = (Date) item.get("date");
            LocalDate date = sqlDate.toLocalDate();
            Integer count = ((Long) item.get("count")).intValue();
            dateCountMap.put(date, count);
        }

        // 转换为前端需要的格式（按日期倒序排列，今天在前）
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i <= 6; i++) {
            LocalDate date = endDate.minusDays(i);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date);
            dayData.put("count", dateCountMap.get(date));
            result.add(dayData);
        }

        return result;
    }
}

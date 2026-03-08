package com.tyut.controller.admin;

import com.tyut.result.Result;
import com.tyut.service.AppointmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("adminAppointmentController")
@RequestMapping("/admin/appointment")
@Api(tags="管理员-预约统计管理")
@Slf4j
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;
    @GetMapping("/status/today")
    @ApiOperation("获取今日预约状态统计")
    public Result<Map<String, Integer>> getTodayAppointmentStats() {
        Map<String, Integer> stats = appointmentService.getTodayAppointmentStatusCount();
        return Result.success(stats);
    }
}

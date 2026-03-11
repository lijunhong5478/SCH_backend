package com.tyut.controller.doctor;

import com.tyut.dto.AppointmentQueryDTO;
import com.tyut.result.PageResult;
import com.tyut.result.Result;
import com.tyut.service.AppointmentService;
import com.tyut.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("doctorAppointmentController")
@RequestMapping("/doctor/appointment")
@Api(tags = "医生-预约管理接口")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private UserService userService;
    @GetMapping("/list")
    @ApiOperation("社区居民查询预约列表接口")
    public Result<PageResult> list(AppointmentQueryDTO appointmentQueryDTO) {
        return Result.success(appointmentService.list(appointmentQueryDTO));
    }
    @PutMapping("/call")
    @ApiOperation("社区居民叫号接口")
    public Result<String> call(Long appointmentId) {
        appointmentService.call(appointmentId);
        return Result.success();
    }
    @PutMapping("/startConsult")
    @ApiOperation("社区居民开始咨询接口")
    public Result<String> startConsult(Long appointmentId) {
        appointmentService.startConsult(appointmentId);
        return Result.success();
    }
    @PutMapping("/skip")
    @ApiOperation("社区居民跳过接口")
    public Result<String> skip(Long appointmentId) {
        appointmentService.skip(appointmentId);
        return Result.success();
    }
    @PutMapping("/finish")
    @ApiOperation("社区居民结束咨询接口")
    public Result<String> finish(Long appointmentId) {
        appointmentService.finish(appointmentId);
        return Result.success();
    }
    @GetMapping("/getRealName")
    @ApiOperation("获取预约人姓名接口")
    public Result<String> getRealName(Long residentId) {
        return Result.success(userService.getRealNameById(residentId));
    }
}

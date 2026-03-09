package com.tyut.controller.resident;

import com.tyut.dto.AppointmentQueryDTO;
import com.tyut.dto.ExactTimeAppointmentDTO;
import com.tyut.entity.Appointment;
import com.tyut.result.PageResult;
import com.tyut.result.Result;
import com.tyut.service.AppointmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("residentAppointmentController")
@RequestMapping("/resident/appointment")
@Api(tags="居民-预约接口")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;
    @PostMapping
    @ApiOperation("社区居民预约接口")
    public Result<String> appointment(@RequestBody ExactTimeAppointmentDTO dto) {
        return Result.success(appointmentService.saveAppointment(dto));
    }
    @GetMapping("/list")
    @ApiOperation("社区居民查询预约列表接口")
    public Result<PageResult> list(AppointmentQueryDTO appointmentQueryDTO) {
        return Result.success(appointmentService.list(appointmentQueryDTO));
    }
    @PutMapping("/cancel")
    @ApiOperation("社区居民取消预约接口")
    public Result<String> cancelAppointment(Long appointmentId,String cancelReason) {
        appointmentService.cancelAppointment(appointmentId,cancelReason);
        return Result.success();
    }
    @ApiOperation("判断是否已经挂号")
    @PostMapping("/isAppointed")
    public Result<Boolean> isAppointed(@RequestBody AppointmentQueryDTO dto){
        return Result.success(appointmentService.isAppointed(dto));
    }
}

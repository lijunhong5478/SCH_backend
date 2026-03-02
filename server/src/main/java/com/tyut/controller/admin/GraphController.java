package com.tyut.controller.admin;

import com.tyut.result.Result;
import com.tyut.service.GraphService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@Api(tags="管理员-图表接口")
@RequestMapping("/admin/graph")
public class GraphController {
    @Autowired
    private GraphService graphService;
    @ApiOperation("用户年龄占比")
    @GetMapping("/userAge")
    public Result<List<Map<String,Object>>> getResidentAgeCount() {
        return Result.success(graphService.getResidentAgeCount());
    }
    @ApiOperation("医生科室占比")
    @GetMapping("/doctorDept")
    public Result<List<Map<String,Object>>> getDoctorDeptCount() {
        return Result.success(graphService.getDoctorDeptCount());
    }
    @ApiOperation("最近一周预约数量")
    @GetMapping("/appointment")
    public Result<List<Map<String,Object>>> getAppointmentCount() {
        return Result.success(graphService.getAppointmentCount());
    }
}

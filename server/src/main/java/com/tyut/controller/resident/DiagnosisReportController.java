package com.tyut.controller.resident;

import com.tyut.dto.DiagnosisReportQueryDTO;
import com.tyut.entity.DiagnosisReport;
import com.tyut.result.PageResult;
import com.tyut.result.Result;
import com.tyut.service.DiagnosisReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("residentDiagnosisReportController")
@RequestMapping("/resident/diagnosisReport")
@Api(tags="居民-诊断报告接口")
public class DiagnosisReportController {
    @Autowired
    private DiagnosisReportService diagnosisReportService;
    @GetMapping("/{id}")
    @ApiOperation("根据id查询诊断报告")
    public Result<DiagnosisReport> getById(@PathVariable Long id){
        return Result.success(diagnosisReportService.getById(id));
    }
    @GetMapping("/list")
    @ApiOperation("查询诊断报告列表")
    public Result<PageResult> list(DiagnosisReportQueryDTO diagnosisReportQueryDTO){
        return Result.success(diagnosisReportService.list(diagnosisReportQueryDTO));
    }
}

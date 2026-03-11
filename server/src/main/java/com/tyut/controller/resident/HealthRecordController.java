package com.tyut.controller.resident;


import com.tyut.entity.MedicalVisit;

import com.tyut.result.Result;
import com.tyut.service.HealthRecordService;
import com.tyut.service.MedicalVisitService;
import com.tyut.vo.HealthRecordVO;
import com.tyut.vo.MedicalVisitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("residentHealthRecordController")
@RequestMapping("/resident/healthRecord")
@Api(tags="居民-健康档案接口")
@Slf4j
public class HealthRecordController {
    @Autowired
    private HealthRecordService healthRecordService;

    @GetMapping("/{id}")
    @ApiOperation("根据id查询健康档案")
    public Result<HealthRecordVO> getHealthRecordById(@PathVariable Long id){
        log.info("查询健康档案:{}",id);
        return Result.success(healthRecordService.getByResidentId(id));
    }
    @GetMapping("/getId")
    @ApiOperation("根据id查询健康档案id")
    public Result<Long> getIdByResidentId(Long id){
        log.info("查询健康档案id:{}",id);
        return Result.success(healthRecordService.getIdByResidentId(id));
    }
}

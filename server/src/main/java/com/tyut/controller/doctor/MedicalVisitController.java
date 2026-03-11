package com.tyut.controller.doctor;

import com.tyut.dto.MedicalVisitQueryDTO;
import com.tyut.entity.MedicalVisit;
import com.tyut.result.PageResult;
import com.tyut.result.Result;

import com.tyut.service.MedicalVisitService;
import com.tyut.vo.MedicalVisitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("doctorMedicalVisitController")
@Api(tags="医生-问诊接口")
@RequestMapping("/doctor/medicalVisit")
public class MedicalVisitController {
    @Autowired
    private MedicalVisitService medicalVisitService;
    @PostMapping
    @ApiOperation("保存问诊信息")
    public Result<Long> saveMedicalVisit(@RequestBody MedicalVisit medicalVisit){
        medicalVisitService.save(medicalVisit);
        return Result.success(medicalVisit.getId());
    }
    @PutMapping
    @ApiOperation("修改问诊信息")
    public Result<String> updateMedicalVisit(@RequestBody MedicalVisit medicalVisit){
        medicalVisitService.update(medicalVisit);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询问诊信息")
    public Result<MedicalVisitVO> getMedicalVisit(@PathVariable Long id){
        return Result.success(medicalVisitService.getById(id));
    }
    @GetMapping("/list")
    @ApiOperation("查询问诊列表")
    public Result<PageResult> listMedicalVisit(MedicalVisitQueryDTO medicalVisitQueryDTO){
        return Result.success(medicalVisitService.list(medicalVisitQueryDTO));
    }
    @GetMapping("/contains")
    @ApiOperation("根据传入的问诊记录id和医生id判断能否修改")
    public Result<Boolean> contains(@RequestParam Long medicalVisitId,@RequestParam Long doctorId){
        return Result.success(medicalVisitService.contains(medicalVisitId,doctorId));
    }
    @GetMapping("/getByAppointId")
    @ApiOperation("根据预约id查询问诊信息")
    public Result<MedicalVisitVO> getByAppointId(@RequestParam Long appointId){
        return Result.success(medicalVisitService.getByAppointId(appointId));
    }
}

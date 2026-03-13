package com.tyut.controller.doctor;

import com.tyut.constant.AccountConstant;
import com.tyut.dto.UpdateProfileDTO;
import com.tyut.exception.BaseException;
import com.tyut.result.PageResult;
import com.tyut.result.Result;
import com.tyut.service.UserService;
import com.tyut.vo.DoctorDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("doctorAccountController")
@RequestMapping("/doctor/account")
@Api(tags = "医生-账号接口")
public class AccountController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    @ApiOperation("根据id查询")
    public Result<DoctorDetailVO> getDoctorDetailById(@PathVariable Long id) {
        DoctorDetailVO doctorById = userService.getDoctorById(id);
        return Result.success(doctorById);
    }

    @PutMapping
    @ApiOperation("修改医生信息")
    public Result<String> updateDoctor(@RequestBody UpdateProfileDTO updateProfileDTO) {
        userService.updateDoctor(updateProfileDTO);
        return Result.success();
    }

    @GetMapping("/myPatient")
    @ApiOperation("查询我的患者")
    public Result<PageResult> myPatient(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Long doctorId) {
        return Result.success(userService.myPatient(pageNum, pageSize,doctorId));
    }
}

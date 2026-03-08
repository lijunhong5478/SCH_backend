package com.tyut.controller.common;

import com.tyut.entity.Department;
import com.tyut.result.Result;
import com.tyut.service.DepartmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/common/department")
@Api(tags="通用-科室接口")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    @GetMapping("/getAll")
    @ApiOperation("获取所有科室")
    public Result<List<Department>> getAll(){
        List<Department> list=departmentService.list();
        return Result.success(list);
    }
}

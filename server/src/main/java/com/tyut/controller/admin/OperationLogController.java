package com.tyut.controller.admin;

import com.tyut.dto.OperationLogQueryDTO;
import com.tyut.entity.OperationLog;
import com.tyut.result.PageResult;
import com.tyut.result.Result;
import com.tyut.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/operationLog")
@Api(tags="管理员-操作日志接口")
public class OperationLogController {
    @Autowired
    private OperationLogService operationLogService;
    @GetMapping("/list")
    @ApiOperation("分页查询操作日志")
    public Result<PageResult> list(OperationLogQueryDTO operationLogQueryDTO){
        operationLogQueryDTO.initDefaultValues();
        return Result.success(operationLogService.list(operationLogQueryDTO));
    }
    @DeleteMapping("/{id}")
    @ApiOperation("删除操作日志")
    public Result<String> delete(@PathVariable Long id){
        operationLogService.deleteById(id);
        return Result.success();
    }
}

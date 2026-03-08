package com.tyut.controller.admin;

import com.tyut.entity.SystemConfig;
import com.tyut.result.Result;
import com.tyut.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController("adminSystemConfigController")
@RequestMapping("/admin/system-configs")
@Api(tags = "管理员-系统配置管理")
@Slf4j
public class SystemConfigController {

    @Resource
    private SystemConfigService systemConfigService;

    @PutMapping
    @ApiOperation("更新系统配置")
    public Result<String> updateSystemConfig(@RequestBody @Valid SystemConfig systemConfig) {
        systemConfigService.updateSystemConfig(systemConfig);
        return Result.success("更新成功");
    }

    @GetMapping
    @ApiOperation("查询所有系统配置")
    public Result<List<SystemConfig>> getAllSystemConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllSystemConfigs();
        return Result.success(configs);
    }

}

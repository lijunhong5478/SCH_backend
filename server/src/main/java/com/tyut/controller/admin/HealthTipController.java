package com.tyut.controller.admin;

import com.tyut.constant.HealthTipConstant;
import com.tyut.dto.HealthTipQueryDTO;
import com.tyut.entity.HealthTips;
import com.tyut.result.PageResult;
import com.tyut.result.Result;
import com.tyut.service.HealthTipService;
import com.tyut.vo.HealthTipVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController("adminHealthTipController")
@RequestMapping("/admin/health-tips")
@Api(tags = "管理员-健康宣教管理")
@Slf4j
public class HealthTipController {

    @Resource
    private HealthTipService healthTipService;

    @PostMapping("/publish")
    @ApiOperation("发布健康宣教")
    public Result<String> publishHealthTip(@RequestBody @Valid HealthTips healthTips) {
        healthTipService.publishHealthTip(healthTips);
        return Result.success("发布成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询健康宣教详情")
    public Result<HealthTipVO> getHealthTipById(@PathVariable Long id) {
        HealthTipVO healthTipVO = healthTipService.getHealthTipById(id);
        return Result.success(healthTipVO);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询健康宣教")
    public Result<PageResult> queryHealthTips(HealthTipQueryDTO queryDTO) {
        PageResult pageResult = healthTipService.queryHealthTips(queryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除健康宣教")
    public Result<String> deleteHealthTip(@PathVariable Long id) {
        healthTipService.deleteHealthTip(id);
        return Result.success("删除成功");
    }
}

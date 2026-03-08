package com.tyut.controller.admin;

import com.tyut.dto.EpidemicAlertQueryDTO;
import com.tyut.entity.EpidemicAlert;
import com.tyut.result.PageResult;
import com.tyut.result.Result;
import com.tyut.service.EpidemicAlertService;
import com.tyut.vo.EpidemicAlertVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController("adminEpidemicAlertController")
@RequestMapping("/admin/epidemic-alerts")
@Api(tags = "管理员-疫情预警管理")
@Slf4j
public class EpidemicAlertController {

    @Resource
    private EpidemicAlertService epidemicAlertService;

    @PostMapping("/publish")
    @ApiOperation("发布疫情预警")
    public Result<String> publishEpidemicAlert(@RequestBody @Valid EpidemicAlert epidemicAlert) {
        epidemicAlertService.publishEpidemicAlert(epidemicAlert);
        return Result.success("发布成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询疫情预警详情")
    public Result<EpidemicAlertVO> getEpidemicAlertById(@PathVariable Long id) {
        log.info("查询ID为{}的疫情预警", id);
        EpidemicAlertVO epidemicAlertVO = epidemicAlertService.getEpidemicAlertById(id);
        return Result.success(epidemicAlertVO);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询疫情预警")
    public Result<PageResult> queryEpidemicAlerts(EpidemicAlertQueryDTO queryDTO) {
        PageResult pageResult = epidemicAlertService.queryEpidemicAlerts(queryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除疫情预警")
    public Result<String> deleteEpidemicAlert(@PathVariable Long id) {
        epidemicAlertService.deleteEpidemicAlert(id);
        return Result.success("删除成功");
    }
}

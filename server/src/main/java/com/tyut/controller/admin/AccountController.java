package com.tyut.controller.admin;

import com.tyut.dto.AccountQueryDTO;
import com.tyut.dto.UpdateProfileDTO;
import com.tyut.result.PageResult;
import com.tyut.result.Result;
import com.tyut.service.UserService;
import com.tyut.vo.AdminDetailVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminAccountController")
@RequestMapping("/admin/account")
@Api(tags = "管理员-账户管理接口")
public class AccountController {
    @Autowired
    private UserService userService;

    @ApiOperation("根据id查询")
    @GetMapping("/{id}")
    public Result<AdminDetailVO> getAdminDetailById(@PathVariable Long id) {
        return Result.success(userService.getAdminById(id));
    }

    @ApiOperation("修改个人信息")
    @PutMapping
    public Result<String> updateProfile(@RequestBody UpdateProfileDTO updateProfileDTO) {
        userService.updateAdmin(updateProfileDTO);
        return Result.success();
    }

    @ApiOperation("启用禁用")
    @PutMapping("/status")
    public Result<String> updateStatus(Long id, Integer status) {
        log.info("修改用户状态:{}", id);
        log.info("启用禁用:{}", status);
        userService.updateStatus(id, status);
        return Result.success();
    }

    @ApiOperation("删除用户")
    @PutMapping("/delete/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @ApiOperation("撤销删除")
    @PutMapping("/revert/{id}")
    public Result<String> revertUser(@PathVariable Long id) {
        userService.revertUser(id);
        return Result.success();
    }

    @ApiOperation("条件查询用户")
    @GetMapping("/query")
    public Result<PageResult> queryUsers(AccountQueryDTO dto) {
        return Result.success(userService.list(dto));
    }

    @ApiOperation("重置密码")
    @PutMapping("/resetPassword")
    public Result<String> resetPassword(Long userId) {
        log.info("重置密码:{}", userId);
        userService.resetPassword(userId);
        return Result.success();
    }
}

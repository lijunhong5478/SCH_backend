package com.tyut.controller.common;

import com.tyut.context.BaseContext;
import com.tyut.dto.LoginDTO;
import com.tyut.result.Result;
import com.tyut.service.UserService;
import com.tyut.vo.LoginUserVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/common/user")
@Api(tags = "通用-用户接口")
@Slf4j
public class AccountController {
    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * 所有用户必须登录
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<LoginUserVO> login(@RequestBody LoginDTO dto) {
        log.info("用户登录：{}",dto);
        LoginUserVO vo = userService.login(dto);
        return Result.success(vo);
    }

    /**
     * 所有用户都有权修改密码
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @ApiOperation("修改密码")
    @PutMapping("/password")
    public Result<String> updatePassword(String oldPassword, String newPassword) {
        userService.updatePassword(oldPassword, newPassword);
        return Result.success();
    }
    /**
     * 用户退出登录
     * 清除当前用户的上下文信息
     */
    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<String> logout() {
        log.info("用户ID：{}退出登录", BaseContext.getCurrentId());
        userService.logout();
        return Result.success("退出登录成功");
    }

}

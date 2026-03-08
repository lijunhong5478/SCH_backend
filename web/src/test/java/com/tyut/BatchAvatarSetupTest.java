package com.tyut;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tyut.Application;
import com.tyut.constant.AccountConstant;
import com.tyut.entity.SysUser;
import com.tyut.mapper.UserMapper;
import com.tyut.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = Application.class)
public class BatchAvatarSetupTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    // OSS基础URL（根据您之前上传的结果调整）
    private static final String OSS_BASE_URL = "https://dev-ljh.oss-cn-beijing.aliyuncs.com/2026/03/";

    // 头像文件名映射
    private static final String ADMIN_AVATAR = "images/管理员.png";
    private static final String FEMALE_DOCTOR_AVATAR = "images/女医生.png";
    private static final String MALE_DOCTOR_AVATAR = "images/男医生.png";
    private static final String FEMALE_RESIDENT_AVATAR = "images/女居民.png";
    private static final String MALE_RESIDENT_AVATAR = "images/男居民.png";

    @Test
    public void setupAllUserAvatars() {
        System.out.println("=== 开始批量设置用户头像 ===");

        // 1. 设置管理员头像
        setupAdminAvatars();

        // 2. 设置医生头像（根据性别）
        setupDoctorAvatars();

        // 3. 设置居民头像（根据性别）
        setupResidentAvatars();

        System.out.println("=== 批量设置用户头像完成 ===");
    }

    /**
     * 设置管理员头像
     */
    private void setupAdminAvatars() {
        System.out.println("\n--- 设置管理员头像 ---");

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getRoleType, AccountConstant.ROLE_ADMIN)
                   .eq(SysUser::getIsDeleted, AccountConstant.NOT_DELETE);

        List<SysUser> admins = userMapper.selectList(queryWrapper);
        System.out.println("找到 " + admins.size() + " 个管理员");

        int successCount = 0;
        int failCount = 0;

        for (SysUser admin : admins) {
            try {
                String avatarUrl = OSS_BASE_URL + ADMIN_AVATAR;
                LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(SysUser::getId, admin.getId())
                           .set(SysUser::getAvatarUrl, avatarUrl);

                userMapper.update(null, updateWrapper);
                successCount++;
                System.out.println("✅ 管理员 " + admin.getUsername() + " 头像设置成功");
            } catch (Exception e) {
                failCount++;
                System.err.println("❌ 管理员 " + admin.getUsername() + " 头像设置失败: " + e.getMessage());
            }
        }

        System.out.println("管理员头像设置完成 - 成功: " + successCount + ", 失败: " + failCount);
    }

    /**
     * 设置医生头像（根据性别）
     */
    private void setupDoctorAvatars() {
        System.out.println("\n--- 设置医生头像 ---");

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getRoleType, AccountConstant.ROLE_DOCTOR)
                   .eq(SysUser::getIsDeleted, AccountConstant.NOT_DELETE);

        List<SysUser> doctors = userMapper.selectList(queryWrapper);
        System.out.println("找到 " + doctors.size() + " 个医生");

        int maleCount = 0, femaleCount = 0;
        int successCount = 0, failCount = 0;

        for (SysUser doctor : doctors) {
            try {
                // 这里需要获取医生的性别信息，由于SysUser表没有gender字段，
                // 需要通过DoctorProfile表获取
                com.tyut.vo.DoctorDetailVO doctorDetail = userService.getDoctorById(doctor.getId());

                String avatarUrl;
                if (doctorDetail != null && doctorDetail.getGender() != null) {
                    if (doctorDetail.getGender() == AccountConstant.FEMALE) {
                        avatarUrl = OSS_BASE_URL + FEMALE_DOCTOR_AVATAR;
                        femaleCount++;
                    } else {
                        avatarUrl = OSS_BASE_URL + MALE_DOCTOR_AVATAR;
                        maleCount++;
                    }
                } else {
                    // 如果无法获取性别信息，默认设置为男医生头像
                    avatarUrl = OSS_BASE_URL + MALE_DOCTOR_AVATAR;
                    maleCount++;
                }

                LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(SysUser::getId, doctor.getId())
                           .set(SysUser::getAvatarUrl, avatarUrl);

                userMapper.update(null, updateWrapper);
                successCount++;
                System.out.println("✅ 医生 " + doctor.getUsername() + " 头像设置成功 (" +
                    (doctorDetail != null && doctorDetail.getGender() != null ?
                     (doctorDetail.getGender() == AccountConstant.FEMALE ? "女" : "男") : "未知") + ")");

            } catch (Exception e) {
                failCount++;
                System.err.println("❌ 医生 " + doctor.getUsername() + " 头像设置失败: " + e.getMessage());
            }
        }

        System.out.println("医生头像设置完成 - 男医生: " + maleCount + ", 女医生: " + femaleCount +
                          ", 成功: " + successCount + ", 失败: " + failCount);
    }

    /**
     * 设置居民头像（根据性别）
     */
    private void setupResidentAvatars() {
        System.out.println("\n--- 设置居民头像 ---");

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getRoleType, AccountConstant.ROLE_RESIDENT)
                   .eq(SysUser::getIsDeleted, AccountConstant.NOT_DELETE);

        List<SysUser> residents = userMapper.selectList(queryWrapper);
        System.out.println("找到 " + residents.size() + " 个居民");

        int maleCount = 0, femaleCount = 0;
        int successCount = 0, failCount = 0;

        for (SysUser resident : residents) {
            try {
                // 获取居民详细信息以获得性别
                com.tyut.vo.ResidentDetailVO residentDetail = userService.getResidentById(resident.getId());

                String avatarUrl;
                if (residentDetail != null && residentDetail.getGender() != null) {
                    if (residentDetail.getGender() == AccountConstant.FEMALE) {
                        avatarUrl = OSS_BASE_URL + FEMALE_RESIDENT_AVATAR;
                        femaleCount++;
                    } else {
                        avatarUrl = OSS_BASE_URL + MALE_RESIDENT_AVATAR;
                        maleCount++;
                    }
                } else {
                    // 如果无法获取性别信息，默认设置为男居民头像
                    avatarUrl = OSS_BASE_URL + MALE_RESIDENT_AVATAR;
                    maleCount++;
                }

                LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(SysUser::getId, resident.getId())
                           .set(SysUser::getAvatarUrl, avatarUrl);

                userMapper.update(null, updateWrapper);
                successCount++;
                System.out.println("✅ 居民 " + resident.getUsername() + " 头像设置成功 (" +
                    (residentDetail != null && residentDetail.getGender() != null ?
                     (residentDetail.getGender() == AccountConstant.FEMALE ? "女" : "男") : "未知") + ")");

            } catch (Exception e) {
                failCount++;
                System.err.println("❌ 居民 " + resident.getUsername() + " 头像设置失败: " + e.getMessage());
            }
        }

        System.out.println("居民头像设置完成 - 男居民: " + maleCount + ", 女居民: " + femaleCount +
                          ", 成功: " + successCount + ", 失败: " + failCount);
    }

    /**
     * 验证头像设置结果
     */
    @Test
    public void verifyAvatarSetup() {
        System.out.println("=== 验证头像设置结果 ===");

        // 验证管理员
        verifyUserRoleAvatars(AccountConstant.ROLE_ADMIN, "管理员");

        // 验证医生
        verifyUserRoleAvatars(AccountConstant.ROLE_DOCTOR, "医生");

        // 验证居民
        verifyUserRoleAvatars(AccountConstant.ROLE_RESIDENT, "居民");

        System.out.println("=== 验证完成 ===");
    }

    private void verifyUserRoleAvatars(Integer roleType, String roleName) {
        System.out.println("\n--- 验证" + roleName + "头像设置 ---");

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getRoleType, roleType)
                   .eq(SysUser::getIsDeleted, AccountConstant.NOT_DELETE)
                   .isNotNull(SysUser::getAvatarUrl);

        List<SysUser> users = userMapper.selectList(queryWrapper);
        System.out.println(roleName + "总数: " + users.size());

        int withAvatarCount = 0;
        for (SysUser user : users) {
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                withAvatarCount++;
                System.out.println("✅ " + roleName + ": " + user.getUsername() + " - " + user.getAvatarUrl());
            }
        }

        System.out.println(roleName + "有头像数量: " + withAvatarCount + "/" + users.size());
    }

    /**
     * 测试单个用户头像设置
     */
    @Test
    public void testSingleUserAvatarSetup() {
        System.out.println("=== 测试单个用户头像设置 ===");

        // 测试设置第一个管理员的头像
        LambdaQueryWrapper<SysUser> adminQuery = new LambdaQueryWrapper<>();
        adminQuery.eq(SysUser::getRoleType, AccountConstant.ROLE_ADMIN)
                 .eq(SysUser::getIsDeleted, AccountConstant.NOT_DELETE)
                 .last("LIMIT 1");

        SysUser admin = userMapper.selectOne(adminQuery);
        if (admin != null) {
            System.out.println("测试管理员: " + admin.getUsername());
            String avatarUrl = OSS_BASE_URL + ADMIN_AVATAR;
            System.out.println("设置头像URL: " + avatarUrl);

            LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(SysUser::getId, admin.getId())
                       .set(SysUser::getAvatarUrl, avatarUrl);

            int result = userMapper.update(null, updateWrapper);
            System.out.println("更新结果: " + (result > 0 ? "成功" : "失败"));
        } else {
            System.out.println("未找到管理员用户");
        }
    }
}

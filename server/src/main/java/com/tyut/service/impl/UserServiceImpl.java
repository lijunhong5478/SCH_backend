package com.tyut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tyut.annotation.DataBackUp;
import com.tyut.constant.AccountConstant;
import com.tyut.constant.AppointConstant;
import com.tyut.constant.LoginConstant;
import com.tyut.constant.ModuleConstant;
import com.tyut.context.BaseContext;
import com.tyut.context.WebSocketContext;
import com.tyut.dto.*;
import com.tyut.entity.*;
import com.tyut.exception.BaseException;
import com.tyut.mapper.*;
import com.tyut.properties.JwtProperties;
import com.tyut.result.PageResult;
import com.tyut.service.UserService;
import com.tyut.utils.CryptoUtil;
import com.tyut.utils.JwtUtil;
import com.tyut.vo.AdminDetailVO;
import com.tyut.vo.DoctorDetailVO;
import com.tyut.vo.LoginUserVO;
import com.tyut.vo.ResidentDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CryptoUtil cryptoUtil;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private ResidentMapper residentMapper;
    //注册时 BaseContext 中没有存储用户 ID 和 Role，因此不能与 AOP 绑定
    @Autowired
    private OperationLogMapper operationLogMapper;
    @Autowired
    private HealthRecordMapper healthRecordMapper;
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    
    // 缓存键前缀
    private static final String USER_CACHE_PREFIX = "sch:user:";
    // 缓存过期时间：30 分钟
    private static final long USER_CACHE_TTL = 30;
    /**
     * 用户登录
     *
     * @param loginDTO
     * @return
     */
    @Override
    public LoginUserVO login(LoginDTO loginDTO) {
        SysUser user = null;
        Integer type = loginDTO.getLoginType();
        if (type == LoginConstant.TYPE_USERNAME) {
            user = userMapper.selectByUsername(loginDTO.getAccount());
        } else if (type == LoginConstant.TYPE_PHONE) {
            user = userMapper.selectByPhone(loginDTO.getAccount());
        } else if (type == LoginConstant.TYPE_ID_CARD) {
            // 对输入的身份证号码进行加密后再查询
            String encryptedIdCard = cryptoUtil.encodeIdCard(loginDTO.getAccount());
            user = userMapper.selectByIdCard(encryptedIdCard);
        }
        if (user == null || user.getIsDeleted() == AccountConstant.IS_DELETE) {
            throw new BaseException("用户不存在");
        }
        if (user.getStatus() == AccountConstant.STATUS_DISABLE) {
            throw new BaseException("用户已禁用");
        }

        // 使用MD5验证密码
        if (!cryptoUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BaseException("密码错误");
        }

        // 生成 JWT
        String token = JwtUtil.generateToken(user.getRoleType(), user.getId(), jwtProperties.getSecretKey(), jwtProperties.getTtl());

        // 封装返回数据
        return LoginUserVO.builder().id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .roleType(user.getRoleType())
                .token(token)
                .build();
    }

    /**
     * 根据 ID 获取管理员信息
     *
     * @param id
     * @return
     */

    @Override
    public AdminDetailVO getAdminById(Long id) {
        // 先从 Redis 缓存获取
        String cacheKey = USER_CACHE_PREFIX + "admin:" + id;
        AdminDetailVO adminDetailVO = (AdminDetailVO) redisTemplate.opsForValue().get(cacheKey);
        
        if (adminDetailVO == null) {
            // 缓存未命中，从数据库查询
            SysUser user = userMapper.selectById(id);
            if (user != null && user.getIsDeleted() != AccountConstant.IS_DELETE) {
                adminDetailVO = new AdminDetailVO();
                BeanUtils.copyProperties(user, adminDetailVO);
                // 存入缓存，设置过期时间
                redisTemplate.opsForValue().set(cacheKey, adminDetailVO, USER_CACHE_TTL, TimeUnit.MINUTES);
            }
        }
        
        return adminDetailVO;
    }

    /**
     * 根据 ID 获取医生信息
     *
     * @param id
     * @return
     */

    @Override
    public DoctorDetailVO getDoctorById(Long id) {
        // 先从 Redis 缓存获取
        String cacheKey = USER_CACHE_PREFIX + "doctor:" + id;
        DoctorDetailVO doctorDetailVO = (DoctorDetailVO) redisTemplate.opsForValue().get(cacheKey);
        
        if (doctorDetailVO == null) {
            // 缓存未命中，从数据库查询
            doctorDetailVO = userMapper.selectDoctorById(id);
            if (doctorDetailVO != null) {
                // 直接缓存 VO 对象
                redisTemplate.opsForValue().set(cacheKey, doctorDetailVO, USER_CACHE_TTL, TimeUnit.MINUTES);
            }
        }
        
        return doctorDetailVO;
    }

    /**
     * 根据 ID 获取居民信息
     *
     * @param id
     * @return
     */

    @Override
    public ResidentDetailVO getResidentById(Long id) {
        // 先从 Redis 缓存获取
        String cacheKey = USER_CACHE_PREFIX + "resident:" + id;
        ResidentDetailVO residentDetailVO = (ResidentDetailVO) redisTemplate.opsForValue().get(cacheKey);
        
        if (residentDetailVO == null) {
            // 缓存未命中，从数据库查询
            residentDetailVO = userMapper.selectResidentById(id);
            if (residentDetailVO != null) {
                // 直接缓存 VO 对象
                redisTemplate.opsForValue().set(cacheKey, residentDetailVO, USER_CACHE_TTL, TimeUnit.MINUTES);
            }
            if (residentDetailVO == null) throw new BaseException("当前账号不可用！");
        }
        
        return residentDetailVO;
    }

    /**
     * 社区居民注册
     *
     * @param residentRegisterDTO
     */
    @Transactional
    @Override
    public void registerResident(ResidentRegisterDTO residentRegisterDTO) {
        // 对身份证号码进行加密
        String encryptedIdCard = cryptoUtil.encodeIdCard(residentRegisterDTO.getIdCard());

        SysUser sysUser = SysUser.builder()
                .username(residentRegisterDTO.getUsername())
                .phone(residentRegisterDTO.getPhone())
                .idCard(encryptedIdCard)
                .password(cryptoUtil.encodePassword(residentRegisterDTO.getPassword()))
                .avatarUrl(residentRegisterDTO.getAvatarUrl())
                .status(AccountConstant.STATUS_NORMAL)
                .isDeleted(AccountConstant.NOT_DELETE)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .roleType(AccountConstant.ROLE_RESIDENT)
                .build();

        try {
            userMapper.insert(sysUser);
        } catch (Exception e) {
            throw new BaseException("用户已存在");
        }
        ResidentProfile residentProfile = ResidentProfile.builder()
                .userId(sysUser.getId())
                .name(residentRegisterDTO.getName())
                .gender(residentRegisterDTO.getGender())
                .age(residentRegisterDTO.getAge())
                .contact(residentRegisterDTO.getContact())
                .address(residentRegisterDTO.getAddress())
                .createTime(LocalDateTime.now())
                .build();
        residentMapper.insert(residentProfile);
        OperationLog operationLog = OperationLog.builder()
                .userId(sysUser.getId())
                .roleType(AccountConstant.ROLE_RESIDENT)
                .methodName("com.tyut.service.impl.UserServiceImpl.registerResident")
                .moduleName(ModuleConstant.USER_REGISTER.getDescription())
                .createTime(LocalDateTime.now())
                .build();
        operationLogMapper.insert(operationLog);
        HealthRecord healthRecord = HealthRecord.builder()
                .residentId(sysUser.getId())
                .title(residentProfile.getName() + "健康档案")
                .updateTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .isDeleted(AccountConstant.NOT_DELETE)
                .build();
        healthRecordMapper.insert(healthRecord);
    }

    /**
     * 更新管理员
     *
     * @param updateProfileDTO
     */
    @Override
    public void updateAdmin(UpdateProfileDTO updateProfileDTO) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(updateProfileDTO, sysUser);
        try{
            userMapper.updateById(sysUser);
            // 清除缓存
            clearUserCache(updateProfileDTO.getId());
        }catch (Exception e){
            throw new BaseException("用户名或电话号码重复！");
        }
    }

    /**
     * 更新医生
     *
     * @param updateProfileDTO
     */
    @Override
    public void updateDoctor(UpdateProfileDTO updateProfileDTO) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(updateProfileDTO, sysUser);
        userMapper.updateById(sysUser);
        // 清除缓存
        clearUserCache(updateProfileDTO.getId());
    }

    /**
     * 居民信息更新
     *
     * @param updateProfileDTO
     */

    @Transactional
    @Override
    public void updateResident(UpdateProfileDTO updateProfileDTO) {
        if (updateProfileDTO.judgeUser()) {
            SysUser sysUser = new SysUser();
            BeanUtils.copyProperties(updateProfileDTO, sysUser);
            System.out.println(sysUser);
            userMapper.updateById(sysUser);
        }
        if (updateProfileDTO.judgeResident()) {
            LambdaUpdateWrapper<ResidentProfile> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ResidentProfile::getUserId, updateProfileDTO.getId());
            // 使用条件判断只设置非 null 字段
            Optional.ofNullable(updateProfileDTO.getName()).ifPresent(name -> wrapper.set(ResidentProfile::getName, name));
            Optional.ofNullable(updateProfileDTO.getGender()).ifPresent(gender -> wrapper.set(ResidentProfile::getGender, gender));
            Optional.ofNullable(updateProfileDTO.getAge()).ifPresent(age -> wrapper.set(ResidentProfile::getAge, age));
            Optional.ofNullable(updateProfileDTO.getContact()).ifPresent(contact -> wrapper.set(ResidentProfile::getContact, contact));
            Optional.ofNullable(updateProfileDTO.getAddress()).ifPresent(address -> wrapper.set(ResidentProfile::getAddress, address));
            residentMapper.update(null, wrapper);
        }
        // 清除缓存
        clearUserCache(updateProfileDTO.getId());
    }

    /**
     * 更新密码
     *
     * @param oldPassword
     * @param newPassword
     */
    @DataBackUp(module = ModuleConstant.USER_UPDATE_PASSWORD)
    @Transactional
    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        Long id = BaseContext.get().getId();
        SysUser sysUser = userMapper.selectById(id);
        if (!cryptoUtil.matches(oldPassword, sysUser.getPassword())) {
            throw new BaseException("密码错误");
        }
        sysUser.setPassword(cryptoUtil.encodePassword(newPassword));
        userMapper.updateById(sysUser);
        // 清除缓存
        clearUserCache(id);
    }
    @DataBackUp(module = ModuleConstant.USER_RESET_PASSWORD)
    @Override
    public void resetPassword(Long userId) {
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, userId)
                .set(SysUser::getPassword, cryptoUtil.encodePassword("123456"));
        userMapper.update(null, wrapper);
        // 清除缓存
        clearUserCache(userId);
    }

    /**
     * 启用禁用
     *
     * @param id
     * @param status
     */
    @DataBackUp(module = ModuleConstant.USER_STATUS_CHANGE)
    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser sysUser = SysUser.builder()
                .id(id)
                .status(status)
                .build();
        userMapper.updateById(sysUser);
        // 清除缓存
        clearUserCache(id);
    }

    /**
     * 删除用户
     *
     * @param id
     */
    @DataBackUp(module = ModuleConstant.USER_DELETE)
    @Override
    public void deleteUser(Long id) {
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, id)
                .set(SysUser::getIsDeleted, AccountConstant.IS_DELETE);
        userMapper.update(null, wrapper);
        // 清除缓存
        clearUserCache(id);
    }

    /**
     * 撤销删除
     *
     * @param id
     */
    @Override
    public void revertUser(Long id) {
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysUser::getId, id)
                .set(SysUser::getIsDeleted, AccountConstant.NOT_DELETE);
        userMapper.update(null, wrapper);
        // 清除缓存
        clearUserCache(id);
    }

    @Override
    public void logout() {
        // 清除当前线程的用户上下文
        BaseContext.remove();
        WebSocketContext.remove();
    }

    @Override
    public PageResult list(AccountQueryDTO accountQueryDTO) {
        Page<SysUser> page = new Page<>(accountQueryDTO.getPageNum(), accountQueryDTO.getPageSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getIsDeleted, accountQueryDTO.getIsDeleted());
        if(accountQueryDTO.getStatus()!=null){
            wrapper.eq(SysUser::getStatus, accountQueryDTO.getStatus());
        }
        if(accountQueryDTO.getRole()!=null){
            wrapper.eq(SysUser::getRoleType, accountQueryDTO.getRole());
        }
        if(accountQueryDTO.getUsername()!=null){
            wrapper.eq(SysUser::getUsername,accountQueryDTO.getUsername());
        }
        if(accountQueryDTO.getPhone()!=null){
            wrapper.eq(SysUser::getPhone,accountQueryDTO.getPhone());
        }
        IPage<SysUser> sysUserPage = userMapper.selectPage(page, wrapper);
        List<SysUser> records = sysUserPage.getRecords();
        records.forEach(sysUser -> sysUser.setIdCard(cryptoUtil.decodeIdCard(sysUser.getIdCard())));
        return PageResult.builder()
                .total(sysUserPage.getTotal())
                .dataList(records)
                .build();
    }

    @Override
    public String getRealNameById(Long id) {
        return residentMapper.selectRealNameById(id);
    }

    @Override
    public PageResult myPatient(Integer pageNum, Integer pageSize, Long doctorId) {
        Page<HealthRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Appointment> appointWrapper=new LambdaQueryWrapper<>();
        appointWrapper.eq(Appointment::getDoctorId,doctorId)
                .eq(Appointment::getAppointmentStatus, AppointConstant.FINSHED)
                .eq(Appointment::getVisitStatus, AppointConstant.FINSHED);
        List<Appointment> appointments = appointmentMapper.selectList(appointWrapper);
        List<Long> patientIds = appointments.stream().map(Appointment::getResidentId).collect(Collectors.toList());
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getIsDeleted, 0)
                .in(HealthRecord::getResidentId, patientIds);
        IPage<HealthRecord> pageResult=healthRecordMapper.selectPage(page, wrapper);
        return PageResult.builder()
                .total(pageResult.getTotal())
                .dataList(pageResult.getRecords())
                .build();
    }

    /**
     * 清除用户缓存
     * @param id 用户 ID
     */
    private void clearUserCache(Long id) {
        // 查询用户信息以获取角色类型
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            Integer roleType = user.getRoleType();
            String cacheKey;
            
            // 根据角色类型清除对应的缓存
            if (roleType == AccountConstant.ROLE_ADMIN) {
                cacheKey = USER_CACHE_PREFIX + "admin:" + id;
            } else if (roleType == AccountConstant.ROLE_DOCTOR) {
                cacheKey = USER_CACHE_PREFIX + "doctor:" + id;
            } else if (roleType == AccountConstant.ROLE_RESIDENT) {
                cacheKey = USER_CACHE_PREFIX + "resident:" + id;
            } else {
                log.warn("未知角色类型：{}, userId: {}", roleType, id);
                return;
            }
            
            redisTemplate.delete(cacheKey);
            log.debug("清除用户缓存：key={}, roleType={}", cacheKey, roleType);
        } else {
            log.warn("用户不存在，无法清除缓存，userId: {}", id);
        }
    }

}

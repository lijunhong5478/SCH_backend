package com.tyut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tyut.constant.AccountConstant;
import com.tyut.constant.ModuleConstant;
import com.tyut.dto.AddDoctorDTO;
import com.tyut.dto.DoctorQueryDTO;
import com.tyut.dto.DoctorScheduleUpdateDTO;
import com.tyut.entity.DoctorProfile;
import com.tyut.entity.DoctorSchedule;
import com.tyut.entity.SysUser;
import com.tyut.mapper.DoctorProfileMapper;
import com.tyut.mapper.DoctorScheduleMapper;
import com.tyut.mapper.UserMapper;
import com.tyut.result.PageResult;
import com.tyut.service.DoctorService;
import com.tyut.utils.CryptoUtil;
import com.tyut.vo.DoctorDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DoctorServiceImpl implements DoctorService {
    @Autowired
    private CryptoUtil cryptoUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DoctorProfileMapper doctorProfileMapper;
    @Autowired
    private DoctorScheduleMapper doctorScheduleMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String USER_CACHE_PREFIX = "sch:user:";
    // 缓存过期时间：30 分钟
    private static final long USER_CACHE_TTL = 30;

    /**
     * 添加医生
     *
     * @param addDoctorDTO
     */
    @Transactional
    @Override
    public void registerDoctor(AddDoctorDTO addDoctorDTO) {
        // 对身份证号码进行加密
        String encryptedIdCard = cryptoUtil.encodeIdCard(addDoctorDTO.getIdCard());

        SysUser sysUser = SysUser.builder()
                .username(addDoctorDTO.getUsername())
                .phone(addDoctorDTO.getPhone())
                .idCard(encryptedIdCard)
                .password(cryptoUtil.encodePassword(addDoctorDTO.getPassword()))
                .avatarUrl(addDoctorDTO.getAvatarUrl())
                .status(AccountConstant.STATUS_NORMAL)
                .isDeleted(AccountConstant.NOT_DELETE)
                .roleType(AccountConstant.ROLE_DOCTOR)
                .updateTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .build();
        userMapper.insert(sysUser);
        DoctorProfile doctorProfile = DoctorProfile.builder()
                .userId(sysUser.getId())
                .name(addDoctorDTO.getName())
                .specialty(addDoctorDTO.getSpecialty())
                .title(addDoctorDTO.getTitle())
                .introduction(addDoctorDTO.getIntroduction())
                .departmentId(addDoctorDTO.getDepartmentId())
                .gender(addDoctorDTO.getGender())
                .build();
        doctorProfileMapper.insert(doctorProfile);
        for (DoctorSchedule doctorSchedule : addDoctorDTO.getDoctorSchedules()) {
            doctorSchedule.setDoctorId(sysUser.getId());
            doctorSchedule.setCreateTime(LocalDateTime.now());
            doctorSchedule.setCurrentNumber(0);
            doctorSchedule.setStatus(AccountConstant.STATUS_NORMAL);
            doctorScheduleMapper.insert(doctorSchedule);
        }
    }

    @Transactional
    @Override
    public void doctorSchedule(DoctorScheduleUpdateDTO dto) {
        //删除原本的排班计划
        doctorScheduleMapper.deleteByDoctorId(dto.getDoctorId());
        //插入新的排班计划
        List<DoctorSchedule> doctorSchedules = dto.getDoctorSchedules();
        System.out.println(doctorSchedules);
        for (DoctorSchedule doctorSchedule : doctorSchedules) {
            doctorSchedule.setDoctorId(dto.getDoctorId());
            doctorSchedule.setCreateTime(LocalDateTime.now());
            // 确保 weekDay 字段不为空
            if (doctorSchedule.getWeekDay() == null) {
                throw new IllegalArgumentException("排班信息中的 weekDay 不能为空");
            }
            doctorScheduleMapper.insert(doctorSchedule);
        }
        clearUserCache(dto.getDoctorId());
        // 清除排班缓存
        clearScheduleCache(dto.getDoctorId());
    }

    @Override
    public PageResult list(DoctorQueryDTO queryDTO) {
        // 基础分页查询（含排班过滤）
        Page<DoctorDetailVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<DoctorDetailVO> pageData = doctorProfileMapper.list(page, queryDTO);

        List<DoctorDetailVO> doctors = pageData.getRecords();

        // 为每个医生查询详细的排班信息
        for (DoctorDetailVO doctor : doctors) {
            LambdaQueryWrapper<DoctorSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
            scheduleWrapper.eq(DoctorSchedule::getDoctorId, doctor.getUserId())
                    .eq(DoctorSchedule::getStatus, 1);

            if (queryDTO.getWorkDay() != null) {
                Integer weekDay = queryDTO.getWorkDay().getDayOfWeek().getValue();
                scheduleWrapper.eq(DoctorSchedule::getWeekDay, weekDay);
            }
            if (queryDTO.getTimeSlot() != null) {
                scheduleWrapper.eq(DoctorSchedule::getTimeSlot, queryDTO.getTimeSlot());
            }

            List<DoctorSchedule> schedules = doctorScheduleMapper.selectList(scheduleWrapper);
            doctor.setDoctorSchedules(schedules);
        }

        return PageResult.builder()
                .total(pageData.getTotal())
                .dataList(doctors)
                .build();
    }

    @Override
    public List<DoctorSchedule> getDoctorScheduleById(Long id) {
        // 先从 Redis 缓存获取
        String cacheKey = USER_CACHE_PREFIX + "schedule:" + id;
        List<DoctorSchedule> schedules = (List<DoctorSchedule>) redisTemplate.opsForValue().get(cacheKey);
        
        if (schedules == null) {
            // 缓存未命中，从数据库查询
            LambdaQueryWrapper<DoctorSchedule> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DoctorSchedule::getDoctorId, id);
            schedules = doctorScheduleMapper.selectList(queryWrapper);
            
            if (schedules != null && !schedules.isEmpty()) {
                // 存入缓存，设置过期时间
                redisTemplate.opsForValue().set(cacheKey, schedules, USER_CACHE_TTL, TimeUnit.MINUTES);
            }
        }
        
        return schedules;
    }
    /**
     * 清除医生用户缓存
     * @param doctorId 医生 ID
     */
    private void clearUserCache(Long doctorId) {
        // 医生的缓存键为 sch:user:doctor:{id}
        String cacheKey = USER_CACHE_PREFIX + "doctor:" + doctorId;
        redisTemplate.delete(cacheKey);
        log.debug("清除医生用户缓存：key={}", cacheKey);
    }
    
    /**
     * 清除医生排班缓存
     * @param doctorId 医生 ID
     */
    private void clearScheduleCache(Long doctorId) {
        String cacheKey = USER_CACHE_PREFIX + "schedule:" + doctorId;
        redisTemplate.delete(cacheKey);
        log.debug("清除排班缓存：{}", cacheKey);
    }

}

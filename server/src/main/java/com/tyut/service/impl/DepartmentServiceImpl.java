package com.tyut.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tyut.entity.Department;
import com.tyut.mapper.DepartmentMapper;
import com.tyut.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService{
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    
    private static final String DEPARTMENT_CACHE_KEY = "sch:department:list";
    private static final long DEPARTMENT_CACHE_TTL = 30; // 30 分钟
    @Override
    public List<Department> list(){
        // 先从 Redis 缓存获取
        List<Department> list = (List<Department>) redisTemplate.opsForValue().get(DEPARTMENT_CACHE_KEY);
        
        if(list == null) {
            // 缓存未命中，从数据库查询
            list = departmentMapper.selectList(null);
            if(list != null && !list.isEmpty()) {
                // 存入缓存，设置过期时间
                redisTemplate.opsForValue().set(DEPARTMENT_CACHE_KEY, list, DEPARTMENT_CACHE_TTL, TimeUnit.MINUTES);
                log.debug("科室列表已缓存到 Redis，key: {}", DEPARTMENT_CACHE_KEY);
            }
        } else {
            log.debug("从 Redis 缓存获取科室列表，key: {}", DEPARTMENT_CACHE_KEY);
        }
        return list;
    }
}

package com.itheima.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}

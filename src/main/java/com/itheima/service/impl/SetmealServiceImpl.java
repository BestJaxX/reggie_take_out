package com.itheima.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.mapper.SetmealMapper;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class SetmealServiceImpl  extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService{
	
	
	@Autowired
	private SetmealDishService setmealDishService;

	//新增套餐，同时需要保存套餐和菜品的关联关系
	@Override
	public void saveWithSetmealDish(SetmealDto setmealDto) {
		//保存套餐的数据不包括菜品数据
		this.save(setmealDto);
		
		//获取套餐的菜品数据 但不包含setmeal_id
		List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
		
		//把对应的setmeal_id放入setmealDishes中
		setmealDishes=setmealDishes.stream().map((item)->{
			item.setSetmealId(setmealDto.getId());
			
			return item;
		}).collect(Collectors.toList());
		
		//保存setmealDish到表中
		setmealDishService.saveBatch(setmealDishes);
	}

	//删除套餐，同时需要删除套餐和菜品的关联数据
		
	@Override
	public void removeWithDish(List<Long> ids) {
		//select count(*) from setmeal where id in(1,2,3) and status=1;
		//查询套餐状态，确定是否可以删除
		LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<Setmeal>();
		queryWrapper.in(Setmeal::getId, ids);
		queryWrapper.eq(Setmeal::getStatus, 1);
		int count =this.count(queryWrapper);
		
		if(count >0) {
			//如果不能删除，抛出一个业务异常
			throw new CustomException("套餐正在售卖中，不能删除");
		}
		
		//如果可以删除，先删除套餐表中的数据--Setmeal
		this.removeByIds(ids);
		
		//删除表中的数据
		//delete from setmeal_dish where setmeal_id in（1，2，3）；
		LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper=new LambdaQueryWrapper<SetmealDish>();
		lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
		setmealDishService.remove(lambdaQueryWrapper);
		
	}
	
	
}

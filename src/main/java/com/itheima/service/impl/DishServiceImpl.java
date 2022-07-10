package com.itheima.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.mapper.DishMapper;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

	
	@Autowired
	private DishFlavorService dishFlavorService;
	
	//新增菜品，同时保存对应的口味数据
	public void saveWithFlavor(DishDto dishDto) {
		//保存菜品的基本信息到菜品表dish
		this.save(dishDto);
		
		Long dishId = dishDto.getId();//菜品id
		//菜品口味
		List<DishFlavor> flavors = dishDto.getFlavors();
//可以使用foreach
//		for(DishFlavor flavor:flavors) {
//			flavor.setDishId(dishId);
//		}
		flavors=flavors.stream().map((item)->{
			item.setDishId(dishId);
			return item;
		}).collect(Collectors.toList());
		
		
		//保存菜品口味数据到菜品口味表dish_flavor
		dishFlavorService.saveBatch(dishDto.getFlavors());
		
		
	}

	//根据id查询菜品信息和对应的口味信息
	@Override
	public DishDto getByIdWithFlavor(Long id) {
		//查询菜品基本信息，从dish表查询
		Dish dish = this.getById(id);
		
		DishDto dishDto=new DishDto();
		BeanUtils.copyProperties(dish, dishDto);
		//查询当前菜品对应的口味信息，从dish_flavor表查询
		LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<DishFlavor>();
		queryWrapper.eq(DishFlavor::getDishId, dish.getId());
		List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
		dishDto.setFlavors(flavors);
		
		return dishDto;
	}

	@Override
	@Transactional
	public void updateWithFlavor(DishDto dishDto) {
		//跟新dish表基本信息
		this.updateById(dishDto);
		//清理当前菜品对应口味数据---dish_flavor表的delete操作
		LambdaQueryWrapper<DishFlavor> queryWrapper =new LambdaQueryWrapper<>();
		queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
		dishFlavorService.remove(queryWrapper);
		//添加当前提交过来的口味数据---dish_flavor表的insert操作
		List<DishFlavor> flavors = dishDto.getFlavors();
		
		flavors=flavors.stream().map((item)->{
			item.setDishId(dishDto.getId());
			return item;
		}).collect(Collectors.toList());
		
		dishFlavorService.saveBatch(flavors);
	}

	@Override
	public void removeWithFlavor(List<Long> dis) {
		//查询删除时是否选择了起售商品
		LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<Dish>();
		queryWrapper.in(Dish::getId, dis);
		queryWrapper.eq(Dish::getStatus, 1);
		int count = this.count(queryWrapper);
		
		//如果选择了起售商品，则删除失败
		if(count>0) {
			throw new  CustomException("选择了起售商品,删除失败!");
		}
		//如果没有选择起售商品，则先删除dish表中数据
		this.removeByIds(dis);
		
		//再删除dish_Flavor表中对应的数据
		LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<DishFlavor>();
		lambdaQueryWrapper.in(DishFlavor::getDishId, dis);
		dishFlavorService.remove(lambdaQueryWrapper);
		
	}



}

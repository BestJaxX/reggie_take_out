package com.itheima.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.mapper.CategoryMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealService;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Category> implements CategoryService  {

	@Autowired
	private DishService dishService;
	
	@Autowired
	private SetmealService setmealService;
	
	//根据id删除分类，删除之前需要进行判断
	@Override
	public void remove(Long id) {
		LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<Dish>();
		//添加查询条件、根据分类id进行查询
		dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
		int count1 = dishService.count(dishLambdaQueryWrapper);
		
		
		//查询当前分类是否关联了菜品,如果已经关联，抛出一个业务异常
		if(count1>0) {
			//已经关联菜品，抛出一个业务异常
			throw new CustomException("当前分类下关联了菜品，不能删除");
		}
		
		
		//查询当前分类是否关联了套餐,如果已经关联，抛出一个业务异常
		LambdaQueryWrapper<Setmeal> SetmealLambdaQueryWrapper=new LambdaQueryWrapper<Setmeal>();
		SetmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
		int count2 = setmealService.count(SetmealLambdaQueryWrapper);
		if(count2>0) {
			throw new CustomException("当前分类下关联了套餐，不能删除");
		}
		//正常删除分类
		super.removeById(id);
	}

}

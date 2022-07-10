package com.itheima.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.SetmealDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.service.CategoryService;
import com.itheima.service.SetmealDishService;
import com.itheima.service.SetmealService;

import lombok.extern.slf4j.Slf4j;

//套餐管理
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
	
	@Autowired
	private SetmealDishService setmealDishService;
	
	@Autowired
	private SetmealService setmealService;
	
	@Autowired
	private CategoryService categoryService;
	
	@PostMapping
	public R<String> save(@RequestBody SetmealDto setmealDto){
		setmealService.saveWithSetmealDish(setmealDto);
		
		return R.success("添加套餐成功");
		
	}
	//套餐分页查询
	@GetMapping("/page")
	public R<Page> page(int page,int pageSize,String name){
		//构造分页构造器
		Page<Setmeal> pageInfo=new Page<Setmeal>(page,pageSize);
		Page<SetmealDto> setmealDtoPage=new Page<SetmealDto>();
		//构造条件构造器
		LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<Setmeal>();
		queryWrapper.like(name!=null, Setmeal::getName, name);
		//添加排序条件
		queryWrapper.orderByDesc(Setmeal::getUpdateTime);
		//执行分页查询
		setmealService.page(pageInfo, queryWrapper);
		//对象拷贝
		BeanUtils.copyProperties(pageInfo, setmealDtoPage,"records");
		
		List<Setmeal> records = pageInfo.getRecords();
		List<SetmealDto> list= records.stream().map((item)->{
			SetmealDto setmealDto = new SetmealDto();
			//对象拷贝
			BeanUtils.copyProperties(item, setmealDto);
			Long categoryId = item.getCategoryId();//套餐id
			//根据id查询套餐id
			Category category = categoryService.getById(categoryId);
			if(category!=null) {
			setmealDto.setCategoryName(category.getName());
			}
			return setmealDto;
		}).collect(Collectors.toList());
		
		setmealDtoPage.setRecords(list);
		
		return R.success(setmealDtoPage);
		
	}
	
	//删除套餐
	@DeleteMapping
	public R<String> delete(@RequestParam List<Long> ids){
		
		setmealService.removeWithDish(ids);
		return R.success("套餐数据删除成功");
		
	}
	
	//停售起售套餐
	@PostMapping("/status/{status}")
	public R<String> status(@PathVariable int status, @RequestParam List<Long> ids){
		//列出已勾选的菜品
		LambdaQueryWrapper<Setmeal> queryWrapper =new LambdaQueryWrapper<Setmeal>();
		queryWrapper.in(Setmeal::getId, ids);
		
		List<Setmeal> list = setmealService.list(queryWrapper);
		
		list.stream().map((item)->{
			item.setStatus(status);
			return item;
		}).collect(Collectors.toList());
		
		setmealService.updateBatchById(list);
		return R.success("状态修改成功");
		
	}
	
	

	//根据条件查询套餐数据
	@GetMapping("/list")
	public R<List<Setmeal>> list(Setmeal setmeal){
		//条件构造器
		LambdaQueryWrapper<Setmeal> queryWrapper =new LambdaQueryWrapper<Setmeal>();
		//添加条件
		queryWrapper.eq(setmeal.getCategoryId()!=null, Setmeal::getCategoryId, setmeal.getCategoryId());
		queryWrapper.eq(setmeal.getStatus()!=null, Setmeal::getStatus, setmeal.getStatus());
		//添加排序条件
		queryWrapper.orderByDesc(Setmeal::getUpdateTime);
		
		 List<Setmeal> list = setmealService.list(queryWrapper);
		
		return R.success(list);
		
	}
	
}

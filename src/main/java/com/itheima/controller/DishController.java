package com.itheima.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.dto.DishDto;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.entity.Setmeal;
import com.itheima.entity.SetmealDish;
import com.itheima.service.CategoryService;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;

import lombok.extern.slf4j.Slf4j;

//菜品管理
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
	@Autowired
	private DishService dishService;
	@Autowired
	private DishFlavorService dishFlavorService;
	
	@Autowired
	private CategoryService categoryService;
	
	//新增菜品
	@PostMapping
	public R<String> save(@RequestBody DishDto dishDto ){
		
		log.info(dishDto.toString());
		
		dishService.saveWithFlavor(dishDto);

		return R.success("新增菜品成功");
		
	}
	
	//菜品信息分页查询
	@GetMapping("/page")
	public R<Page> page(int page,int pageSize,String name){
		
		//构造分页构造器对象
		Page<Dish> pageInfo = new Page<Dish>(page,pageSize);
		Page<DishDto> dishDtoPage=new Page<>();
		
		//条件构造器
		LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<Dish>();
		
		//添加过滤器条件
		lambdaQueryWrapper.like(name!=null,Dish::getName,name);
		
		//添加排序条件
		lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
		
		//执行分页查询
		dishService.page(pageInfo,lambdaQueryWrapper);
		
		//对象拷贝
		BeanUtils.copyProperties(pageInfo, dishDtoPage,"records");
		
		List<Dish> records = pageInfo.getRecords();
		List<DishDto> list=records.stream().map((item) -> {
			DishDto dishDto=new DishDto();
			
			BeanUtils.copyProperties(item, dishDto);
			
			
			Long categoryId=item.getCategoryId();//分类id
			//根据id查询分类对象
			Category category = categoryService.getById(categoryId);
			if(category!=null) {
				String categoryName = category.getName();
				dishDto.setCategoryName(categoryName);
			}
			
			
			return dishDto;
		}).collect(Collectors.toList());
		
		
		
		
		dishDtoPage.setRecords(list);
		
		return R.success(dishDtoPage);
		
	}
	
	
	//根据id查询菜品信息和对应的口味信息
	@GetMapping("/{id}")
	public R<DishDto> get(@PathVariable Long id){
		
		DishDto dishDto = dishService.getByIdWithFlavor(id);
		
		return R.success(dishDto);
		
	}
	
	//修改菜品
	@PutMapping
	public R<String> update(@RequestBody DishDto dishDto ){
		
		log.info(dishDto.toString());
		
		dishService.updateWithFlavor(dishDto);

		return R.success("修改菜品成功");
		
	}
	
//	//根据条件查询对应的菜品数据
//	@GetMapping("/list")
//	public R<List<Dish>> list(Dish dish){
//		//构造查询条件
//		LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<Dish>();
//		queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//		//添加条件、查询状态为1（起售状态）的菜品
//		queryWrapper.eq(Dish::getStatus, 1);
//		//添加排序条件
//		queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//		
//		List<Dish> list = dishService.list(queryWrapper);
//		return R.success(list);
//		
//	}
	
	//根据条件查询对应的菜品数据
	@GetMapping("/list")
	public R<List<DishDto>> list(Dish dish){
		//构造查询条件
		LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<Dish>();
		queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
		//添加条件、查询状态为1（起售状态）的菜品
		queryWrapper.eq(Dish::getStatus, 1);
		//添加排序条件
		queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
		
		List<Dish> list = dishService.list(queryWrapper);
		
		
		List<DishDto> dishDtoList=list.stream().map((item) -> {
			DishDto dishDto=new DishDto();
			
			BeanUtils.copyProperties(item, dishDto);
			
			
			Long dishId=item.getId();//当前菜品id
			//根据id查询口味对象
			LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
			lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
			 List<DishFlavor> flavors = dishFlavorService.list(lambdaQueryWrapper);
			if(flavors!=null) {
				dishDto.setFlavors(flavors);
			}
			return dishDto;
		}).collect(Collectors.toList());
		
		return R.success(dishDtoList);
		
	}
	
	@DeleteMapping
	public R<String> delete(@RequestParam List<Long> ids){
		dishService.removeWithFlavor(ids);
		return R.success("菜品删除成功");
		
	}
	
	//停售起售菜品
	@PostMapping("/status/{status}")
	public R<String> status(@PathVariable int status, @RequestParam List<Long> ids){
		//列出已勾选的菜品
		LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<Dish>();
		queryWrapper.in(Dish::getId, ids);
		
		List<Dish> list = dishService.list(queryWrapper);
		
		list.stream().map((item)->{
			item.setStatus(status);
			return item;
		}).collect(Collectors.toList());
		
		dishService.updateBatchById(list);
		return R.success("状态修改成功");
		
	}
	
	
}

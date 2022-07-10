package com.itheima.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.ShoppingCartService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
	
	@Autowired
	private ShoppingCartService shoppingCartService;
	
	//添加购物车
	@PostMapping("/add")
	public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
		 //设置用户id，指定当前是哪个用户的购物车数据。
		Long currentId = BaseContext.getCurrentId();
		shoppingCart.setUserId(currentId);
		
		
		Long dishId = shoppingCart.getDishId();
		Long setmealId = shoppingCart.getSetmealId();
		LambdaQueryWrapper<ShoppingCart> queryWrapper =new LambdaQueryWrapper<>();
		queryWrapper.eq(ShoppingCart::getUserId, currentId);
		if(dishId!=null) {
			//添加到购物车的是菜品
			queryWrapper.eq(ShoppingCart::getDishId, dishId);
			
		}else {
			//添加到购物车的是套餐
			queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
		}
		//查询当前菜品或者套餐是否在购物车中
		ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
		
		if(cartServiceOne!=null) {
			//如果已经存在，就在原来数量基础上加一
			Integer number=cartServiceOne.getNumber();
			cartServiceOne.setNumber(number+1);
			shoppingCartService.updateById(cartServiceOne);
		}else {
			//如果不存在，则添加到购物车，数量默认就是一
			shoppingCart.setNumber(1);
			shoppingCart.setCreateTime(LocalDateTime.now());
			shoppingCartService.save(shoppingCart);
			
			cartServiceOne=shoppingCart;
		}
		
		return R.success(cartServiceOne);
		
	}
	
	//查看购物车
	@GetMapping("/list")
	public R<List<ShoppingCart>> list(){
		log.info("查看购物车...");
		LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
		queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
		queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
		
		
		List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
		
		return R.success(list);
		
	}
	
	//清空购物车
	@DeleteMapping("/clean")
	public R<String> clean(){
		
		//SQL:delete from shoping_cartwhere user_id=?
		LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
		queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
		shoppingCartService.remove(queryWrapper);
		
		return R.success("清空购物车成功");
		
	}
	
	//减少购物车菜品数量
	@PostMapping("/sub")
	public R<String> sub(@RequestBody ShoppingCart shoppingCart){
		Long dishId = shoppingCart.getDishId();
		Long setmealId = shoppingCart.getSetmealId();
		Integer number=null;
		ShoppingCart cartOne=null;
		//判断是菜品还是套餐
		
		if(dishId!=null) {
			//如果是菜品
			LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
			queryWrapper.eq(ShoppingCart::getDishId,dishId);
			 cartOne = shoppingCartService.getOne(queryWrapper);
			 number = cartOne.getNumber();
			
		}else {
			LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
			queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
			 cartOne = shoppingCartService.getOne(queryWrapper);
			 number = cartOne.getNumber();
		}
		//判断数量是否为1，如果是1则删除，否则number-1；
		if(number<=1) {
			shoppingCartService.removeById(cartOne);
		}else {
			cartOne.setNumber(number-1);
			shoppingCartService.updateById(cartOne);
		}
		
		return R.success("以减少或删除菜品或套餐");
		
	}
	
}

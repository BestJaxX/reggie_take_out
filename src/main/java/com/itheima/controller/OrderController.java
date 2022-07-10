package com.itheima.controller;


import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.common.R;
import com.itheima.entity.Orders;
import com.itheima.service.OrderService;

import lombok.extern.slf4j.Slf4j;

//订单

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
	@Autowired
	private OrderService orderService;
	
	//用户下单
	@PostMapping("/submit")
	public R<String> submit(@RequestBody Orders orders){
		log.info("订单数据{}",orders);
		orderService.submit(orders);
		return R.success("下单成功");
		
	}
	//手机端订单
	@GetMapping("/userPage")
	public R<Page> userPage(int page, int pageSize){
		//构造分页构造器
		Page<Orders> pageInfo=new Page<Orders>(page,pageSize);
		
		//条件构造器
		LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<Orders>();
		//添加排序条件
		queryWrapper.orderByDesc(Orders::getOrderTime);
		
		//执行分页查询
		orderService.page(pageInfo,queryWrapper);
		
		
		return R.success(pageInfo);
		
	}
	
	//电脑端订单
		@GetMapping("/page")
		public R<Page> Page(int page, int pageSize,Long number,
				@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date beginTime,
				@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime){
			//构造分页构造器
			Page<Orders> pageInfo=new Page<Orders>(page,pageSize);
			
			//条件构造器
			LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<Orders>();
			
			//添加查询条件
			queryWrapper.eq(number!=null, Orders::getId, number);
			if(beginTime!=null&&endTime!=null) {
			queryWrapper.between(Orders::getOrderTime, beginTime, endTime);
			}
			//添加排序条件
			queryWrapper.orderByDesc(Orders::getOrderTime);
			
			//执行分页查询
			orderService.page(pageInfo,queryWrapper);
			
			
			return R.success(pageInfo);
			
		}
	
}

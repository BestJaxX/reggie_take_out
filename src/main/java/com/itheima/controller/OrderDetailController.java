package com.itheima.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itheima.service.OrderDetailService;

import lombok.extern.slf4j.Slf4j;

//订单明细

@Slf4j
@RestController
@RequestMapping("/orderDeatil")
public class OrderDetailController {
	
	@Autowired
	private OrderDetailService orderDetailService;
	
}

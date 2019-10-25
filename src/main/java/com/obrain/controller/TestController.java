package com.obrain.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import com.obrain.annotation.MyAutowired;
import com.obrain.annotation.MyController;
import com.obrain.annotation.MyRequestMapping;
import com.obrain.service.TestService;

@MyController
@MyRequestMapping("/test")
public class TestController {
	
	@MyAutowired("/testService")
	TestService testService;
	
	
	@MyRequestMapping("/index")
	public String index() {
		return "Test springMVC:"+testService.test();
	}
	
	@MyRequestMapping("/index2")
	public String index2() {
		return "Test springMVC:"+testService.test();
	}
}

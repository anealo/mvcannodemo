package com.obrain.service;

import com.obrain.annotation.MyService;

@MyService("/testService")
public class TestService {
	
	public String test() {
		System.out.println("执行业务逻辑--------");
		return "TestService.test()---";
	}
}

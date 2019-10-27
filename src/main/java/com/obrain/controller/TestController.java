package com.obrain.controller;

import javax.servlet.http.HttpServletRequest;

import com.obrain.annotation.MyAutowired;
import com.obrain.annotation.MyController;
import com.obrain.annotation.MyRequestMapping;
import com.obrain.annotation.MyRequestParam;
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
	
	@MyRequestMapping("/user")
	public String index2(@MyRequestParam String name,@MyRequestParam String age) {
		return "姓名：:"+name+",年龄："+age;
	}
	
	@MyRequestMapping("/info")
	public String index3(@MyRequestParam String name,@MyRequestParam String age,@MyRequestParam HttpServletRequest req) {
		System.out.println("ContextPath:"+ req.getContextPath()+"\r\n"+
		"CharacterEncodin:"+ req.getCharacterEncoding()+"\r\n"+
//		"AsyncContext:"+req.getAsyncContext()+"\r\n"+
		"ContentLength:"+req.getContentLength()+"\r\n"+
		"ContentType:"+req.getContentType()+"\r\n"+
//		"HttpServletMapping:"+req.getHttpServletMapping()+"\r\n"+   //java.lang.NoSuchMethodError
		"LocalAddr:"+req.getLocalAddr()+"\r\n"+
		"Locale:"+req.getLocale()+"\r\n"+
		"LocalName:"+req.getLocalName()+"\r\n"+
		"Method:"+req.getMethod()+"\r\n"+
		"ParameterMap:"+req.getParameterMap()+"\r\n"+
		"ParameterNames:"+req.getParameterNames()+"\r\n"+
		"HeaderNames:"+req.getHeaderNames()+"\r\n"+
		"PathInfo:"+req.getPathInfo()+"\r\n"+
		"QueryString:"+req.getQueryString()+"\r\n"+
		"RemoteAddr:"+req.getRemoteAddr()+"\r\n"+
		"RemoteHost:"+req.getRemoteHost()+"\r\n"+
		"RemotePort:"+req.getRemotePort()+"\r\n"+
		"RequestedSessionId:"+req.getRequestedSessionId()+"\r\n"+
		"RequestURI:"+req.getRequestURI()+"\r\n"+
		"RequestURL:"+req.getRequestURL()+"\r\n"+
		"Scheme:"+req.getScheme()+"\r\n"+
		"ServerName:"+req.getServerName()+"\r\n"+
		"ServerPort:"+req.getServerPort()+"\r\n"+
		"ServletContext:"+req.getServletContext()+"\r\n"+
		"ServletPath:"+req.getServletPath()+"\r\n"+
		"Session:"+req.getSession()
		);
		return "姓名：:"+name+",年龄："+age;
	}
}

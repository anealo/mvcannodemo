package com.obrain.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.obrain.annotation.MyAutowired;
import com.obrain.annotation.MyController;
import com.obrain.annotation.MyRequestMapping;
import com.obrain.annotation.MyService;

/**
 * Servlet implementation class MyDispatcherServlet
 */
public class MyDispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	// 包扫描得到的所有类名，如：com.obrain.controller.TestController.class
	List<String> clazzNames = new ArrayList<String>();
	// 类映射路径和类实例化的映射关系，如/test对应TestController，/testService对应TestService
	Map<String, Object> beans = new HashMap<String, Object>();
	// 路径和方法的映射关系，如（在TestController中）/test/index对应index()
	Map<String, Object> handlerMethod = new HashMap<String, Object>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MyDispatcherServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init() throws ServletException {
		System.out.println("-------init MyDispatcherServlet---------------------");
		// 1.扫描包
		scanPackage("com.obrain");
		for (String name : clazzNames) {
			System.out.println("扫描到的类名：" + name);
		}
		// 2.类实例化
		classInstance();

		// 3.完成整自动注入的装入
		inject();

		// 4.完成url和controller中method的映射
		handlerMapping();

	}

	private void handlerMapping() {
		if (beans.isEmpty()) {
			System.out.println("没有实例化的类");
			return;
		}
		for (Map.Entry<String, Object> entry : beans.entrySet()) {
			// 从集合中获取类实例
			Object instance = entry.getValue();
			if (instance.getClass().isAnnotationPresent(MyController.class)) {
				MyRequestMapping requestMapping = instance.getClass().getAnnotation(MyRequestMapping.class);
				// 获取类上的requestMapping中个 定义的路径值=>@RequestMapping("/test")
				String path = requestMapping.value();

				// 获取类成员属性
				Method[] methods = instance.getClass().getDeclaredMethods();
				for (Method method : methods) {
					// 获取方法上的yRequestMapping("/index")
					MyRequestMapping methodMapping = method.getAnnotation(MyRequestMapping.class);
					String value = methodMapping.value();
					// 类上的路径+方法上的路径
					handlerMethod.put(path + value, method);

				}
			}
		}
	}

	private void inject() {
		if (beans.isEmpty()) {
			System.out.println("没有实例化的类");
			return;
		}
		for (Map.Entry<String, Object> entry : beans.entrySet()) {
			// 从集合中获取类实例
			Object instance = entry.getValue();
			Field[] fields = instance.getClass().getDeclaredFields();
			for (Field field : fields) {
				// 属性上是否有批注@MyAutowired
				if (field.isAnnotationPresent(MyAutowired.class)) {
					MyAutowired autowired = field.getAnnotation(MyAutowired.class);
					String value = autowired.value();
					field.setAccessible(true);
					try {
						// 最关键的一行，完成依赖注入
						field.set(instance, beans.get(value));

					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void classInstance() {
		if (clazzNames.isEmpty()) {
			System.out.println("没有扫描到任何包");
			return;
		}

		for (String name : clazzNames) {
			String realName = name.replace(".class", "");
			try {
				Class<?> clazz = Class.forName(realName);
				if (clazz.isAnnotationPresent(MyController.class)) {
					MyController controller = (MyController) clazz.getAnnotation(MyController.class);
					// 完成实例化
					Object instance = clazz.newInstance();
					MyRequestMapping requestMappering = (MyRequestMapping) clazz.getAnnotation(MyRequestMapping.class);
					String mappingValue = requestMappering.value();

					// 把类映射路径和类实例化进行绑定
					beans.put(mappingValue, instance);

				}
				if (clazz.isAnnotationPresent(MyService.class)) {
					MyService service = (MyService) clazz.getAnnotation(MyService.class);
					// 完成实例化
					Object instance = clazz.newInstance();
					// 把类映射路径和类实例化进行绑定
					beans.put(service.value(), instance);

				}
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	private void scanPackage(String basePackage) {
		// com.obrain=>‪E:/SpringToolSuiteDemo/mvcannodemo/src/main/java/com/obrain
		String path = basePackage.replaceAll("\\.", "/");
		URL url = getClass().getClassLoader().getResource(path);
		// 目录递归扫描
		String filePath = url.getFile();
//		System.out.println("filePath:"+filePath);
		File[] files = new File(filePath).listFiles();
		for (File file : files) {
			// 如果file是目录就递归
			if (file.isDirectory()) {
//				System.out.println(file.getPath());
				scanPackage(basePackage + "." + file.getName());
			} else {
				// 类以点分隔的
				clazzNames.add(basePackage + "." + file.getName());
			}
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String uri = request.getRequestURI();
		String contextPath = request.getContextPath();
		// mvcannodemo/test/index=>/test/index
		String path = uri.replaceAll(contextPath, "");
		Method method = (Method) handlerMethod.get(path);
		Object instance = beans.get("/" + path.split("/")[1]);
		try {
			if(method==null) {
				response.getWriter().write("404 NOT FOUND!");
				return ;
			}
			// 获取参数
			Object[] params = getRequestParam(request, response);

			Object obj = method.invoke(instance, params);
			System.out.println(obj);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private Object[] getRequestParam(HttpServletRequest req, HttpServletResponse resp) {
		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		url = url.replace(contextPath, "").replaceAll("/+", "/");

		Method method = (Method) handlerMethod.get(url);
		// 获取方法的参数列表
		Class<?>[] parameterTypes = method.getParameterTypes();
		Parameter[] methodParams = method.getParameters();
		// 获取请求的参数

		Map<String, String[]> requestParameterMap = req.getParameterMap();

		// 保存参数值

		Object[] invokeParams = new Object[parameterTypes.length];
		for(int i = 0; i < methodParams.length; i++) {
			String methodParamName = methodParams[i].getName();
			Type methodParamType = methodParams[i].getParameterizedType();
//			System.out.println("获取的methodParamType："+methodParamType);//class java.lang.String	     interface javax.servlet.http.HttpServletRequest
//			Type methodParamType1 = methodParams[i].getType();
//			System.out.println("获取的methodParamType1："+methodParamType1);//class java.lang.String    interface javax.servlet.http.HttpServletRequest
//			System.out.println("TypeName:"+methodParamType.getTypeName());//java.lang.String
//			System.out.println("Class:"+methodParamType.getClass());//class java.lang.Class
//			System.out.println("toString:"+methodParamType.toString());//class java.lang.String
			if("java.lang.String".equals(methodParamType.getTypeName())) {
				for (Entry<String, String[]> param : requestParameterMap.entrySet()) {
					  if(methodParamName.equals(param.getKey())) {
						  
						  String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","").replaceAll(",\\s", ",");
						  
						  invokeParams[i] = value;
						  break;
					  }
				  }
			  
			}else if("javax.servlet.http.HttpServletRequest".equals(methodParamType.getTypeName())) {
				invokeParams[i] = req;
				continue;
			}else if("javax.servlet.http.HttpServletResponse".equals(methodParamType.getTypeName())) {
				invokeParams[i] = resp;
				continue;
			}
			
		}
		// 方法的参数列表
		/*
		 * for (int i = 0; i < parameterTypes.length; i++) { // 根据参数名称，做某些处理
		 * 
		 * String methodParamType = parameterTypes[i].getSimpleName();
		 * 
		 * if (methodParamType.equals("HttpServletRequest")) {
		 * 
		 * // 参数类型已明确，这边强转类型
		 * 
		 * invokeParams[i] = req;
		 * 
		 * continue;
		 * 
		 * }
		 * 
		 * if (methodParamType.equals("HttpServletResponse")) {
		 * 
		 * invokeParams[i] = resp;
		 * 
		 * continue;
		 * 
		 * }
		 * 
		 * if (methodParamType.equals("String")) {
		 * 
		 * // JSONArray jSONArray = (JSONArray) JSONArray.toJSON(requestParameterMap);
		 * // System.out.println("第一次jSONArray:"+jSONArray);
		 * System.out.println(JSON.toJSONString(requestParameterMap)); String mapStr =
		 * JSON.toJSONString(requestParameterMap,
		 * SerializerFeature.BrowserCompatible).replace("\\\\u", "\\u"); // JSONArray
		 * arr = JSONArray.parseArray(mapStr); // System.out.println("第二次arr:"+arr);
		 * 
		 * JSONObject jsonObject = JSON.parseObject(mapStr);
		 * System.out.println("第三次转为jsonObject："+jsonObject); }
		 * 
		 * if (methodParamType.equals("String")) {
		 * 
		 * for (Entry<String, String[]> param : requestParameterMap.entrySet()) {
		 * 
		 * String value =
		 * Arrays.toString(param.getValue()).replaceAll("\\[|\\]","").replaceAll(",\\s",
		 * ",");
		 * 
		 * invokeParams[i] = value; if(i <= parameterTypes.length-2) { i++; } }
		 * 
		 * }
		 * 
		 * }
		 */
		return invokeParams;
	}

}

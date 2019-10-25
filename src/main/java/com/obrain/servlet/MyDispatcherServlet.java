package com.obrain.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.obrain.annotation.MyAutowired;
import com.obrain.annotation.MyController;
import com.obrain.annotation.MyRequestMapping;
import com.obrain.annotation.MyService;

/**
 * Servlet implementation class MyDispatcherServlet
 */
public class MyDispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	List<String> clazzNames = new ArrayList<String>();
	Map<String, Object> beans = new HashMap<String, Object>();
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
		
		//3.完整自动注入的装入
		inject();
		
		//4.完成url和controller中method的映射
		handlerMapping();
	}

	private void handlerMapping() {
		if(beans.isEmpty()) {
			System.out.println("没有实例化的类");
			return;
		}
		for(Map.Entry<String,Object> entry : beans.entrySet()) {
			//从集合中获取类实例
			Object instance = entry.getValue();
			if(instance.getClass().isAnnotationPresent(MyController.class)) {
				MyRequestMapping requestMapping = instance.getClass().getAnnotation(MyRequestMapping.class);
				//获取类上的requestMapping中个 定义的路径值=>@RequestMapping("/test")
				String path = requestMapping.value();
				
				//获取类成员属性
				Method[] methods = instance.getClass().getDeclaredMethods();
				for(Method method : methods) {
					//获取方法上的yRequestMapping("/index")
					MyRequestMapping methodMapping = method.getAnnotation(MyRequestMapping.class);
					String value = methodMapping.value();
					//类上的路径+方法上的路径
					handlerMethod.put(path+value, method);
					
				}
			}
		}
	}

	private void inject() {
		if(beans.isEmpty()) {
			System.out.println("没有实例化的类");
			return;
		}
		for(Map.Entry<String,Object> entry : beans.entrySet()) {
			//从集合中获取类实例
			Object instance = entry.getValue();
			Field[] fields = instance.getClass().getDeclaredFields();
			for(Field field :fields) {
				//属性上是否有批注@MyAutowired
				if(field.isAnnotationPresent(MyAutowired.class)) {
					MyAutowired autowired = field.getAnnotation(MyAutowired.class);
					String value  = autowired.value();
					field.setAccessible(true);
					try {
						//最关键的一行，完成依赖注入
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
		//mvcannodemo/test/index=>/test/index
		String path = uri.replaceAll(contextPath, "");
		Method method = (Method) handlerMethod.get(path);
		Object instance = beans.get("/"+path.split("/")[1]);
		try {
			if(method==null) {
				return;
			}
			Object obj = method.invoke(instance, null);
			System.out.println(obj);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}

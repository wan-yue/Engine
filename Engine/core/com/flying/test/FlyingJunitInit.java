package com.flying.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.flying.builder.TableNameFileForJar;
import com.flying.exception.FlyingException;
import com.flying.init.StaticVariable;
import com.flying.logging.Log;
import com.flying.logging.LogFactory;
import com.flying.service.Engine;

public class FlyingJunitInit {
	private static Log log = LogFactory.getLog(FlyingJunitInit.class);
	
	public static void initEnvironment(){
		InputStream in = null;// 文件输入流
		Properties pp = new Properties();// 数据库属性

		try{
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config/flying.properties");
			pp.load(in);// 将输入流编程属性文件
		}catch(IOException e){
			log.error("flying.properties 配置文件有错误！",e);
		}
		
		String debug = pp.getProperty("flying.DEBUG") == null ? "" : pp
				.getProperty("flying.DEBUG");
		String path = pp.getProperty("flying.PATH") == null ? "" : pp
				.getProperty("flying.PATH");
		String module = pp.getProperty("flying.MODULE") == null ? "" : pp
				.getProperty("flying.MODULE");
		String alias = pp.getProperty("flying.ALIAS") == null ? "" : pp
				.getProperty("flying.ALIAS");
		String authentication = pp.getProperty("flying.AUTHENTICATION") == null ? "" : pp
				.getProperty("flying.AUTHENTICATION");
		String ssoAuth = pp.getProperty("flying.SSO_AUTH") == null ? "" : pp
				.getProperty("flying.SSO_AUTH");
		String flyinglog = pp.getProperty("flying.LOG") == null ? "" : pp
				.getProperty("flying.LOG");
		String rootmenu = pp.getProperty("flying.ROOT_MENU") == null ? "" : pp
				.getProperty("flying.ROOT_MENU");
		try{
			if (!"".equals(debug)
					&& ("true".equals(debug) || "false".equals(debug))) {// 设置调试模式
				StaticVariable.DEBUG = Boolean.parseBoolean(debug);
			} else {
				throw new FlyingException("flying.DEBUG 参数为空或者有误!");
			}
			
			if(StaticVariable.DEBUG){
				if (!"".equals(path)) {// 调试模式下，开发环境地址
					StaticVariable.PATH = path;
				} else {
					throw new FlyingException("flying.PATH 参数为空!");
				}
			}else{
				throw new FlyingException("不在调试模式，运行了junit！");
			}
			
	
			if (!"".equals(module)) {// 生成ibatis配置文件的别名
				StaticVariable.MODULE = module.toLowerCase();
			} else {
				throw new FlyingException("flying.MODULE 参数为空!");
			}
	
			if (!"".equals(alias)) {// 生成ibatis配置文件的别名
				StaticVariable.ALIAS = alias;
			} else {
				throw new FlyingException("flying.ALIAS 参数为空!");
			}
			
			if (!"".equals(authentication)
					&& ("true".equals(authentication) || "false".equals(authentication))) {// 设置调试模式
				StaticVariable.AUTHENTICATION = Boolean.parseBoolean(authentication);
			} else {
				throw new FlyingException("flying.AUTHENTICATION 参数为空或者有误!");
			}
			
			if (!"".equals(ssoAuth)) {//是否开启单点登录
				StaticVariable.SSO_AUTH = Boolean.parseBoolean(ssoAuth);
			}
			
			if (!"".equals(flyinglog)
					&& ("true".equals(flyinglog) || "false".equals(flyinglog))) {// 设置调试模式
				StaticVariable.LOG = Boolean.parseBoolean(flyinglog);
			}
			
			if (!"".equals(rootmenu)) {// 生产的根节点ID
				StaticVariable.ROOT_MENU = rootmenu;
			} 
			
			if(StaticVariable.MAVEN){
				StaticVariable.CONFIG_PATH = "src/main/resources";
				StaticVariable.DEBUG = true;
			}else{
				StaticVariable.CONFIG_PATH = "src";
			}
		}catch(FlyingException e){
			e.printStackTrace();
		}
		
		log.debug("解析flying.properties配置文件完成");
	}
	
	public static void initDB(){
		/**
		 * 在系统初始化的时候，根据用户连接数据的驱动信息，配置用户使用的数据库类型信息
		 */
		InputStream in;// 文件输入流
		Properties pp = new Properties();// 数据库属性
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream("sql/database.properties");// 将文件编程输入流
			pp.load(in);// 将输入流编程属性文件
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String driver = pp.getProperty("driver");// 获取驱动属性

		if (driver.contains("mysql")) {// 驱动中含有mysql，则是mysql数据库
			StaticVariable.DB = "mysql";

		} else if (driver.contains("oracle")) {// 驱动中含有oracle，则是oracle数据库
			StaticVariable.DB = "oracle";

		} else {// 其他则为空字符
			StaticVariable.DB = "";
		}

		log.debug("本系统使用的数据库是：" + StaticVariable.DB);
	}
	
	public static void initContainer() {
		String applicationContext = "config/applicationContext.xml";
		
		/* 构建spring运行环境 */
		ApplicationContext wac = new ClassPathXmlApplicationContext(
				new String[] { applicationContext });
		
		/* 设置引擎运行环境 */
		Engine.ac = wac;
	}
	
	public static void init(){
		if(Engine.ac == null){
			/* 初始化环境参数 */
			initEnvironment();
	
			/* 判断使用的数据库*/
			initDB();
			
			/* 初始化spring容器 */
			initContainer();
	
			//解析tablename文件
			try {
				TableNameFileForJar.parse();
			} catch (FlyingException e) {
				e.printStackTrace();
			}
		}else{
			log.debug("spring 环境已经初始化完毕！");
		}
	}
}

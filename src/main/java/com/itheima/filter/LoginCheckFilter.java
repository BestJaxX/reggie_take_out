package com.itheima.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.AntPathMatcher;

import com.alibaba.fastjson.JSON;
import com.itheima.common.BaseContext;
import com.itheima.common.R;

import lombok.extern.slf4j.Slf4j;


//检查用户是否已经完成登录

@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
	//路径匹配器，支持通配符。
	public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest=(HttpServletRequest)request;
		HttpServletResponse httpResponse=(HttpServletResponse)response;

		//1、获取本次请求的URI
		String requestURI = httpRequest.getRequestURI();
		
		log.info("拦截到请求{}",requestURI);
		
		//定义不需要处理的请求路径
		String[] urls=new String[] {
				"/employee/login",
				"/employee/logout",
				"/backend/**",
				"/front/**",
				"/common/**",
				"/user/sendMsg",//移动端发送短信
				"/user/login"//移动端登录
		};
		
		//2、判断本次请求是否需要处理
		boolean check = check(urls,requestURI);
		
		//3、如果不需要处理，则直接放行
		if(check) {
			log.info("本次请求{}不需要处理",requestURI);
			chain.doFilter(httpRequest, httpResponse);
			return;
		}
		
		//4-1、判断登录状态，如果已登录，则直接放行
		if(httpRequest.getSession().getAttribute("employee")!=null) {
			log.info("用户已登录，用户id为{}",httpRequest.getSession().getAttribute("employee"));
			
			Long empId = (Long) httpRequest.getSession().getAttribute("employee");
			BaseContext.setCurrentId(empId);
			
			long id =Thread.currentThread().getId();
			log.info("线程id为：{}",id);
			
			chain.doFilter(httpRequest, httpResponse);
			return;
		}
		
		//4-2、判断登录状态，如果已登录，则直接放行
				if(httpRequest.getSession().getAttribute("user")!=null) {
					log.info("用户已登录，用户id为{}",httpRequest.getSession().getAttribute("user"));
					
					Long userId = (Long) httpRequest.getSession().getAttribute("user");
					BaseContext.setCurrentId(userId);
					
					long id =Thread.currentThread().getId();
					log.info("线程id为：{}",id);
					
					chain.doFilter(httpRequest, httpResponse);
					return;
				}
		
		log.info("用户未登录");
		//5、如果未登录返回未登录结果，通过输出流方式向客户端页面响应数据。
		response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
	}
	//路径匹配，检查本次请求是否放行。
	public boolean check(String[] urls,String requestURI) {
		
		for(String url:urls) {
			boolean match = PATH_MATCHER.match(url, requestURI);
			if(match) {
				return true;
			}
		}
		
		return false;
		
	}
	
}

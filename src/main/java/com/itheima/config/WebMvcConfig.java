package com.itheima.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter;
import com.itheima.common.JacksonObjectMapper;
import com.itheima.common.converter.String2DateConverter;
import com.itheima.common.converter.String2LocalDateConverter;
import com.itheima.common.converter.String2LocalDateTimeConverter;
import com.itheima.common.converter.String2LocalTimeConverter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
	
	/*
	 * 设置静态资源映射
	 * @param registry
	 * */
	@Override
	protected void addResourceHandlers(ResourceHandlerRegistry registry) {
		log.info("开始进行静态资源映射");
		registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
		registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");

	}
	
	//扩展mvc框架的消息转换器
	@Override
	protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		log.info("扩展消息转换器。。。");
		//创建消息转换器对象
		MappingJackson2HttpMessageConverter messageConverter=new MappingJackson2HttpMessageConverter();
		//设置对象转换器，底层使用Jackson将Java对象转为json
		messageConverter.setObjectMapper(new JacksonObjectMapper());
		//将上面的消息转换器对象追加到mvc框架的转换器集合中,0表示索引，放在第一位。
		converters.add(0,messageConverter);
	}
	
	@Override
    protected void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new String2LocalDateConverter());
        registry.addConverter(new String2LocalDateTimeConverter());
        registry.addConverter(new String2LocalTimeConverter());
        registry.addConverter(new String2DateConverter());
    }

	
}

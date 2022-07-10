package com.itheima.controller;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.R;
import com.itheima.common.ValidateCodeUtils;
import com.itheima.entity.User;
import com.itheima.service.UserService;
import com.itheima.utils.SMSUtils;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
	@Autowired
	private UserService userService;
	
	//发送手机短信验证码
	@PostMapping("/sendMsg")
	public R<String> sendMsg(@RequestBody User user,HttpSession session){
		//获取手机号
		String phone= user.getPhone();
		String vildCode=null;
		if(StringUtils.isNotEmpty(phone)) {
			//生成随机4位验证码
			String code = ValidateCodeUtils.generateValidateCode(4).toString();
			log.info(code);
//			//调用阿里云提供的短信API完成发送短信
//			try {
//				com.aliyun.dysmsapi20170525.Client client=SMSUtils.createClient("LTAI5tE6FRGqLKLHmH5u9kPE", "V3ZTOgsnYk2RnU6zuhQVJm70uSQybp");
//				 SendSmsRequest sendSmsRequest = new SendSmsRequest()
//			                .setSignName("阿里云短信测试")
//			                .setTemplateCode("SMS_154950909")
//			                .setPhoneNumbers("18018592686")
//			                .setTemplateParam("{\"code\":\""+code+"\"}");
//			     // 复制代码运行请自行打印 API 的返回值
//			        SendSmsResponse response = client.sendSms(sendSmsRequest);
//			        vildCode=response.getBody().toString();
//			        System.out.println(vildCode);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			//需要将生成的验证码保存到Session
			session.setAttribute(phone, code);
			
			return R.success("手机验证码短信发送成功");
		}
		
		return R.error("短信发送失败");
	}
	
	//移动端用户登录
	@PostMapping("/login")
	public R<User> login(@RequestBody Map map ,HttpSession session){
		log.info(map.toString());
		
		//获取手机号
		String phone = map.get("phone").toString();
		//获取验证码
		String code = map.get("code").toString();
		//从Session中获取保存的验证码
		Object codeInSession = session.getAttribute(phone);
		//进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
		if(codeInSession!=null && codeInSession.equals(code)) {
			//如果能够比对成功，说明登录成功
			
			LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<User>();
			queryWrapper.eq(User::getPhone, phone);
			
			User user = userService.getOne(queryWrapper);
			if(user==null) {
				//判断当前手机号是否位新用户，如果是新用户，就自动完成注册
				user = new User();
				user.setPhone(phone);
				user.setStatus(1);
				userService.save(user);
			}
			session.setAttribute("user", user.getId());
			return R.success(user);  
		}
		
		
		return R.error("登录失败");
		
	}
	
}

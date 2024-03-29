package top.cicle.bigger.controller;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;


import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import top.cicle.bigger.common.biggerConstants;
import top.cicle.bigger.domain.Attachment;
import top.cicle.bigger.domain.User;
import top.cicle.bigger.service.biggerService;


@Controller
@RequestMapping(value="/user")
public class UserController {

	private static Logger logger = Logger.getLogger(UserController.class);
	@Autowired
	@Qualifier("biggerService")
	private biggerService biggerservice;

	//登陆页面
	@RequestMapping(value="login",method=RequestMethod.GET)
	public String loginForm()
	{
		System.out.println("function loginForm is active.");
		return "login";
	}
	
	//登陆校验-post
	@ResponseBody
	@RequestMapping(value="login",method=RequestMethod.POST,produces = "text/json;charset=UTF-8")
	public String login(@RequestParam("email") String email,@RequestParam("password") String password,Model model,HttpSession session) throws JSONException
	{
		logger.info("email"+email);
		User user=biggerservice.login(email, password);
		JSONObject json=new JSONObject();
		if(user!=null)
		{
			session.setAttribute(biggerConstants.USER_SESSION, user);
			session.setAttribute("username", user.getName());
			logger.info("log successed!");
			json.put("success", true);
			json.put("message", "登录成功！");
		}
		else
		{
			json.put("success", false);
			json.put("message", "用户名或者密码不正确！");
		}
		return json.toString();
	}
	
	//个人信息页面获取
	@RequestMapping(value="selinfo",method=RequestMethod.GET,produces = "text/json;charset=UTF-8")
	public String selfInfo()
	{
		return "user";
	}
	
	
	//个人信息获取-get
	@ResponseBody
	@RequestMapping(value="getselinfo",method=RequestMethod.GET,produces = "text/json;charset=UTF-8")
	public String selfInfo(HttpServletRequest request) throws JSONException
	{
		System.out.println("function get_selinfo is active.");
		User user=(User) request.getSession().getAttribute(biggerConstants.USER_SESSION);
		JSONObject json=new JSONObject();
		JSONArray list=new JSONArray();
		if(user!=null)
		{
			Date date=user.getInTime();
			SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD");
			String date1 =sdf.format(date);
			
		    JSONObject map=new JSONObject();
			map.put("username",user.getName());
			map.put("email",user.getEmail());
			map.put("tel",user.getTel());
			map.put("Date",user.getInTime());
			map.put("position",user.getPosition());
			map.put("profile",user.getProfile());
			map.put("pic_url",user.getPic_url());
			map.put("password",user.getPassword());
			map.put("in_time",date1);
			
			list.add(map);

			json.put("success", true);
			json.put("error","");
			json.put("data", list);
			logger.info(json);
			System.out.println(json);
		}
		else
		{
			json.put("success", false);
			json.put("error","");
			json.put("data", list);
		}
		return json.toString();
	}
	
	@ResponseBody
	@RequestMapping(value="saveselinfo",method=RequestMethod.POST,produces = "text/json;charset=UTF-8")
	public String saveselinfo(@ModelAttribute User user,HttpServletRequest request) throws JSONException
	{
		JSONObject json=new JSONObject();
		User user_now=(User) request.getSession().getAttribute(biggerConstants.USER_SESSION);
		Integer id=user_now.getId();
		user.setId(id);
		try{
			biggerservice.updateUser(user);
			json.put("success",true);
			json.put("message","更新成功！");
			
		}catch(Exception e)
		{
			e.printStackTrace();
			json.put("success",false);
			json.put("message","更新失败！");
		}
		return json.toString();
	}
	
	@RequestMapping(value="/logout")  
    public String logout(HttpSession session) throws Exception{  
        //清除Session  
        session.invalidate();  
          
        return "login";  
    }  
	
	//上传头像
	@ResponseBody
	@RequestMapping(value="addpic",method=RequestMethod.POST)
	public String addatt(@RequestParam("file") CommonsMultipartFile file,HttpSession session) throws IllegalStateException, IOException
	{
		
		JSONObject json=new JSONObject();
		//String path=session.getServletContext().getRealPath("/upload");
		String path="/usr/tomcat/apache-tomcat-7.0.82/webapps/bigger/pic";
		System.out.println(path);
		String fileName=file.getOriginalFilename();
		file.transferTo(new File(path+File.separator+fileName));
		Attachment attachment=new Attachment();
        attachment.setTitle(fileName);
        User user=(User) session.getAttribute(biggerConstants.USER_SESSION);
        
        //给User加头像链接；
        user.setPic_url("../../bigger/pic/"+fileName);
        biggerservice.updateUser(user);

        attachment.setUser(user);
        try
        {
        	(biggerservice).addAttachment(attachment);
        	json.put("success", true);
        	json.put("message", "附件添加成功！");
        }catch(Exception e)
        {
        	e.printStackTrace();
        	logger.info(e);
        	json.put("success", false);
        	json.put("message", "附件添加失败！");
        }
        return json.toString();
	}
	
}

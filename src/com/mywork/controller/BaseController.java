package com.mywork.controller;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.servlet.ModelAndView;

import com.mywork.bean.User;
import com.mywork.common.SessionKeys;

/**
 * 公共的控制类
 * @author 
 *
 */
public class BaseController {
	/**
	 * 处理以文本的形式做 Ajax 相应
	 * @param response
	 * @param text
	 * @throws IOException
	 */
	public void ajax(HttpServletResponse response,String text){
		try{
			PrintWriter out = response.getWriter();
			out.print(text);
			out.close();
		}catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	/**
	 * 获取session用户
	 * @param request
	 * @return
	 */
	public User getSessionUser(HttpServletRequest request){
		String role = request.getParameter("sessionRole");
		if(role == null || role.trim().length() == 0){
			role = request.getParameter("isadmin");
		}
		if((role == null || role.trim().length() == 0) && request.getAttribute("sessionRole") != null){
			role = request.getAttribute("sessionRole").toString();
		}
		if(role != null && role.trim().length() > 0){
			User roleUser = (User) request.getSession().getAttribute(SessionKeys.loginUserRoleKey(role.trim()));
			if(roleUser != null){
				return roleUser;
			}
		}
		return (User) request.getSession().getAttribute(SessionKeys.LOGIN_USER);
	}

	public boolean requireLogin(HttpServletRequest request, HttpServletResponse response){
		if(getSessionUser(request) != null){
			return true;
		}
		ajax(response, "登录已过期，请重新登录");
		return false;
	}

	public boolean requireRole(HttpServletRequest request, HttpServletResponse response, String role, String message){
		User user = getSessionUser(request);
		if(user == null){
			ajax(response, "登录已过期，请重新登录");
			return false;
		}
		if(role != null && role.equals(user.getIsadmin())){
			return true;
		}
		ajax(response, message == null ? "无权操作" : message);
		return false;
	}

	public boolean requireAdminRole(HttpServletRequest request, HttpServletResponse response){
		return requireRole(request, response, "0", "无权操作管理员功能");
	}

	public boolean requireTeacherRole(HttpServletRequest request, HttpServletResponse response){
		return requireRole(request, response, "1", "无权操作教师功能");
	}

	public boolean requireStudentRole(HttpServletRequest request, HttpServletResponse response){
		return requireRole(request, response, "2", "无权操作学生功能");
	}
	
	/**
	 * 页面跳转 --
	 * @param url 返回的页面的地址
	 * @param obj 要传递的页面信息,
	 * @param request 预定义，得到request的session,并将session的id放在map作为ModelAndView方法的一个参数
	 * @return
	 */
	public ModelAndView jsp(String url,Map<String,Object> map,HttpServletRequest request){
		HttpSession session=request.getSession();
		if(map == null){
			map = new HashMap<String,Object>();
		}
		User user = getSessionUser(request);
		map.put("sessionId", session.getId());
		map.put("sessionRole", user == null ? "" : user.getIsadmin());
		return new ModelAndView(url,map);
	}
}

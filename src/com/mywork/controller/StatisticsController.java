package com.mywork.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mywork.bean.User;
import com.mywork.service.StatisticsService;

@Controller
@RequestMapping(value="statistics")
public class StatisticsController extends BaseController{
	@Inject
	private StatisticsService statisticsService;

	@RequestMapping(value="grade-service-count")
	public void gradeServiceCount(HttpServletRequest request, HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		User user = getSessionUser(request);
		if(user == null){
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			ajax(response, jsonResponse(401, null, "登录已过期，请重新登录").toString());
			return;
		}
		if(!"0".equals(user.getIsadmin())){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			ajax(response, jsonResponse(403, null, "无权访问该统计数据").toString());
			return;
		}
		JSONObject data = new JSONObject();
		data.put("count", statisticsService.getGradeServiceCount());
		ajax(response, jsonResponse(200, data, "success").toString());
	}

	private JSONObject jsonResponse(int code, JSONObject data, String message){
		JSONObject result = new JSONObject();
		result.put("code", code);
		result.put("data", data == null ? new JSONObject() : data);
		result.put("message", message);
		return result;
	}
}

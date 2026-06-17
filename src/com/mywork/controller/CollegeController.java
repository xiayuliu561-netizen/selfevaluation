package com.mywork.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.mywork.bean.College;
import com.mywork.service.CollegeService;
import com.mywork.util.CommonUtil;
/**
 * 学院
 * @author 
 *
 */
@Controller
@RequestMapping(value="college")
public class CollegeController extends BaseController{
	@Inject
	private CollegeService collegeService;
	
	
	/**
	 * 学院列表
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="list")
	public ModelAndView list(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		if(getSessionUser(request) == null || !"0".equals(getSessionUser(request).getIsadmin())){
			return jsp("login", map, request);
		}
		return jsp("college", map, request);
	}
	/**
	 * 
	 * get list data
	 * @param request
	 * @return
	 */
	@RequestMapping(value="listdata")
	public void listdata(HttpServletRequest request, HttpServletResponse response){
		if(!requireAdminRole(request, response)){
			return;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("collegeName", CommonUtil.changeEncoding(request.getParameter("queryname")));
		List<College> list = collegeService.getList(map);
		JSONArray jsonarray = JSONArray.fromObject(list);
		String json = "{\"data\":"+jsonarray.toString()+"}";
		ajax(response, json);
	}
	/**
	 * 添加
	 * @param request
	 * @return
	 */
	@RequestMapping(value="add")
	public void add(HttpServletRequest request, HttpServletResponse response, College college){
		if(!requireAdminRole(request, response)){
			return;
		}
		ajax(response, "学院信息可在课程学生名单导入时自动补全，请先导入学生名单");
	}
	/**
	 * 修改
	 * @param request
	 * @return
	 */
	@RequestMapping(value="update")
	public void update(HttpServletRequest request, HttpServletResponse response, College college){
		if(!requireAdminRole(request, response)){
			return;
		}
		collegeService.update(college);
		ajax(response, "修改成功");
	}
	
	/**
	 * delete
	 * @param request
	 * @return
	 */
	@RequestMapping(value="del")
	public void del(HttpServletRequest request, HttpServletResponse response){
		if(!requireAdminRole(request, response)){
			return;
		}
		String id = request.getParameter("id");
		collegeService.delete(id);
		ajax(response, "删除成功");
		
	}

	@RequestMapping(value="dels")
	public void dels(HttpServletRequest request, HttpServletResponse response){
		if(!requireAdminRole(request, response)){
			return;
		}
		String ids = request.getParameter("ids");
		if(ids == null || "".equals(ids.trim())){
			ajax(response, "请选择要删除的学院");
			return;
		}
		int count = 0;
		String[] idArray = ids.split(",");
		for(String id : idArray){
			if(id != null && !"".equals(id.trim())){
				collegeService.delete(id.trim());
				count++;
			}
		}
		ajax(response, count > 0 ? "删除成功" : "请选择要删除的学院");
	}

}

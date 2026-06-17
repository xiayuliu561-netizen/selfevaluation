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

import com.mywork.bean.Rate;
import com.mywork.service.LessonService;
import com.mywork.service.RateService;
/**
 * 比例
 * @author 
 *
 */
@Controller
@RequestMapping(value="rate")
public class RateController extends BaseController{
	@Inject
	private RateService rateService;
	@Inject
	private LessonService lessonService;
	
	
	/**
	 * 列表
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="list")
	public ModelAndView list(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("teacherid", getSessionUser(request).getId());
		map.put("lessonlist", lessonService.getList(map));
		return jsp("rate", map, request);
	}
	/**
	 * 
	 * get list data
	 * @param request
	 * @return
	 */
	@RequestMapping(value="listdata")
	public void listdata(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("lessonid", request.getParameter("queryname"));
		map.put("teacherid", getSessionUser(request).getId());
		List<Rate> list = rateService.getList(map);
		JSONArray jsonarray = JSONArray.fromObject(list);
		String json = "{\"data\":"+jsonarray.toString()+"}";
		ajax(response, json);
	}
	/**
	 * form
	 * @param request
	 * @return
	 */
	@RequestMapping(value="form")
	public ModelAndView form(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("rate", rateService.getByTeacherId(getSessionUser(request).getId()+""));
		return jsp("rate", map, request);
	}
	/**
	 * 添加
	 * @param request
	 * @return
	 */
	@RequestMapping(value="add")
	public void add(HttpServletRequest request, HttpServletResponse response, Rate rate){
		rate.setTeacherid(getSessionUser(request).getId());
		rateService.insert(rate);
		ajax(response, "新增成功");
	}
	/**
	 * 修改
	 * @param request
	 * @return
	 */
	@RequestMapping(value="update")
	public void update(HttpServletRequest request, HttpServletResponse response, Rate rate){
		rate.setTeacherid(getSessionUser(request).getId());
		rateService.update(rate);
		ajax(response, "修改成功");
	}
	
	/**
	 * delete
	 * @param request
	 * @return
	 */
	@RequestMapping(value="del")
	public void del(HttpServletRequest request, HttpServletResponse response){
		String id = request.getParameter("id");
		rateService.delete(id);
		ajax(response, "删除成功");
		
	}

}

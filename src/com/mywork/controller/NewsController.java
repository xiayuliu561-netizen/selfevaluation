package com.mywork.controller;


import java.util.Date;
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

import com.mywork.bean.News;
import com.mywork.service.NewsService;
import com.mywork.util.CommonUtil;
import com.mywork.util.DateUtil;
/**
 * 
 * @author 
 *
 */
@Controller
@RequestMapping(value="news")
public class NewsController extends BaseController{
	@Inject
	private NewsService newsService;
	
	/**
	 * 列表
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="checklist")
	public ModelAndView checklist(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		return jsp("newscheck", map, request);
		
	}
	/**
	 * 列表
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="list")
	public ModelAndView list(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		return jsp("news", map, request);
		
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
		map.put("title", CommonUtil.changeEncoding(request.getParameter("queryname")));
		List<News> list = newsService.getList(map);
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
	public void add(HttpServletRequest request, HttpServletResponse response, News news){
		news.setType(getSessionUser(request).getDeptId());
		news.setTime(DateUtil.formatHMS(new Date()));
		newsService.insert(news);
		ajax(response, "新增成功");
	}
	/**
	 * 修改
	 * @param request
	 * @return
	 */
	@RequestMapping(value="update")
	public void update(HttpServletRequest request, HttpServletResponse response, News news){
		news.setType(getSessionUser(request).getDeptId());
		news.setTime(DateUtil.formatHMS(new Date()));
		newsService.update(news);
		ajax(response, "修改成功");
	}
	
	/**
	 * delete
	 * @param request
	 * @return
	 */
	@RequestMapping(value="del")
	public void del(HttpServletRequest request,HttpServletResponse response){
		String id = request.getParameter("id");
		newsService.delete(id);
		ajax(response, "删除成功");
	}
}

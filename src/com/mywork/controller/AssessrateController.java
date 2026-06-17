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

import com.mywork.bean.Assessrate;
import com.mywork.service.AssessrateService;
import com.mywork.service.LessonService;
import com.mywork.util.CommonUtil;
/**
 * 
 * @author 
 *
 */
@Controller
@RequestMapping(value="assessrate")
public class AssessrateController extends BaseController{
	@Inject
	private AssessrateService assessrateService;
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
		return jsp("assessrate", map, request);
		
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
		map.put("targetname", CommonUtil.changeEncoding(request.getParameter("queryname")));
		map.put("userid", getSessionUser(request).getId());
		List<Assessrate> list = assessrateService.getList(map);
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
	public void add(HttpServletRequest request, HttpServletResponse response, Assessrate assessrate){
		if(assessrate.getRate1() == null)assessrate.setRate1(0);
		if(assessrate.getRate2() == null)assessrate.setRate2(0);
		if(assessrate.getRate3() == null)assessrate.setRate3(0);
		if(assessrate.getRate4() == null)assessrate.setRate4(0);
		if(assessrate.getRate5() == null)assessrate.setRate5(0);
		if(assessrate.getRate6() == null)assessrate.setRate6(0);
		assessrate.setUserid(getSessionUser(request).getId());
		assessrateService.insert(assessrate);
		ajax(response, "新增成功");
	}
	/**
	 * 修改
	 * @param request
	 * @return
	 */
	@RequestMapping(value="update")
	public void update(HttpServletRequest request, HttpServletResponse response, Assessrate assessrate){
		if(assessrate.getRate1() == null)assessrate.setRate1(0);
		if(assessrate.getRate2() == null)assessrate.setRate2(0);
		if(assessrate.getRate3() == null)assessrate.setRate3(0);
		if(assessrate.getRate4() == null)assessrate.setRate4(0);
		if(assessrate.getRate5() == null)assessrate.setRate5(0);
		if(assessrate.getRate6() == null)assessrate.setRate6(0);
		assessrate.setUserid(getSessionUser(request).getId());
		assessrateService.update(assessrate);
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
		assessrateService.delete(id);
		ajax(response, "删除成功");
	}
}

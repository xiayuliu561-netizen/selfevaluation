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

import com.mywork.bean.AiModel;
import com.mywork.service.AiModelService;
import com.mywork.util.CommonUtil;
import com.mywork.util.DateUtil;

@Controller
@RequestMapping(value="aimodel")
public class AiModelController extends BaseController{
	@Inject
	private AiModelService aiModelService;

	@RequestMapping(value="list")
	public ModelAndView list(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		return jsp("aimodel", map, request);
	}

	@RequestMapping(value="listdata")
	public void listdata(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("queryname", CommonUtil.changeEncoding(request.getParameter("queryname")));
		List<AiModel> list = aiModelService.getList(map);
		for(AiModel model : list){
			model.setApiKey("");
		}
		JSONArray jsonarray = JSONArray.fromObject(list);
		String json = "{\"data\":"+jsonarray.toString()+"}";
		ajax(response, json);
	}

	@RequestMapping(value="add")
	public void add(HttpServletRequest request, HttpServletResponse response, AiModel aiModel){
		aiModel.setEnabled("0");
		aiModel.setIsDefault("0");
		normalizeSingletonState(aiModel);
		aiModel.setCreatetime(DateUtil.formatHMS(new Date()));
		aiModelService.insert(aiModel);
		ajax(response, "新增成功");
	}

	@RequestMapping(value="update")
	public void update(HttpServletRequest request, HttpServletResponse response, AiModel aiModel){
		AiModel oldModel = aiModelService.getById(aiModel.getId().toString());
		if(oldModel != null){
			aiModel.setEnabled(oldModel.getEnabled());
			aiModel.setIsDefault(oldModel.getIsDefault());
			if(aiModel.getApiKey() == null || "".equals(aiModel.getApiKey().trim())){
				aiModel.setApiKey(oldModel.getApiKey());
			}
		}
		aiModelService.update(aiModel);
		ajax(response, "修改成功");
	}

	@RequestMapping(value="updatestatus")
	public void updatestatus(HttpServletRequest request, HttpServletResponse response){
		String id = request.getParameter("id");
		String field = request.getParameter("field");
		String value = request.getParameter("value");
		if(id == null || "".equals(id) || field == null || value == null){
			ajax(response, "参数错误");
			return;
		}
		if(!"enabled".equals(field) && !"isDefault".equals(field)){
			ajax(response, "参数错误");
			return;
		}
		value = "1".equals(value) ? "1" : "0";
		AiModel aiModel = aiModelService.getById(id);
		if(aiModel == null){
			ajax(response, "模型不存在");
			return;
		}
		if("enabled".equals(field)){
			aiModel.setEnabled(value);
			if("0".equals(value)){
				aiModel.setIsDefault("0");
			}
		}else{
			aiModel.setIsDefault(value);
			if("1".equals(value)){
				aiModel.setEnabled("1");
			}
		}
		normalizeSingletonState(aiModel);
		aiModelService.update(aiModel);
		ajax(response, "修改成功");
	}

	@RequestMapping(value="del")
	public void del(HttpServletRequest request, HttpServletResponse response){
		String id = request.getParameter("id");
		aiModelService.delete(id);
		ajax(response, "删除成功");
	}

	@RequestMapping(value="testcall")
	public void testcall(HttpServletRequest request, HttpServletResponse response){
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain;charset=UTF-8");
		String id = request.getParameter("id");
		try{
			ajax(response, aiModelService.testCall(id));
		}catch(Exception e){
			ajax(response, "调用失败：" + e.getMessage());
		}
	}

	private void normalizeSingletonState(AiModel aiModel){
		if("1".equals(aiModel.getIsDefault())){
			aiModel.setEnabled("1");
			aiModelService.clearDefault(aiModel.getId());
			aiModelService.clearEnabled(aiModel.getId());
			return;
		}
		if("1".equals(aiModel.getEnabled())){
			aiModelService.clearEnabled(aiModel.getId());
			aiModelService.clearDefault(aiModel.getId());
		}
	}
}

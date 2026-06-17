package com.mywork.controller;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.mywork.bean.AnalysisComponent;
import com.mywork.bean.AnalysisTarget;
import com.mywork.bean.AnalysisTargetItem;
import com.mywork.bean.Assessrate;
import com.mywork.bean.Lesson;
import com.mywork.bean.Rate;
import com.mywork.service.AiModelService;
import com.mywork.service.AnalysisComponentService;
import com.mywork.service.AnalysisTargetItemService;
import com.mywork.service.AnalysisTargetService;
import com.mywork.service.AssessrateService;
import com.mywork.service.LessonService;
import com.mywork.service.RateService;
import com.mywork.util.AnalysisJsonUtil;
import com.mywork.util.ImageUtil;
import com.mywork.util.SyllabusAnalysisParser;
import com.mywork.util.SyllabusTextExtractor;
import com.mywork.util.SysModel;

@Controller
@RequestMapping(value="fusion")
public class FusionController extends BaseController{
	@Inject
	private LessonService lessonService;
	@Inject
	private AiModelService aiModelService;
	@Inject
	private AnalysisComponentService analysisComponentService;
	@Inject
	private AnalysisTargetService analysisTargetService;
	@Inject
	private AnalysisTargetItemService analysisTargetItemService;
	@Inject
	private RateService rateService;
	@Inject
	private AssessrateService assessrateService;

	@RequestMapping(value="list")
	public ModelAndView list(HttpServletRequest request, HttpServletResponse response){
		if(!requireLogin(request, response)){
			return null;
		}
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("userid", getSessionUser(request).getId());
		map.put("lessonlist", lessonService.getList(query));
		return jsp("fusion", map, request);
	}

	@RequestMapping(value="get")
	public void get(HttpServletRequest request, HttpServletResponse response){
		if(!requireLogin(request, response)){
			return;
		}
		JSONObject result = getSavedAnalysis(request.getParameter("lessonid"), getSessionUser(request).getId());
		ajax(response, result.toString());
	}

	@RequestMapping(value="parse")
	public void parse(HttpServletRequest request, HttpServletResponse response){
		if(!requireLogin(request, response)){
			return;
		}
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json;charset=UTF-8");
		JSONObject localData = null;
		try{
			String lessonid = request.getParameter("lessonid");
			if(!isNotBlank(lessonid)){
				ajax(response, error("请选择课程"));
				return;
			}
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multipartRequest.getFile("file");
			if(file == null || file.getOriginalFilename() == null || "".equals(file.getOriginalFilename())){
				ajax(response, error("请选择教学大纲文件"));
				return;
			}
			String fileName = file.getOriginalFilename();
			String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
			if(!"doc".equals(suffix) && !"docx".equals(suffix) && !"pdf".equals(suffix) && !"txt".equals(suffix)){
				ajax(response, error("仅支持 doc、docx、pdf、txt 文件"));
				return;
			}
			String uploadPath = SysModel.get("uploadRoot").toString();
			String filePath = new SimpleDateFormat("yyyyMMddHHmmssSS").format(Calendar.getInstance().getTime()) + "." + suffix;
			String fileChangePath = uploadPath + filePath;
			ImageUtil.uploadfile(file, fileChangePath, filePath);
			File uploadFile = new File(fileChangePath);
			String text = SyllabusTextExtractor.extract(uploadFile, fileName);
			if(!isNotBlank(text)){
				ajax(response, error("未能从文件中提取到文本内容"));
				return;
			}
			localData = SyllabusAnalysisParser.parse(text);
			if(hasComponents(localData) && hasTargets(localData)){
				ajax(response, success(localData, "系统已从教学大纲文本中完成结构化分析，请核对下方数据是否与教学大纲一致。", "本地规则解析"));
				return;
			}
			String aiContent = aiModelService.callPrompt(buildPrompt(buildAnalysisText(text)));
			JSONObject data;
			try{
				data = AnalysisJsonUtil.normalizeJson(aiContent);
				}catch(Exception e){
					if(hasComponents(localData)){
						ajax(response, success(localData, "系统已根据教学大纲文本完成初步解析，请核对后再保存。", aiContent));
						return;
					}
					ajax(response, error("AI 分析结果暂时无法整理成可保存的数据，请稍后重试；如多次失败，请联系管理员。"));
					return;
				}
			if(data.getJSONArray("components").size() == 0){
				if(hasComponents(localData)){
					ajax(response, success(localData, "AI 未解析到总评成绩组成，系统已根据教学大纲文本完成初步解析，请核对后再保存。", aiContent));
					return;
				}
				ajax(response, error("AI 未解析到总评成绩组成，请检查教学大纲内容"));
				return;
			}
			if(data.getJSONArray("targets").size() == 0 && hasTargets(localData)){
				data.put("targets", localData.getJSONArray("targets"));
			}
			mergeTargetContent(data, localData);
			ajax(response, success(data, "AI 分析已完成，请核对下方数据是否与教学大纲一致。", aiContent));
		}catch(Exception e){
			if(hasComponents(localData)){
				ajax(response, success(localData, "AI 分析暂时未完成，系统已根据教学大纲文本完成初步解析，请核对后再保存。", "系统规则解析"));
				return;
			}
			ajax(response, error("AI 分析暂时未完成，请稍后重试；如多次失败，请联系管理员。"));
		}
	}

	@RequestMapping(value="save")
	public void save(HttpServletRequest request, HttpServletResponse response){
		if(!requireLogin(request, response)){
			return;
		}
		try{
			String lessonid = request.getParameter("lessonid");
			String analysisJson = request.getParameter("analysisJson");
			if(!isNotBlank(lessonid)){
				ajax(response, "请选择课程");
				return;
			}
			if(!isNotBlank(analysisJson)){
				ajax(response, "请先上传教学大纲并完成 AI 分析");
				return;
			}
			Lesson lesson = lessonService.getById(lessonid);
			if(lesson == null || !getSessionUser(request).getId().equals(lesson.getUserid())){
				ajax(response, "课程不存在或无权操作");
				return;
			}
			JSONObject json = JSONObject.fromObject(analysisJson);
			List<AnalysisComponent> components = AnalysisJsonUtil.parseComponents(json);
			List<AnalysisTarget> targets = AnalysisJsonUtil.parseTargets(json);
			if(components.size() == 0){
				ajax(response, "总评成绩组成不能为空");
				return;
			}
			Map<String,Object> query = new HashMap<String,Object>();
			query.put("lessonid", lessonid);
			query.put("teacherid", getSessionUser(request).getId());
			analysisTargetItemService.deleteByLesson(query);
			analysisTargetService.deleteByLesson(query);
			analysisComponentService.deleteByLesson(query);
			rateService.deleteByLesson(query);
			Map<String,Object> assessQuery = new HashMap<String,Object>();
			assessQuery.put("lessonid", lessonid);
			assessQuery.put("userid", getSessionUser(request).getId());
			assessrateService.deleteByLesson(assessQuery);

			for(int i=0; i<components.size(); i++){
				AnalysisComponent component = components.get(i);
				component.setLessonid(Integer.valueOf(lessonid));
				component.setTeacherid(getSessionUser(request).getId());
				component.setSortno(i+1);
				analysisComponentService.insert(component);
			}
			for(int i=0; i<targets.size(); i++){
				AnalysisTarget target = targets.get(i);
				target.setLessonid(Integer.valueOf(lessonid));
				target.setTeacherid(getSessionUser(request).getId());
				target.setSortno(i+1);
				if(target.getTargetrate() == null){
					target.setTargetrate(new BigDecimal("0"));
				}
				analysisTargetService.insert(target);
				if(target.getItemlist() != null){
					for(int j=0; j<target.getItemlist().size(); j++){
						AnalysisTargetItem item = target.getItemlist().get(j);
						item.setTargetid(target.getId());
						item.setLessonid(Integer.valueOf(lessonid));
						item.setTeacherid(getSessionUser(request).getId());
						item.setSortno(j+1);
						analysisTargetItemService.insert(item);
					}
				}
			}
			saveLegacyData(lessonid, components, targets, getSessionUser(request).getId());
			ajax(response, "保存成功");
		}catch(Exception e){
			ajax(response, "保存失败：" + e.getMessage());
		}
	}

	private JSONObject getSavedAnalysis(String lessonid, Integer teacherid){
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("lessonid", lessonid);
		query.put("teacherid", teacherid);
		List<AnalysisComponent> components = analysisComponentService.getList(query);
		List<AnalysisTarget> targets = analysisTargetService.getList(query);
		for(AnalysisTarget target : targets){
			Map<String,Object> itemQuery = new HashMap<String,Object>();
			itemQuery.put("targetid", target.getId());
			target.setItemlist(analysisTargetItemService.getList(itemQuery));
		}
		JSONObject result = new JSONObject();
		result.put("success", true);
		result.put("data", AnalysisJsonUtil.toJson(components, targets));
		return result;
	}

	private void saveLegacyData(String lessonid, List<AnalysisComponent> components, List<AnalysisTarget> targets, Integer teacherid){
		Rate rate = new Rate();
		rate.setTeacherid(teacherid);
		rate.setLessonid(Integer.valueOf(lessonid));
		rate.setShowrate(getRate(components, 0));
		rate.setHomeworkrate(getRate(components, 1));
		rate.setTestrate(getRate(components, 2));
		rate.setDesignrate(getRate(components, 3));
		rate.setMiddlerate(getRate(components, 4));
		rate.setEndrate(getRate(components, 5));
		rateService.insert(rate);
		for(AnalysisTarget target : targets){
			Assessrate assessrate = new Assessrate();
			assessrate.setUserid(teacherid);
			assessrate.setLessonid(Integer.valueOf(lessonid));
			assessrate.setTargetname(target.getTargetName());
			assessrate.setTargetrate(target.getTargetrate());
			assessrate.setRate1(getCoeff(target, components, 0));
			assessrate.setRate2(getCoeff(target, components, 1));
			assessrate.setRate3(getCoeff(target, components, 2));
			assessrate.setRate4(getCoeff(target, components, 3));
			assessrate.setRate5(getCoeff(target, components, 4));
			assessrate.setRate6(getCoeff(target, components, 5));
			assessrateService.insert(assessrate);
		}
	}

	private Integer getRate(List<AnalysisComponent> components, int index){
		if(index >= components.size() || components.get(index).getRate() == null){
			return 0;
		}
		return Integer.valueOf(components.get(index).getRate().setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
	}

	private Integer getCoeff(AnalysisTarget target, List<AnalysisComponent> components, int index){
		if(index >= components.size() || target.getItemlist() == null){
			return 0;
		}
		String name = components.get(index).getComponentName();
		for(AnalysisTargetItem item : target.getItemlist()){
			if(item.getMethodName() != null && item.getMethodName().equals(name)){
				return item.getCoefficient() == null ? 0 : item.getCoefficient().setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
			}
		}
		if(index < target.getItemlist().size()){
			AnalysisTargetItem item = target.getItemlist().get(index);
			return item.getCoefficient() == null ? 0 : item.getCoefficient().setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
		}
		return 0;
	}

	private String buildPrompt(String text){
		return "请从以下教学大纲中提取成绩组成、总评比例、课程目标原文、课程目标考核比例和系数。"
			+ "只返回 JSON，不要返回 Markdown 或解释文字。JSON 格式必须为："
			+ "{\"components\":[{\"name\":\"平时表现\",\"rate\":10}],"
			+ "\"targets\":[{\"name\":\"课程目标1\",\"content\":\"从教学大纲课程目标章节原文复制的完整目标正文\",\"targetrate\":0,"
			+ "\"items\":[{\"method\":\"平时表现\",\"weight\":10,\"coefficient\":40}]}]}。"
			+ "content 必须原样摘录教学大纲里的课程目标正文，不要总结、改写或自行生成。"
			+ "rate、weight、coefficient、targetrate 均使用百分数数值，10% 返回 10。教学大纲如下：\n" + text;
	}

	private String buildAnalysisText(String text){
		if(text == null){
			return "";
		}
		String[] lines = text.split("\\r?\\n");
		StringBuilder builder = new StringBuilder();
		int keep = 0;
		for(int i=0; i<lines.length; i++){
			String line = lines[i];
			if(isAnalysisLine(line)){
				keep = line.indexOf("课程目标") >= 0 && line.indexOf("考核") < 0 ? 30 : 8;
			}
			if(keep > 0){
				builder.append(line).append('\n');
				keep--;
			}
		}
		String result = builder.toString().trim();
		if(result.length() < 300){
			result = text;
		}
		if(result.length() > 9000){
			result = result.substring(0, 9000);
		}
		return result;
	}

	private boolean isAnalysisLine(String line){
		if(line == null){
			return false;
		}
		return line.indexOf("总评") >= 0 || line.indexOf("总成绩") >= 0 || line.indexOf("课程评价") >= 0
			|| line.indexOf("考核内容、考核方式") >= 0 || line.indexOf("各考核方式占总成绩权重") >= 0
			|| line.indexOf("课程目标在各考核方式中占比") >= 0 || line.indexOf("考核方式评分标准") >= 0
			|| line.indexOf("课程目标") >= 0 || line.indexOf("考核方式") >= 0 || line.indexOf("占比") >= 0 || line.indexOf("系数") >= 0;
	}

	private void mergeTargetContent(JSONObject data, JSONObject localData){
		if(data == null || localData == null || !data.containsKey("targets") || !localData.containsKey("targets")){
			return;
		}
		for(int i=0; i<data.getJSONArray("targets").size(); i++){
			JSONObject target = data.getJSONArray("targets").getJSONObject(i);
			if(target.containsKey("content") && isNotBlank(target.getString("content"))){
				continue;
			}
			if(i < localData.getJSONArray("targets").size()){
				JSONObject localTarget = localData.getJSONArray("targets").getJSONObject(i);
				if(localTarget.containsKey("content") && isNotBlank(localTarget.getString("content"))){
					target.put("content", localTarget.getString("content"));
				}
			}
		}
	}

	private boolean hasComponents(JSONObject data){
		return data != null && data.containsKey("components") && data.getJSONArray("components").size() > 0;
	}

	private boolean hasTargets(JSONObject data){
		return data != null && data.containsKey("targets") && data.getJSONArray("targets").size() > 0;
	}

	private String success(JSONObject data, String message, String raw){
		JSONObject result = new JSONObject();
		result.put("success", true);
		result.put("message", message);
		result.put("data", data);
		return result.toString();
	}

	private String error(String message){
		JSONObject result = new JSONObject();
		result.put("success", false);
		result.put("message", message);
		return result.toString();
	}

	private boolean isNotBlank(String value){
		return value != null && value.trim().length() > 0;
	}
}

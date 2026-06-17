package com.mywork.controller;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.mywork.bean.LessonStudent;
import com.mywork.bean.Question;
import com.mywork.bean.Questionscore;
import com.mywork.bean.Score;
import com.mywork.service.LessonService;
import com.mywork.service.LessonStudentService;
import com.mywork.service.QuestionService;
import com.mywork.service.QuestionscoreService;
import com.mywork.service.ScoreService;
import com.mywork.service.UserService;
import com.mywork.util.CommonUtil;
/**
 * 
 * @author 
 *
 */
@Controller
@RequestMapping(value="question")
public class QuestionController extends BaseController{
	@Inject
	private QuestionService questionService;
	@Inject
	private QuestionscoreService questionscoreService;
	@Inject
	private UserService userService;
	@Inject
	private LessonService lessonService;
	@Inject
	private LessonStudentService lessonStudentService;
	@Inject
	private ScoreService scoreService;

	@RequestMapping(value="questionanswer")
	public ModelAndView answerlist(HttpServletRequest request){
		return showQuestionAnswer(request, null);
	}

	@RequestMapping(value="answer")
	public ModelAndView answer(HttpServletRequest request){
		String teacherid = request.getParameter("teacherid");
		String lessonid = request.getParameter("lessonid");
		String surveyIdText = request.getParameter("surveyId");
		Integer surveyId = parseInteger(surveyIdText);
		if(isBlank(teacherid) || isBlank(lessonid)){
			return showQuestionAnswer(request, "请选择课程后再提交问卷");
		}

		List<Question> questionlist = getQuestionsForSurvey(teacherid, lessonid, surveyId);
		if(questionlist.size() == 0){
			return showQuestionAnswer(request, "当前问卷暂无问题，无法提交");
		}

		Map<String,Object> scoreQuery = new HashMap<String,Object>();
		scoreQuery.put("userid", getSessionUser(request).getId());
		scoreQuery.put("teacherid", teacherid);
		scoreQuery.put("lesson", lessonid);
		if(surveyId == null){
			scoreQuery.put("surveyIdNull", "1");
		}else{
			scoreQuery.put("surveyId", surveyId);
		}
		List<Questionscore> haslist = questionscoreService.getList(scoreQuery);
		if(haslist.size() > 0){
			return showQuestionAnswer(request, "不可重复提交");
		}

		for(Question question : questionlist){
			String answerValue = request.getParameter(question.getId()+"");
			String score1 = getAnswerScore(answerValue);
			Integer optionIndex = getAnswerOptionIndex(answerValue);
			if(isBlank(score1)){
				score1 = "0";
			}
			Questionscore questionscore = new Questionscore();
			questionscore.setUserid(getSessionUser(request).getId());
			questionscore.setLesson(question.getSecondtype());
			questionscore.setTeacherid(Integer.parseInt(question.getType()));
			questionscore.setSurveyId(surveyId);
			questionscore.setQuestionid(question.getId());
			setAnswerOption(questionscore, optionIndex, score1);
			questionscore.setAvgscore(score1);
			questionscoreService.insert(questionscore);
		}
		return showQuestionAnswer(request, "提交成功");
	}

	@RequestMapping(value="questionanswerscorelist")
	public ModelAndView questionanswerscorelist(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", getSessionUser(request).getId());
		map.put("lessonlist", lessonService.getList(map));
		return jsp("questionanswerscore", map, request);
		
	}

	@RequestMapping(value="questionanswerstatsdata")
	public void questionanswerstatsdata(HttpServletRequest request, HttpServletResponse response){
		String lessonid = request.getParameter("lessonid");
		Map<String,Object> questionQuery = new HashMap<String,Object>();
		questionQuery.put("type", getSessionUser(request).getId()+"");
		questionQuery.put("lessonid", lessonid);
		List<Question> questions = questionService.getList(questionQuery);
		List<Map<String,Object>> surveylist = buildSurveyList(questions);

		Map<String,Object> scoreQuery = new HashMap<String,Object>();
		scoreQuery.put("teacherid", getSessionUser(request).getId());
		scoreQuery.put("lesson", lessonid);
		List<Questionscore> scores = questionscoreService.getList(scoreQuery);
		Map<Integer,List<Questionscore>> scoreMap = groupScoresByQuestion(scores);
		for(Map<String,Object> survey : surveylist){
			enrichSurveyStats(survey, scoreMap);
		}
		JSONArray jsonarray = JSONArray.fromObject(surveylist);
		String json = "{\"data\":"+jsonarray.toString()+"}";
		ajax(response, json);
	}

	@RequestMapping(value="questionanswerscorelistdata")
	public void questionanswerscorelistdata(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", request.getParameter("queryname"));
		map.put("lesson", request.getParameter("lessonid"));
		map.put("teacherid", getSessionUser(request).getId());
		List<Questionscore> list = questionscoreService.getAvgList(map);
		Map<String,String> surveyNameMap = getSurveyNameMap(getSessionUser(request).getId()+"", request.getParameter("lessonid"));
		for(Questionscore score : list){
			score.setUser(userService.getUserById(score.getUserid()+""));
			score.setSurveyName(resolveSurveyName(surveyNameMap, score.getLesson(), score.getSurveyId()));
		}
		JSONArray jsonarray = JSONArray.fromObject(list);
		String json = "{\"data\":"+jsonarray.toString()+"}";
		ajax(response, json);
	}
	
	@RequestMapping(value="list")
	public ModelAndView list(HttpServletRequest request){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userid", getSessionUser(request).getId());
		map.put("lessonlist", lessonService.getList(map));
		return jsp("question", map, request);
		
	}

	@RequestMapping(value="surveylistdata")
	public void surveylistdata(HttpServletRequest request, HttpServletResponse response){
		String keyword = CommonUtil.changeEncoding(request.getParameter("queryname"));
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("type", getSessionUser(request).getId());
		map.put("lessonid", request.getParameter("lessonid"));
		List<Question> list = questionService.getList(map);
		List<Map<String,Object>> surveylist = buildSurveyList(list);
		List<Map<String,Object>> filtered = filterSurveyList(surveylist, keyword);
		JSONArray jsonarray = JSONArray.fromObject(filtered);
		String json = "{\"data\":"+jsonarray.toString()+"}";
		ajax(response, json);
	}

	@RequestMapping(value="savesurvey")
	public void savesurvey(HttpServletRequest request, HttpServletResponse response){
		try{
			String lessonid = request.getParameter("lessonid");
			String surveyName = CommonUtil.changeEncoding(request.getParameter("surveyName"));
			String surveyDesc = CommonUtil.changeEncoding(request.getParameter("surveyDesc"));
			String surveyKey = request.getParameter("surveyKey");
			Integer surveyId = parseInteger(request.getParameter("surveyId"));
			String surveyJson = request.getParameter("surveyJson");
			if(isBlank(lessonid)){
				ajax(response, "请选择课程");
				return;
			}
			if(isBlank(surveyName)){
				ajax(response, "请输入问卷名称");
				return;
			}
			if(isBlank(surveyJson)){
				ajax(response, "请至少添加一个问题");
				return;
			}
			JSONObject json = JSONObject.fromObject(surveyJson);
			JSONArray questions = json.getJSONArray("questions");
			if(questions == null || questions.size() == 0){
				ajax(response, "请至少添加一个问题");
				return;
			}
			if(surveyId == null){
				surveyId = createSurveyId(getSessionUser(request).getId()+"");
			}
			deleteSurveyQuestions(getSessionUser(request).getId()+"", lessonid, surveyKey, surveyId);
			for(int i=0; i<questions.size(); i++){
				JSONObject questionJson = questions.getJSONObject(i);
				String title = trim(questionJson.getString("title"));
				JSONArray options = questionJson.getJSONArray("options");
				if(isBlank(title)){
					ajax(response, "第" + (i + 1) + "个问题未填写题目");
					return;
				}
				if(options == null || options.size() < 2){
					ajax(response, "第" + (i + 1) + "个问题至少需要两个选项");
					return;
				}
				Question question = new Question();
				question.setType(getSessionUser(request).getId()+"");
				question.setSecondtype(lessonid);
				question.setLessonid(lessonid);
				question.setSurveyId(surveyId);
				question.setSurveyName(surveyName);
				question.setSurveyDesc(surveyDesc);
				question.setSortno(i + 1);
				question.setTitle(title);
				applyOptions(question, options);
				questionService.insert(question);
			}
			ajax(response, "保存成功");
		}catch(Exception e){
			ajax(response, "保存失败：" + e.getMessage());
		}
	}

	@RequestMapping(value="delsurvey")
	public void delsurvey(HttpServletRequest request,HttpServletResponse response){
		String lessonid = request.getParameter("lessonid");
		String surveyKey = request.getParameter("surveyKey");
		Integer surveyId = parseInteger(request.getParameter("surveyId"));
		deleteSurveyQuestions(getSessionUser(request).getId()+"", lessonid, surveyKey, surveyId);
		ajax(response, "删除成功");
	}

	@RequestMapping(value="listdata")
	public void listdata(HttpServletRequest request, HttpServletResponse response){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("title", CommonUtil.changeEncoding(request.getParameter("queryname")));
		map.put("type", getSessionUser(request).getId());
		List<Question> list = questionService.getList(map);
		JSONArray jsonarray = JSONArray.fromObject(list);
		String json = "{\"data\":"+jsonarray.toString()+"}";
		ajax(response, json);
	}

	@RequestMapping(value="add")
	public void add(HttpServletRequest request, HttpServletResponse response, Question question){
		question.setType(getSessionUser(request).getId()+"");
		question.setSecondtype(request.getParameter("lessonid"));
		question.setLessonid(request.getParameter("lessonid"));
		question.setSurveyId(createSurveyId(getSessionUser(request).getId()+""));
		question.setSurveyName("默认问卷");
		question.setSortno(1);
		question.setOptionCount(5);
		question.setOption1("完全不达成(1分)");
		question.setOption2("基本不达成(2分)");
		question.setOption3("基本达成(3分)");
		question.setOption4("达成(4分)");
		question.setOption5("完全达成(5分)");
		question.setOption1score("20");
		question.setOption2score("40");
		question.setOption3score("60");
		question.setOption4score("80");
		question.setOption5score("100");
		questionService.insert(question);
		ajax(response, "新增成功");
	}

	@RequestMapping(value="update")
	public void update(HttpServletRequest request, HttpServletResponse response, Question question){
		question.setType(getSessionUser(request).getId()+"");
		question.setSecondtype(request.getParameter("lessonid"));
		question.setLessonid(request.getParameter("lessonid"));
		questionService.update(question);
		ajax(response, "修改成功");
	}
	
	@RequestMapping(value="del")
	public void del(HttpServletRequest request,HttpServletResponse response){
		String id = request.getParameter("id");
		questionService.delete(id);
		ajax(response, "删除成功");
	}

	@RequestMapping(value="delscore")
	public void delscore(HttpServletRequest request,HttpServletResponse response){
		questionscoreService.delete(getSessionUser(request).getId()+"");
		ajax(response, "操作成功");
	}

	private ModelAndView showQuestionAnswer(HttpServletRequest request, String msg){
		Map<String,Object> map = new HashMap<String,Object>();
		String queryid = request.getParameter("queryid");
		List<Score> scorelist = getStudentCourseList(getSessionUser(request).getId());
		Score score = getSelectedScore(queryid, scorelist);
		if(isBlank(queryid) && score.getTeacherid() != null && !isBlank(score.getLesson())){
			queryid = score.getTeacherid() + "," + score.getLesson();
		}
		map.put("msg", msg);
		map.put("queryid", queryid);
		map.put("scorelist", scorelist);
		map.put("surveylist", new ArrayList<Map<String,Object>>());
		if(score.getTeacherid() != null && !isBlank(score.getLesson())){
			Map<String,Object> questionQuery = new HashMap<String,Object>();
			questionQuery.put("type", score.getTeacherid()+"");
			questionQuery.put("secondtype", score.getLesson());
			List<Question> questionlist = questionService.getList(questionQuery);
			List<Map<String,Object>> surveylist = buildSurveyList(questionlist);
			markAnsweredSurveys(surveylist, getSessionUser(request).getId(), score.getTeacherid(), score.getLesson());
			map.put("surveylist", surveylist);
			map.put("teacherid", score.getTeacherid());
			map.put("lessonid", score.getLesson());
		}
		return jsp("questionanswer", map, request);
	}

	private List<Score> getStudentCourseList(Integer userid){
		LinkedHashMap<String,Score> result = new LinkedHashMap<String,Score>();
		Map<String,Object> bindQuery = new HashMap<String,Object>();
		bindQuery.put("userid", userid);
		List<LessonStudent> bindings = lessonStudentService.getList(bindQuery);
		for(LessonStudent binding : bindings){
			if(binding.getTeacherid() == null || binding.getLessonid() == null){
				continue;
			}
			if(binding.getLesson() == null || binding.getLesson().getUserid() == null || !binding.getLesson().getUserid().equals(binding.getTeacherid())){
				continue;
			}
			Score score = new Score();
			score.setTeacherid(binding.getTeacherid());
			score.setLesson(binding.getLessonid()+"");
			score.setLessonentity(binding.getLesson());
			score.setTeacher(userService.getUserById(binding.getTeacherid()+""));
			score.setQueryid(binding.getTeacherid() + "," + binding.getLessonid());
			result.put(score.getQueryid(), score);
		}
		return new ArrayList<Score>(result.values());
	}

	private Score getSelectedScore(String queryid, List<Score> scorelist){
		if(isBlank(queryid)){
			if(scorelist != null && scorelist.size() > 0){
				return scorelist.get(0);
			}
			return new Score();
		}
		String[] parts = queryid.split(",");
		if(parts.length < 2){
			return new Score();
		}
		for(Score score : scorelist){
			if(queryid.equals(score.getQueryid())){
				return score;
			}
		}
		return new Score();
	}

	private List<Question> getQuestionsForSurvey(String teacherid, String lessonid, Integer surveyId){
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("type", teacherid);
		query.put("secondtype", lessonid);
		if(surveyId != null){
			query.put("surveyId", surveyId);
			return questionService.getList(query);
		}
		List<Question> all = questionService.getList(query);
		List<Question> legacy = new ArrayList<Question>();
		for(Question question : all){
			if(question.getSurveyId() == null){
				legacy.add(question);
			}
		}
		return legacy;
	}

	private List<Map<String,Object>> buildSurveyList(List<Question> questions){
		LinkedHashMap<String,Map<String,Object>> grouped = new LinkedHashMap<String,Map<String,Object>>();
		for(Question question : questions){
			String key = getSurveyKey(question);
			Map<String,Object> survey = grouped.get(key);
			if(survey == null){
				survey = new LinkedHashMap<String,Object>();
				survey.put("surveyKey", key);
				survey.put("surveyId", question.getSurveyId() == null ? "" : question.getSurveyId()+"");
				survey.put("surveyName", isBlank(question.getSurveyName()) ? "默认问卷" : question.getSurveyName());
				survey.put("surveyDesc", question.getSurveyDesc() == null ? "" : question.getSurveyDesc());
				survey.put("lessonid", question.getSecondtype());
				survey.put("lessonName", question.getLesson() == null ? "" : question.getLesson().getName());
				survey.put("teacherid", question.getType());
				survey.put("questionCount", 0);
				survey.put("answered", false);
				survey.put("questions", new ArrayList<Map<String,Object>>());
				grouped.put(key, survey);
			}
			@SuppressWarnings("unchecked")
			List<Map<String,Object>> questionMaps = (List<Map<String,Object>>)survey.get("questions");
			Map<String,Object> questionMap = new LinkedHashMap<String,Object>();
			questionMap.put("id", question.getId());
			questionMap.put("title", question.getTitle());
			questionMap.put("sortno", question.getSortno() == null ? questionMaps.size() + 1 : question.getSortno());
			questionMap.put("options", getOptionMaps(question));
			questionMaps.add(questionMap);
			survey.put("questionCount", questionMaps.size());
		}
		return new ArrayList<Map<String,Object>>(grouped.values());
	}

	private List<Map<String,Object>> filterSurveyList(List<Map<String,Object>> surveylist, String keyword){
		if(isBlank(keyword)){
			return surveylist;
		}
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> survey : surveylist){
			if(containsText(survey.get("surveyName"), keyword) || containsText(survey.get("lessonName"), keyword)){
				result.add(survey);
				continue;
			}
			@SuppressWarnings("unchecked")
			List<Map<String,Object>> questions = (List<Map<String,Object>>)survey.get("questions");
			for(Map<String,Object> question : questions){
				if(containsText(question.get("title"), keyword)){
					result.add(survey);
					break;
				}
			}
		}
		return result;
	}

	private void markAnsweredSurveys(List<Map<String,Object>> surveylist, Integer userid, Integer teacherid, String lessonid){
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("userid", userid);
		query.put("teacherid", teacherid);
		query.put("lesson", lessonid);
		List<Questionscore> scores = questionscoreService.getList(query);
		for(Map<String,Object> survey : surveylist){
			String surveyId = survey.get("surveyId")+"";
			boolean answered = false;
			for(Questionscore score : scores){
				if(isBlank(surveyId) && score.getSurveyId() == null){
					answered = true;
					break;
				}
				if(!isBlank(surveyId) && score.getSurveyId() != null && surveyId.equals(score.getSurveyId()+"")){
					answered = true;
					break;
				}
			}
			survey.put("answered", answered);
		}
	}

	private Map<Integer,List<Questionscore>> groupScoresByQuestion(List<Questionscore> scores){
		Map<Integer,List<Questionscore>> result = new HashMap<Integer,List<Questionscore>>();
		for(Questionscore score : scores){
			if(score.getQuestionid() == null){
				continue;
			}
			List<Questionscore> list = result.get(score.getQuestionid());
			if(list == null){
				list = new ArrayList<Questionscore>();
				result.put(score.getQuestionid(), list);
			}
			list.add(score);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void enrichSurveyStats(Map<String,Object> survey, Map<Integer,List<Questionscore>> scoreMap){
		List<Map<String,Object>> questions = (List<Map<String,Object>>)survey.get("questions");
		Set<Integer> studentIds = new HashSet<Integer>();
		BigDecimal total = BigDecimal.ZERO;
		int answerCount = 0;
		for(Map<String,Object> question : questions){
			Integer questionId = toInteger(question.get("id"));
			List<Questionscore> questionScores = scoreMap.get(questionId);
			if(questionScores == null){
				questionScores = new ArrayList<Questionscore>();
			}
			for(Questionscore score : questionScores){
				if(score.getUserid() != null){
					studentIds.add(score.getUserid());
				}
				total = total.add(toDecimal(score.getAvgscore()));
				answerCount++;
			}
			enrichQuestionStats(question, questionScores);
		}
		survey.put("answerRecordCount", answerCount);
		survey.put("responseStudentCount", studentIds.size());
		survey.put("avgScore", answerCount == 0 ? "0.00" : formatDecimal(total.divide(new BigDecimal(answerCount), 2, RoundingMode.HALF_UP)));
	}

	@SuppressWarnings("unchecked")
	private void enrichQuestionStats(Map<String,Object> question, List<Questionscore> scores){
		List<Map<String,Object>> options = (List<Map<String,Object>>)question.get("options");
		int totalCount = scores.size();
		BigDecimal totalScore = BigDecimal.ZERO;
		for(Questionscore score : scores){
			totalScore = totalScore.add(toDecimal(score.getAvgscore()));
		}
		for(Map<String,Object> option : options){
			String optionScore = option.get("score") == null ? "" : option.get("score").toString();
			Integer optionIndex = toInteger(option.get("index"));
			int count = 0;
			for(Questionscore score : scores){
				if(isSelectedOption(score, optionIndex, optionScore)){
					count++;
				}
			}
			option.put("count", count);
			option.put("rate", totalCount == 0 ? "0.00%" : formatPercent(count, totalCount));
		}
		question.put("answerCount", totalCount);
		question.put("avgScore", totalCount == 0 ? "0.00" : formatDecimal(totalScore.divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP)));
	}

	private boolean isSelectedOption(Questionscore score, Integer optionIndex, String optionScore){
		if(optionIndex != null){
			Integer stored = getStoredOptionScore(score, optionIndex);
			if(stored != null && toDecimal(optionScore).compareTo(new BigDecimal(stored)) == 0){
				return true;
			}
		}
		return sameScore(optionScore, score.getAvgscore());
	}

	private Integer getStoredOptionScore(Questionscore score, Integer optionIndex){
		if(optionIndex == null){
			return null;
		}
		if(optionIndex == 1){
			return score.getOption1score();
		}else if(optionIndex == 2){
			return score.getOption2score();
		}else if(optionIndex == 3){
			return score.getOption3score();
		}else if(optionIndex == 4){
			return score.getOption4score();
		}else if(optionIndex == 5){
			return score.getOption5score();
		}else if(optionIndex == 6){
			return score.getOption6score();
		}
		return null;
	}

	private List<Map<String,Object>> getOptionMaps(Question question){
		List<Map<String,Object>> options = new ArrayList<Map<String,Object>>();
		addOption(options, 1, question.getOption1(), question.getOption1score());
		addOption(options, 2, question.getOption2(), question.getOption2score());
		addOption(options, 3, question.getOption3(), question.getOption3score());
		addOption(options, 4, question.getOption4(), question.getOption4score());
		addOption(options, 5, question.getOption5(), question.getOption5score());
		addOption(options, 6, question.getOption6(), question.getOption6score());
		addOption(options, 7, question.getOption7(), question.getOption7score());
		addOption(options, 8, question.getOption8(), question.getOption8score());
		return options;
	}

	private void addOption(List<Map<String,Object>> options, int index, String text, String score){
		if(isBlank(text)){
			return;
		}
		if(index == 1 && "0".equals(text.trim())){
			return;
		}
		Map<String,Object> option = new LinkedHashMap<String,Object>();
		option.put("index", index);
		option.put("text", text);
		option.put("score", isBlank(score) ? "0" : score);
		options.add(option);
	}

	private void applyOptions(Question question, JSONArray options){
		question.setOption1(null);
		question.setOption2(null);
		question.setOption3(null);
		question.setOption4(null);
		question.setOption5(null);
		question.setOption6(null);
		question.setOption7(null);
		question.setOption8(null);
		question.setOption1score(null);
		question.setOption2score(null);
		question.setOption3score(null);
		question.setOption4score(null);
		question.setOption5score(null);
		question.setOption6score(null);
		question.setOption7score(null);
		question.setOption8score(null);
		int count = Math.min(options.size(), 8);
		for(int i=0; i<count; i++){
			JSONObject option = options.getJSONObject(i);
			String text = trim(option.getString("text"));
			String score = trim(option.getString("score"));
			if(isBlank(score)){
				score = "0";
			}
			setOption(question, i + 1, text, score);
		}
		question.setOptionCount(count);
	}

	private void setOption(Question question, int index, String text, String score){
		if(index == 1){
			question.setOption1(text);
			question.setOption1score(score);
		}else if(index == 2){
			question.setOption2(text);
			question.setOption2score(score);
		}else if(index == 3){
			question.setOption3(text);
			question.setOption3score(score);
		}else if(index == 4){
			question.setOption4(text);
			question.setOption4score(score);
		}else if(index == 5){
			question.setOption5(text);
			question.setOption5score(score);
		}else if(index == 6){
			question.setOption6(text);
			question.setOption6score(score);
		}else if(index == 7){
			question.setOption7(text);
			question.setOption7score(score);
		}else if(index == 8){
			question.setOption8(text);
			question.setOption8score(score);
		}
	}

	private String getAnswerScore(String answerValue){
		if(isBlank(answerValue)){
			return "0";
		}
		int split = answerValue.indexOf("|");
		if(split >= 0 && split < answerValue.length() - 1){
			return answerValue.substring(split + 1);
		}
		return answerValue;
	}

	private Integer getAnswerOptionIndex(String answerValue){
		if(isBlank(answerValue)){
			return null;
		}
		int split = answerValue.indexOf("|");
		if(split <= 0){
			return null;
		}
		return parseInteger(answerValue.substring(0, split));
	}

	private void setAnswerOption(Questionscore questionscore, Integer optionIndex, String score){
		Integer value = parseInteger(score);
		if(optionIndex == null || value == null){
			return;
		}
		if(optionIndex == 1){
			questionscore.setOption1score(value);
		}else if(optionIndex == 2){
			questionscore.setOption2score(value);
		}else if(optionIndex == 3){
			questionscore.setOption3score(value);
		}else if(optionIndex == 4){
			questionscore.setOption4score(value);
		}else if(optionIndex == 5){
			questionscore.setOption5score(value);
		}else if(optionIndex == 6){
			questionscore.setOption6score(value);
		}
	}

	private void deleteSurveyQuestions(String teacherid, String lessonid, String surveyKey, Integer surveyId){
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("type", teacherid);
		query.put("secondtype", lessonid);
		List<Question> all = questionService.getList(query);
		boolean deleteLegacy = surveyKey != null && surveyKey.indexOf("legacy_") == 0;
		boolean deleteExistingSurvey = surveyKey != null && surveyKey.indexOf("survey_") == 0;
		for(Question question : all){
			if(deleteLegacy && question.getSurveyId() == null){
				questionService.delete(question.getId()+"");
			}else if(deleteExistingSurvey && surveyId != null && surveyId.equals(question.getSurveyId())){
				questionService.delete(question.getId()+"");
			}else if(!deleteLegacy && !deleteExistingSurvey && surveyId != null && surveyId.equals(question.getSurveyId())){
				questionService.delete(question.getId()+"");
			}
		}
		if(deleteLegacy || deleteExistingSurvey){
			Map<String,Object> scoreQuery = new HashMap<String,Object>();
			scoreQuery.put("teacherid", teacherid);
			scoreQuery.put("lesson", lessonid);
			if(deleteLegacy){
				scoreQuery.put("surveyIdNull", "1");
			}else{
				scoreQuery.put("surveyId", surveyId);
			}
			questionscoreService.deleteBySurvey(scoreQuery);
		}
	}

	private Integer createSurveyId(String teacherid){
		int candidate = (int)(System.currentTimeMillis() % 1000000000L);
		if(candidate < 1){
			candidate = candidate * -1 + 1;
		}
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("type", teacherid);
		for(int i=0; i<1000; i++){
			query.put("surveyId", candidate);
			if(questionService.getList(query).size() == 0){
				return candidate;
			}
			candidate++;
		}
		return candidate;
	}

	private String getSurveyKey(Question question){
		if(question.getSurveyId() != null){
			return "survey_" + question.getSurveyId();
		}
		return "legacy_" + question.getSecondtype();
	}

	private Map<String,String> getSurveyNameMap(String teacherid, String lessonid){
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("type", teacherid);
		query.put("secondtype", lessonid);
		List<Question> questions = questionService.getList(query);
		Map<String,String> result = new HashMap<String,String>();
		for(Question question : questions){
			String key = question.getSecondtype() + ":" + (question.getSurveyId() == null ? "legacy" : question.getSurveyId()+"");
			if(!result.containsKey(key)){
				result.put(key, isBlank(question.getSurveyName()) ? "默认问卷" : question.getSurveyName());
			}
		}
		return result;
	}

	private String resolveSurveyName(Map<String,String> surveyNameMap, String lessonid, Integer surveyId){
		String key = lessonid + ":" + (surveyId == null ? "legacy" : surveyId+"");
		String name = surveyNameMap.get(key);
		return isBlank(name) ? "默认问卷" : name;
	}

	private Integer parseInteger(String value){
		if(isBlank(value)){
			return null;
		}
		try{
			return Integer.parseInt(value.trim());
		}catch(Exception e){
			return null;
		}
	}

	private Integer toInteger(Object value){
		if(value == null){
			return null;
		}
		try{
			return Integer.parseInt(value.toString());
		}catch(Exception e){
			return null;
		}
	}

	private BigDecimal toDecimal(String value){
		if(isBlank(value)){
			return BigDecimal.ZERO;
		}
		try{
			return new BigDecimal(value.trim());
		}catch(Exception e){
			return BigDecimal.ZERO;
		}
	}

	private boolean sameScore(String left, String right){
		return toDecimal(left).compareTo(toDecimal(right)) == 0;
	}

	private String formatPercent(int count, int total){
		if(total == 0){
			return "0.00%";
		}
		BigDecimal value = new BigDecimal(count).multiply(new BigDecimal("100")).divide(new BigDecimal(total), 2, RoundingMode.HALF_UP);
		return formatDecimal(value) + "%";
	}

	private String formatDecimal(BigDecimal value){
		if(value == null){
			return "0.00";
		}
		return new DecimalFormat("0.00").format(value);
	}

	private boolean containsText(Object source, String keyword){
		if(source == null || keyword == null){
			return false;
		}
		return source.toString().indexOf(keyword) >= 0;
	}

	private String trim(String value){
		return value == null ? "" : value.trim();
	}

	private boolean isBlank(String value){
		return value == null || value.trim().length() == 0;
	}
}

package com.mywork.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.mywork.bean.AnalysisComponent;
import com.mywork.bean.ScoreImportItem;

public class ScoreExcelParser {
	private static final String SOURCE_LOCAL = "本地规则识别";
	private static final String SOURCE_AI = "AI智能识别";

	private ScoreExcelParser(){
	}

	public static ParseResult parseRows(ArrayList<ArrayList<Object>> rows, List<AnalysisComponent> components){
		ParseResult result = parseByHeader(rows, components);
		if(result.items.size() > 0){
			result.source = SOURCE_LOCAL;
			result.message = "已根据表头和课程成绩组成完成识别";
		}
		return result;
	}

	public static ParseResult parseAiContent(String aiContent){
		ParseResult result = new ParseResult();
		result.source = SOURCE_AI;
		result.message = "AI 已完成成绩表结构识别";
		JSONObject json = JSONObject.fromObject(AnalysisJsonUtil.extractJson(aiContent));
		result.tableType = firstString(json, new String[]{"tableType","type","scoreType","成绩表类型","表格类型"});
		result.questionScoreMessage = firstString(json, new String[]{"questionScoreMessage","questionMessage","小题说明","小题得分说明"});
		if(json.containsKey("fieldMappings") && json.get("fieldMappings") instanceof JSONObject){
			JSONObject mappings = json.getJSONObject("fieldMappings");
			for(Object key : mappings.keySet()){
				result.fieldMappings.put(key.toString(), mappings.get(key).toString());
			}
		}
		JSONArray array = null;
		if(json.containsKey("scores") && json.get("scores") instanceof JSONArray){
			array = json.getJSONArray("scores");
		}else if(json.containsKey("items") && json.get("items") instanceof JSONArray){
			array = json.getJSONArray("items");
		}else if(json.containsKey("data") && json.get("data") instanceof JSONArray){
			array = json.getJSONArray("data");
		}
		if(array == null){
			return result;
		}
		for(int i=0; i<array.size(); i++){
			JSONObject itemJson = array.getJSONObject(i);
			Integer rowNumber = getInt(itemJson, new String[]{"rowNumber","row","行号"}, i + 1);
			String no = normalizeNo(firstString(itemJson, new String[]{"no","studentNo","studentId","学号","学生学号"}));
			String name = clean(firstString(itemJson, new String[]{"name","studentName","姓名","学生姓名"}));
			BigDecimal confidence = parseDecimal(firstString(itemJson, new String[]{"confidence","置信度"}));
			String itemSource = firstString(itemJson, new String[]{"scoreSource","source","来源"});
			JSONArray scoreArray = null;
			if(itemJson.containsKey("components") && itemJson.get("components") instanceof JSONArray){
				scoreArray = itemJson.getJSONArray("components");
			}else if(itemJson.containsKey("scores") && itemJson.get("scores") instanceof JSONArray){
				scoreArray = itemJson.getJSONArray("scores");
			}
			if(scoreArray != null){
				for(int j=0; j<scoreArray.size(); j++){
					JSONObject scoreJson = scoreArray.getJSONObject(j);
					ScoreImportItem item = new ScoreImportItem();
					item.setRowNumber(rowNumber);
					item.setNo(no);
					item.setName(name);
					item.setComponentName(clean(firstString(scoreJson, new String[]{"componentName","component","name","成绩项","组成项"})));
					String rawScore = firstString(scoreJson, new String[]{"score","value","分数","成绩"});
					item.setRawScore(rawScore);
					item.setScore(parseDecimal(rawScore));
					item.setScoreSource(defaultText(firstString(scoreJson, new String[]{"scoreSource","source","来源"}), itemSource));
					item.setTotalScoreSource(firstString(scoreJson, new String[]{"totalScoreSource","总分来源"}));
					item.setConfidence(confidence);
					result.items.add(item);
				}
			}else{
				ScoreImportItem item = new ScoreImportItem();
				item.setRowNumber(rowNumber);
				item.setNo(no);
				item.setName(name);
				item.setComponentName(clean(firstString(itemJson, new String[]{"componentName","component","scoreName","成绩项","组成项"})));
				String rawScore = firstString(itemJson, new String[]{"score","value","分数","成绩"});
				item.setRawScore(rawScore);
				item.setScore(parseDecimal(rawScore));
				item.setScoreSource(itemSource);
				item.setTotalScoreSource(firstString(itemJson, new String[]{"totalScoreSource","总分来源"}));
				item.setConfidence(confidence);
				result.items.add(item);
			}
		}
		return result;
	}

	public static String buildAiPrompt(ArrayList<ArrayList<Object>> rows, List<AnalysisComponent> components, String lessonName){
		StringBuilder componentText = new StringBuilder();
		if(components != null){
			for(int i=0; i<components.size(); i++){
				AnalysisComponent component = components.get(i);
				if(i > 0){
					componentText.append("、");
				}
				componentText.append(component.getComponentName());
				if(component.getRate() != null){
					componentText.append("(").append(component.getRate()).append("%)");
				}
			}
		}
		return "请分析下面的 Excel 成绩表文本，识别其中可以确定读取的学生成绩数据。"
			+ "当前课程：" + defaultText(lessonName, "未提供") + "。系统已有成绩组成项：" + componentText.toString() + "。"
			+ "只返回 JSON，不要 Markdown，不要解释。JSON 格式必须为："
			+ "{\"tableType\":\"期末成绩\",\"fieldMappings\":{\"学号\":\"A列\",\"姓名\":\"B列\",\"期末成绩\":\"G列\"},"
			+ "\"questionScoreMessage\":\"识别到小题得分，本次只导入期末总分\","
			+ "\"scores\":[{\"rowNumber\":2,\"no\":\"2024010101\",\"name\":\"张三\","
			+ "\"components\":[{\"componentName\":\"期末成绩\",\"score\":88,\"scoreSource\":\"直接读取期末总分列\",\"totalScoreSource\":\"总分列\"}],\"confidence\":0.92}]}。"
			+ "要求：1. 判断表格是否包含成绩数据；2. componentName 必须尽量映射到系统已有成绩组成项，不要创造无关成绩项；"
			+ "3. 如果同时存在期末小题得分和期末总分，只返回期末总分；4. 如果没有明确期末总分但多个小题得分可无歧义相加，可返回相加结果，并在 totalScoreSource 写明由小题合计；"
			+ "5. 如果有缺考、缓考、作弊、补考、加权、折算等歧义，不要返回该成绩，或在 scoreSource 说明需要人工确认；"
			+ "6. 不要把第一题、第二题等小题作为独立成绩项；7. 学号保持原样字符串；8. 不能确定的行不要返回。"
			+ "表格内容如下：\n" + rowsToText(rows, 100, 40);
	}

	public static String rowsToText(ArrayList<ArrayList<Object>> rows, int maxRows, int maxCols){
		StringBuilder builder = new StringBuilder();
		if(rows == null){
			return "";
		}
		int rowCount = Math.min(rows.size(), maxRows);
		for(int i=0; i<rowCount; i++){
			ArrayList<Object> row = rows.get(i);
			builder.append("第").append(i + 1).append("行：");
			if(row != null){
				int colCount = Math.min(row.size(), maxCols);
				for(int j=0; j<colCount; j++){
					if(j > 0){
						builder.append(" | ");
					}
					builder.append(columnName(j)).append("=").append(cell(row, j));
				}
			}
			builder.append('\n');
		}
		return builder.toString();
	}

	private static ParseResult parseByHeader(ArrayList<ArrayList<Object>> rows, List<AnalysisComponent> components){
		ParseResult result = new ParseResult();
		if(rows == null || rows.size() == 0 || components == null || components.size() == 0){
			return result;
		}
		for(int i=0; i<Math.min(rows.size(), 12); i++){
			ArrayList<Object> headerRow = rows.get(i);
			HeaderMatch header = matchHeader(headerRow, components);
			boolean hasQuestionTotal = header.questionColumns.size() > 1 && isFinalLikeComponent(header.finalComponentName);
			if(header.noIndex < 0 || (header.scoreColumns.size() == 0 && !hasQuestionTotal)){
				continue;
			}
			if(header.nameIndex >= 0){
				result.fieldMappings.put("姓名", columnName(header.nameIndex));
			}
			result.fieldMappings.put("学号", columnName(header.noIndex));
			if(hasQuestionTotal){
				result.fieldMappings.put(header.finalComponentName, "小题得分合计：" + questionColumnNames(header.questionColumns));
			}
			for(ComponentColumn column : header.scoreColumns){
				result.fieldMappings.put(column.componentName, columnName(column.index) + "：" + column.headerName);
			}
			if(header.finalTotalColumn != null && header.questionColumns.size() > 0){
				result.tableType = "期末成绩";
				result.questionScoreMessage = "识别到小题得分列，但本次只准备导入期末总分列：" + header.finalTotalColumn.headerName;
			}else if(header.finalTotalColumn == null && header.questionColumns.size() > 1 && isFinalLikeComponent(header.finalComponentName)){
				result.tableType = "期末成绩";
				result.questionScoreMessage = "识别到小题得分列，未发现明确期末总分列；系统将尝试用小题得分合计作为期末总分。";
			}else{
				result.tableType = guessTableType(header.scoreColumns);
			}
			for(int r=i + 1; r<rows.size(); r++){
				ArrayList<Object> row = rows.get(r);
				if(isRowBlank(row)){
					continue;
				}
				String no = normalizeNo(cell(row, header.noIndex));
				String name = header.nameIndex >= 0 ? clean(cell(row, header.nameIndex)) : "";
				if(isBlank(no) || !isLikelyStudentNo(no)){
					continue;
				}
				if(header.finalTotalColumn == null && header.questionColumns.size() > 1 && isFinalLikeComponent(header.finalComponentName)){
					ScoreImportItem item = buildQuestionTotalItem(row, r + 1, no, name, header);
					result.items.add(item);
					continue;
				}
				for(ComponentColumn column : header.scoreColumns){
					String rawScore = cell(row, column.index);
					if(isBlank(rawScore)){
						continue;
					}
					ScoreImportItem item = new ScoreImportItem();
					item.setRowNumber(Integer.valueOf(r + 1));
					item.setNo(no);
					item.setName(name);
					item.setComponentName(column.componentName);
					item.setRawScore(rawScore);
					item.setScore(parseDecimal(rawScore));
					item.setScoreSource(column.scoreSource);
					item.setTotalScoreSource(column.totalScoreSource);
					item.setConfidence(new BigDecimal("0.95"));
					if(column == header.finalTotalColumn && header.questionColumns.size() > 0){
						String warning = compareQuestionTotal(row, header, item.getScore());
						if(!isBlank(warning)){
							item.setMessage(warning);
						}
					}
					result.items.add(item);
				}
			}
			return result;
		}
		return result;
	}

	private static HeaderMatch matchHeader(ArrayList<Object> row, List<AnalysisComponent> components){
		HeaderMatch match = new HeaderMatch();
		if(row == null){
			return match;
		}
		Set<String> usedComponents = new HashSet<String>();
		for(int i=0; i<row.size(); i++){
			String header = clean(cell(row, i));
			String normalized = normalize(header);
			if(isBlank(normalized)){
				continue;
			}
			if(match.noIndex < 0 && isStudentNoHeader(normalized)){
				match.noIndex = i;
				continue;
			}
			if(match.nameIndex < 0 && isNameHeader(normalized)){
				match.nameIndex = i;
				continue;
			}
			if(isQuestionScoreHeader(normalized)){
				ComponentColumn question = new ComponentColumn();
				question.index = i;
				question.headerName = header;
				match.questionColumns.add(question);
				continue;
			}
			AnalysisComponent component = findBestComponent(header, components);
			if(component == null){
				continue;
			}
			String componentName = component.getComponentName();
			if(usedComponents.contains(componentName)){
				continue;
			}
			ComponentColumn column = new ComponentColumn();
			column.index = i;
			column.headerName = header;
			column.componentName = componentName;
			column.scoreSource = "直接读取列：" + header;
			column.totalScoreSource = "";
			if((isFinalTotalHeader(normalized) || isGenericTotalHeader(normalized)) && isFinalLikeComponent(componentName)){
				column.scoreSource = "直接读取期末总分列：" + header;
				column.totalScoreSource = "总分列";
				match.finalTotalColumn = column;
				match.finalComponentName = componentName;
			}
			match.scoreColumns.add(column);
			usedComponents.add(componentName);
			if(match.finalComponentName == null && isFinalLikeComponent(componentName)){
				match.finalComponentName = componentName;
			}
		}
		if(match.finalTotalColumn == null){
			String finalComponent = findFinalComponentName(components);
			if(finalComponent != null && match.questionColumns.size() > 1){
				match.finalComponentName = finalComponent;
			}
		}
		return match;
	}

	private static ScoreImportItem buildQuestionTotalItem(ArrayList<Object> row, int rowNumber, String no, String name, HeaderMatch header){
		ScoreImportItem item = new ScoreImportItem();
		item.setRowNumber(Integer.valueOf(rowNumber));
		item.setNo(no);
		item.setName(name);
		item.setComponentName(header.finalComponentName);
		item.setScoreSource("由小题得分合计");
		item.setTotalScoreSource("小题合计");
		item.setConfidence(new BigDecimal("0.80"));
		BigDecimal total = BigDecimal.ZERO;
		List<String> rawValues = new ArrayList<String>();
		List<String> badValues = new ArrayList<String>();
		for(ComponentColumn column : header.questionColumns){
			String raw = cell(row, column.index);
			if(isBlank(raw)){
				badValues.add(column.headerName + "=空");
				continue;
			}
			rawValues.add(column.headerName + "=" + raw);
			if(hasAmbiguousMarker(raw)){
				badValues.add(column.headerName + "=" + raw);
				continue;
			}
			BigDecimal score = parseDecimal(raw);
			if(score == null){
				badValues.add(column.headerName + "=" + raw);
			}else{
				total = total.add(score);
			}
		}
		item.setRawScore(join(rawValues, "；"));
		if(badValues.size() > 0 || rawValues.size() == 0){
			item.setMessage("小题得分存在缺失或特殊标记，需人工确认：" + join(badValues, "；"));
		}else{
			item.setScore(total.setScale(2, BigDecimal.ROUND_HALF_UP));
		}
		return item;
	}

	private static String compareQuestionTotal(ArrayList<Object> row, HeaderMatch header, BigDecimal totalScore){
		if(totalScore == null || header == null || header.questionColumns.size() == 0){
			return "";
		}
		BigDecimal total = BigDecimal.ZERO;
		int count = 0;
		for(ComponentColumn column : header.questionColumns){
			String raw = cell(row, column.index);
			if(isBlank(raw)){
				return "";
			}
			if(hasAmbiguousMarker(raw)){
				return "";
			}
			BigDecimal score = parseDecimal(raw);
			if(score == null){
				return "";
			}
			total = total.add(score);
			count++;
		}
		if(count == 0){
			return "";
		}
		total = total.setScale(2, BigDecimal.ROUND_HALF_UP);
		if(total.compareTo(totalScore.setScale(2, BigDecimal.ROUND_HALF_UP)) != 0){
			return "期末总分与小题得分合计不一致：总分=" + totalScore + "，小题合计=" + total;
		}
		return "";
	}

	private static AnalysisComponent findBestComponent(String header, List<AnalysisComponent> components){
		String normalizedHeader = normalizeScoreHeader(header);
		if(isBlank(normalizedHeader)){
			return null;
		}
		AnalysisComponent best = null;
		int bestScore = 0;
		for(AnalysisComponent component : components){
			String componentName = component.getComponentName();
			String normalizedComponent = normalizeScoreHeader(componentName);
			int score = matchScore(normalizedHeader, normalizedComponent, componentName);
			if(score > bestScore){
				bestScore = score;
				best = component;
			}
		}
		return bestScore >= 45 ? best : null;
	}

	private static int matchScore(String header, String component, String componentName){
		if(isBlank(header) || isBlank(component)){
			return 0;
		}
		if(header.equals(component)){
			return 100;
		}
		if(header.indexOf(component) >= 0 || component.indexOf(header) >= 0){
			return 80;
		}
		if(isFinalTotalHeader(header) && isFinalLikeComponent(componentName)){
			return 92;
		}
		if(isGenericTotalHeader(header) && isFinalLikeComponent(componentName)){
			return 70;
		}
		if(isUsualLike(header) && isUsualLike(componentName)){
			return 85;
		}
		if(isHomeworkLike(header) && isHomeworkLike(componentName)){
			return 85;
		}
		if(isExperimentLike(header) && isExperimentLike(componentName)){
			return 85;
		}
		if(isMiddleLike(header) && isMiddleLike(componentName)){
			return 85;
		}
		if(isFinalLike(header) && isFinalLikeComponent(componentName)){
			return 85;
		}
		if(isTotalLike(header) && isTotalLike(componentName)){
			return 85;
		}
		return 0;
	}

	private static String guessTableType(List<ComponentColumn> columns){
		if(columns == null || columns.size() == 0){
			return "成绩表";
		}
		if(columns.size() > 1){
			return "综合成绩表";
		}
		String name = columns.get(0).componentName;
		if(isFinalLikeComponent(name)){
			return "期末成绩";
		}
		if(isMiddleLike(name)){
			return "期中成绩";
		}
		if(isHomeworkLike(name)){
			return "作业成绩";
		}
		if(isExperimentLike(name)){
			return "实验成绩";
		}
		if(isUsualLike(name)){
			return "平时成绩";
		}
		return name + "成绩表";
	}

	private static String questionColumnNames(List<ComponentColumn> columns){
		List<String> names = new ArrayList<String>();
		if(columns != null){
			for(ComponentColumn column : columns){
				names.add(columnName(column.index) + "：" + column.headerName);
			}
		}
		return join(names, "、");
	}

	private static String findFinalComponentName(List<AnalysisComponent> components){
		if(components == null){
			return null;
		}
		for(AnalysisComponent component : components){
			if(component != null && isFinalLikeComponent(component.getComponentName())){
				return component.getComponentName();
			}
		}
		return null;
	}

	private static boolean isStudentNoHeader(String normalized){
		return "学号".equals(normalized) || "学生编号".equals(normalized) || "学生id".equalsIgnoreCase(normalized)
			|| "studentid".equalsIgnoreCase(normalized) || "studentno".equalsIgnoreCase(normalized) || "no".equalsIgnoreCase(normalized);
	}

	private static boolean isNameHeader(String normalized){
		return "姓名".equals(normalized) || "学生姓名".equals(normalized) || "名字".equals(normalized)
			|| "name".equalsIgnoreCase(normalized) || "studentname".equalsIgnoreCase(normalized);
	}

	private static boolean isFinalTotalHeader(String normalized){
		return normalized.indexOf("期末") >= 0 && (normalized.indexOf("总分") >= 0 || normalized.indexOf("成绩") >= 0 || normalized.indexOf("分数") >= 0)
			|| "期末".equals(normalized) || normalized.indexOf("final") >= 0 && normalized.indexOf("total") >= 0;
	}

	private static boolean isGenericTotalHeader(String normalized){
		return "总分".equals(normalized) || "总成绩".equals(normalized) || "考试总分".equals(normalized)
			|| "卷面总分".equals(normalized) || "total".equalsIgnoreCase(normalized);
	}

	private static boolean isQuestionScoreHeader(String normalized){
		if(normalized.indexOf("总分") >= 0 || normalized.indexOf("总成绩") >= 0 || normalized.indexOf("总评") >= 0){
			return false;
		}
		if(normalized.matches(".*第[一二三四五六七八九十0-9]+题.*")){
			return true;
		}
		if(normalized.matches(".*[一二三四五六七八九十0-9]+题.*得分.*")){
			return true;
		}
		return normalized.matches("q[0-9]+") || normalized.matches("question[0-9]+");
	}

	private static boolean isUsualLike(String value){
		String text = normalize(value);
		return text.indexOf("平时") >= 0 || text.indexOf("过程") >= 0 || text.indexOf("课堂") >= 0 || text.indexOf("表现") >= 0;
	}

	private static boolean isHomeworkLike(String value){
		String text = normalize(value);
		return text.indexOf("作业") >= 0 || text.indexOf("homework") >= 0;
	}

	private static boolean isExperimentLike(String value){
		String text = normalize(value);
		return text.indexOf("实验") >= 0 || text.indexOf("实训") >= 0 || text.indexOf("实践") >= 0 || text.indexOf("lab") >= 0;
	}

	private static boolean isMiddleLike(String value){
		String text = normalize(value);
		return text.indexOf("期中") >= 0 || text.indexOf("mid") >= 0;
	}

	private static boolean isFinalLike(String value){
		String text = normalize(value);
		return text.indexOf("期末") >= 0 || text.indexOf("final") >= 0;
	}

	private static boolean isFinalLikeComponent(String value){
		return isFinalLike(value);
	}

	private static boolean isTotalLike(String value){
		String text = normalize(value);
		return text.indexOf("总评") >= 0 || text.indexOf("总成绩") >= 0 || text.indexOf("总分") >= 0 || text.indexOf("最终") >= 0 || text.indexOf("综合") >= 0 || text.indexOf("total") >= 0;
	}

	private static String normalizeScoreHeader(String value){
		return normalize(value).replace("原始分", "").replace("折算分", "").replace("百分制", "")
			.replace("分数", "").replace("得分", "");
	}

	private static String normalize(String value){
		if(value == null){
			return "";
		}
		return value.replace("成绩", "").replace("考核", "").replace("方式", "").replace("：", "").replace(":", "")
			.replace(" ", "").replace("\t", "").trim().toLowerCase();
	}

	private static BigDecimal parseDecimal(String value){
		if(value == null){
			return null;
		}
		String text = value.replace("%", "").replace("％", "").replace(",", "").trim();
		if(text.length() == 0 || "null".equalsIgnoreCase(text) || hasAmbiguousMarker(text)){
			return null;
		}
		try{
			return new BigDecimal(text).setScale(2, BigDecimal.ROUND_HALF_UP);
		}catch(Exception e){
			return null;
		}
	}

	private static boolean hasAmbiguousMarker(String value){
		if(value == null){
			return false;
		}
		String text = value.trim();
		return text.indexOf("缺考") >= 0 || text.indexOf("作弊") >= 0 || text.indexOf("缓考") >= 0
			|| text.indexOf("补考") >= 0 || text.indexOf("免考") >= 0 || text.indexOf("旷考") >= 0
			|| text.indexOf("加权") >= 0 || text.indexOf("折算") >= 0;
	}

	private static String normalizeNo(String value){
		String text = clean(value);
		if(text.endsWith(".0")){
			text = text.substring(0, text.length() - 2);
		}
		if(text.endsWith(".00")){
			text = text.substring(0, text.length() - 3);
		}
		return text.replace(" ", "");
	}

	private static boolean isLikelyStudentNo(String value){
		if(isBlank(value)){
			return false;
		}
		String text = value.trim();
		return text.length() >= 4 && text.length() <= 50 && text.matches("[0-9A-Za-z_-]+") && text.matches(".*[0-9].*");
	}

	private static String cell(ArrayList<Object> row, int index){
		if(row == null || index < 0 || index >= row.size() || row.get(index) == null){
			return "";
		}
		String value = row.get(index).toString().trim();
		if(value.endsWith(".00")){
			value = value.substring(0, value.length() - 3);
		}
		if(value.endsWith(".0")){
			value = value.substring(0, value.length() - 2);
		}
		return value;
	}

	private static boolean isRowBlank(ArrayList<Object> row){
		if(row == null || row.size() == 0){
			return true;
		}
		for(Object value : row){
			if(value != null && value.toString().trim().length() > 0){
				return false;
			}
		}
		return true;
	}

	private static String clean(String value){
		if(value == null){
			return "";
		}
		return value.replace("\r", " ").replace("\n", " ").replace("\t", " ").replaceAll("\\s+", " ").trim();
	}

	private static String firstString(JSONObject json, String[] keys){
		if(json == null){
			return "";
		}
		for(String key : keys){
			if(json.containsKey(key) && json.get(key) != null){
				String value = json.get(key).toString();
				if(!isBlank(value) && !"null".equalsIgnoreCase(value)){
					return value;
				}
			}
		}
		return "";
	}

	private static Integer getInt(JSONObject json, String[] keys, int fallback){
		for(String key : keys){
			if(json.containsKey(key) && json.get(key) != null){
				try{
					return Integer.valueOf(json.get(key).toString());
				}catch(Exception e){
				}
			}
		}
		return Integer.valueOf(fallback);
	}

	private static String defaultText(String value, String fallback){
		return isBlank(value) ? fallback : value;
	}

	private static boolean isBlank(String value){
		return value == null || value.trim().length() == 0;
	}

	private static String columnName(int index){
		return "列" + (index + 1);
	}

	private static String join(List<String> values, String separator){
		StringBuilder builder = new StringBuilder();
		if(values == null){
			return "";
		}
		for(String value : values){
			if(isBlank(value)){
				continue;
			}
			if(builder.length() > 0){
				builder.append(separator);
			}
			builder.append(value);
		}
		return builder.toString();
	}

	private static class HeaderMatch{
		int noIndex = -1;
		int nameIndex = -1;
		String finalComponentName;
		ComponentColumn finalTotalColumn;
		List<ComponentColumn> scoreColumns = new ArrayList<ComponentColumn>();
		List<ComponentColumn> questionColumns = new ArrayList<ComponentColumn>();
	}

	private static class ComponentColumn{
		int index;
		String headerName;
		String componentName;
		String scoreSource;
		String totalScoreSource;
	}

	public static class ParseResult {
		public String source = SOURCE_LOCAL;
		public String tableType = "成绩表";
		public String message = "";
		public String questionScoreMessage = "";
		public Map<String,String> fieldMappings = new LinkedHashMap<String,String>();
		public List<ScoreImportItem> items = new ArrayList<ScoreImportItem>();
	}
}

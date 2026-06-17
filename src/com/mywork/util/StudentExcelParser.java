package com.mywork.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.mywork.bean.CourseStudentImportItem;

public class StudentExcelParser {
	private static final String SOURCE_LOCAL = "本地规则识别";
	private static final String SOURCE_AI = "AI智能识别";

	private StudentExcelParser(){
	}

	public static ParseResult parseRows(ArrayList<ArrayList<Object>> rows){
		ParseResult result = parseByHeader(rows);
		if(result.items.size() > 0){
			result.source = SOURCE_LOCAL;
			result.message = "已根据表格表头完成学生信息识别";
			return result;
		}
		result = parseByContent(rows);
		if(result.items.size() > 0){
			result.source = SOURCE_LOCAL;
			result.message = "表格没有标准表头，系统已根据内容特征完成初步识别";
		}
		return result;
	}

	public static ParseResult parseAiContent(String aiContent){
		ParseResult result = new ParseResult();
		result.source = SOURCE_AI;
		result.message = "AI 已完成学生信息识别";
		JSONObject json = JSONObject.fromObject(AnalysisJsonUtil.extractJson(aiContent));
		JSONArray array = null;
		if(json.containsKey("students") && json.get("students") instanceof JSONArray){
			array = json.getJSONArray("students");
		}else if(json.containsKey("data") && json.get("data") instanceof JSONArray){
			array = json.getJSONArray("data");
		}else if(json.containsKey("items") && json.get("items") instanceof JSONArray){
			array = json.getJSONArray("items");
		}
		if(array == null){
			return result;
		}
		for(int i=0; i<array.size(); i++){
			JSONObject itemJson = array.getJSONObject(i);
			CourseStudentImportItem item = new CourseStudentImportItem();
			item.setRowNumber(getInt(itemJson, new String[]{"rowNumber","row","行号"}, i + 1));
			item.setName(firstString(itemJson, new String[]{"name","studentName","姓名","学生姓名"}));
			item.setNo(normalizeNo(firstString(itemJson, new String[]{"no","studentNo","studentId","学号","学生学号"})));
			item.setCollegeName(clean(firstString(itemJson, new String[]{"college","collegeName","学院","学院名称"})));
			item.setDeptName(clean(firstString(itemJson, new String[]{"dept","deptName","className","班级","行政班","教学班"})));
			result.items.add(item);
		}
		return result;
	}

	public static String buildAiPrompt(ArrayList<ArrayList<Object>> rows){
		return "请从下面的 Excel 表格文本中识别学生名单。"
			+ "只返回 JSON，不要 Markdown，不要解释。JSON 格式必须为："
			+ "{\"students\":[{\"rowNumber\":2,\"name\":\"张三\",\"no\":\"2024010101\",\"college\":\"计算机学院\",\"dept\":\"软件工程1班\"}]}。"
			+ "要求：只提取真实学生行；忽略标题、说明、合计、成绩项、空行；学号必须保持原样字符串；"
			+ "如果学院或班级在整张表中以合并表头、标题或列值出现，也要补全到每个学生。"
			+ "不能确定的行不要返回。表格内容如下：\n" + rowsToText(rows, 80, 30);
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

	public static void validateItems(ParseResult result, StudentLookup lookup){
		if(result == null){
			return;
		}
		Set<String> seenNos = new HashSet<String>();
		for(CourseStudentImportItem item : result.items){
			List<String> errors = new ArrayList<String>();
			item.setName(clean(item.getName()));
			item.setNo(normalizeNo(item.getNo()));
			item.setCollegeName(clean(item.getCollegeName()));
			item.setDeptName(clean(item.getDeptName()));
			if(isBlank(item.getName())){
				errors.add("姓名为空");
			}
			if(isBlank(item.getNo())){
				errors.add("学号为空");
			}else if(!seenNos.add(item.getNo())){
				errors.add("学号在本次识别结果中重复");
				item.setDuplicate(Boolean.TRUE);
			}
			if(isBlank(item.getCollegeName())){
				errors.add("学院为空");
			}
			if(isBlank(item.getDeptName())){
				errors.add("班级为空");
			}
			if(item.getNo() != null && item.getNo().length() > 50){
				errors.add("学号过长");
			}
			if(item.getName() != null && item.getName().length() > 100){
				errors.add("姓名过长");
			}
			if(lookup != null && !isBlank(item.getNo())){
				item.setExistingStudent(Boolean.valueOf(lookup.existsStudentNo(item.getNo())));
			}
			item.setValid(Boolean.valueOf(errors.size() == 0));
			item.setMessage(join(errors));
		}
	}

	private static ParseResult parseByHeader(ArrayList<ArrayList<Object>> rows){
		ParseResult result = new ParseResult();
		if(rows == null || rows.size() == 0){
			return result;
		}
		for(int i=0; i<Math.min(rows.size(), 12); i++){
			ArrayList<Object> row = rows.get(i);
			Map<String,Integer> header = buildHeaderMap(row);
			if(!header.containsKey("name") || !header.containsKey("no")){
				continue;
			}
			if(!header.containsKey("college") || !header.containsKey("dept")){
				continue;
			}
			for(int j=i + 1; j<rows.size(); j++){
				ArrayList<Object> data = rows.get(j);
				if(isRowBlank(data)){
					continue;
				}
				CourseStudentImportItem item = new CourseStudentImportItem();
				item.setRowNumber(j + 1);
				item.setName(cell(data, header.get("name").intValue()));
				item.setNo(normalizeNo(cell(data, header.get("no").intValue())));
				item.setCollegeName(cell(data, header.get("college").intValue()));
				item.setDeptName(cell(data, header.get("dept").intValue()));
				if(!isLikelyStudent(item)){
					continue;
				}
				result.items.add(item);
			}
			return result;
		}
		return result;
	}

	private static ParseResult parseByContent(ArrayList<ArrayList<Object>> rows){
		ParseResult result = new ParseResult();
		if(rows == null){
			return result;
		}
		String contextCollege = "";
		String contextDept = "";
		for(int i=0; i<rows.size(); i++){
			ArrayList<Object> row = rows.get(i);
			if(isRowBlank(row)){
				continue;
			}
			List<String> cells = new ArrayList<String>();
			for(int j=0; j<row.size(); j++){
				String value = cell(row, j);
				if(!isBlank(value)){
					cells.add(value);
				}
			}
			if(cells.size() <= 2){
				String joined = join(cells);
				if(joined.indexOf("学院") >= 0 && joined.length() <= 80){
					contextCollege = clean(joined.replace("学院：", "").replace("学院:", ""));
				}
				if((joined.indexOf("班") >= 0 || joined.indexOf("级") >= 0) && joined.length() <= 80){
					contextDept = clean(joined.replace("班级：", "").replace("班级:", ""));
				}
			}
			int noIndex = findNoIndex(cells);
			if(noIndex < 0){
				continue;
			}
			int nameIndex = findNameIndex(cells, noIndex);
			if(nameIndex < 0){
				continue;
			}
			CourseStudentImportItem item = new CourseStudentImportItem();
			item.setRowNumber(i + 1);
			item.setNo(normalizeNo(cells.get(noIndex)));
			item.setName(clean(cells.get(nameIndex)));
			item.setCollegeName(findCollegeText(cells, contextCollege));
			item.setDeptName(findDeptText(cells, contextDept));
			if(isLikelyStudent(item)){
				result.items.add(item);
			}
		}
		return result;
	}

	private static Map<String,Integer> buildHeaderMap(ArrayList<Object> row){
		Map<String,Integer> map = new LinkedHashMap<String,Integer>();
		if(row == null){
			return map;
		}
		for(int i=0; i<row.size(); i++){
			String value = clean(cell(row, i)).replace(" ", "");
			if(value.length() == 0){
				continue;
			}
			if(value.indexOf("姓名") >= 0 || value.indexOf("学生姓名") >= 0){
				map.put("name", Integer.valueOf(i));
			}else if(value.indexOf("学号") >= 0 || value.indexOf("学生编号") >= 0 || value.equalsIgnoreCase("studentid")){
				map.put("no", Integer.valueOf(i));
			}else if(value.indexOf("学院") >= 0 || value.indexOf("院系") >= 0){
				map.put("college", Integer.valueOf(i));
			}else if(value.indexOf("班级") >= 0 || value.indexOf("行政班") >= 0 || value.indexOf("教学班") >= 0 || value.equals("班别")){
				map.put("dept", Integer.valueOf(i));
			}
		}
		return map;
	}

	private static int findNoIndex(List<String> cells){
		for(int i=0; i<cells.size(); i++){
			String value = normalizeNo(cells.get(i));
			if(isLikelyNo(value)){
				return i;
			}
		}
		return -1;
	}

	private static int findNameIndex(List<String> cells, int noIndex){
		for(int i=0; i<cells.size(); i++){
			if(i == noIndex){
				continue;
			}
			String value = clean(cells.get(i));
			if(value.length() >= 2 && value.length() <= 20 && value.matches("[\\u4e00-\\u9fa5A-Za-z·. ]+")
				&& value.indexOf("学院") < 0 && value.indexOf("班") < 0 && value.indexOf("成绩") < 0){
				return i;
			}
		}
		return -1;
	}

	private static String findCollegeText(List<String> cells, String fallback){
		for(String value : cells){
			String text = clean(value);
			if(text.indexOf("学院") >= 0 || text.indexOf("院系") >= 0){
				return text.replace("学院：", "").replace("学院:", "");
			}
		}
		return fallback;
	}

	private static String findDeptText(List<String> cells, String fallback){
		for(String value : cells){
			String text = clean(value);
			if(text.indexOf("班") >= 0 && text.length() <= 80 && text.indexOf("排名") < 0){
				return text.replace("班级：", "").replace("班级:", "");
			}
		}
		return fallback;
	}

	private static boolean isLikelyStudent(CourseStudentImportItem item){
		return item != null && !isBlank(item.getName()) && !isBlank(item.getNo()) && isLikelyNo(item.getNo());
	}

	private static boolean isLikelyNo(String value){
		if(isBlank(value)){
			return false;
		}
		String text = value.trim();
		if(text.length() < 4 || text.length() > 50){
			return false;
		}
		return text.matches("[0-9A-Za-z_-]+") && text.matches(".*[0-9].*");
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

	private static String columnName(int index){
		return "列" + (index + 1);
	}

	private static String cell(ArrayList<Object> row, int index){
		if(row == null || index < 0 || index >= row.size() || row.get(index) == null){
			return "";
		}
		return row.get(index).toString().trim();
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

	private static boolean isBlank(String value){
		return value == null || value.trim().length() == 0;
	}

	private static String join(List<String> values){
		StringBuilder builder = new StringBuilder();
		for(String value : values){
			if(builder.length() > 0){
				builder.append(" ");
			}
			builder.append(value);
		}
		return builder.toString();
	}

	public static class ParseResult {
		public String source = SOURCE_LOCAL;
		public String message = "";
		public List<CourseStudentImportItem> items = new ArrayList<CourseStudentImportItem>();
	}

	public static interface StudentLookup {
		public boolean existsStudentNo(String no);
	}
}

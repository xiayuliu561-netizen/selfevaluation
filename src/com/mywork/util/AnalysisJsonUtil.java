package com.mywork.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.mywork.bean.AnalysisComponent;
import com.mywork.bean.AnalysisTarget;
import com.mywork.bean.AnalysisTargetItem;

public class AnalysisJsonUtil {
	private AnalysisJsonUtil(){
	}

	public static List<AnalysisComponent> parseComponents(JSONObject json){
		List<AnalysisComponent> list = new ArrayList<AnalysisComponent>();
		JSONArray array = getArray(json, "components");
		for(int i=0; i<array.size(); i++){
			JSONObject item = array.getJSONObject(i);
			String name = firstString(item, new String[]{"name","componentName","component_name","成绩组成","组成部分"});
			if(!isNotBlank(name)){
				continue;
			}
			AnalysisComponent component = new AnalysisComponent();
			component.setComponentName(normalizeName(name));
			component.setRate(parsePercent(firstString(item, new String[]{"rate","percent","percentage","占比","比例"})));
			component.setSortno(i+1);
			list.add(component);
		}
		return list;
	}

	public static List<AnalysisTarget> parseTargets(JSONObject json){
		List<AnalysisTarget> list = new ArrayList<AnalysisTarget>();
		JSONArray targets = getArray(json, "targets");
		for(int i=0; i<targets.size(); i++){
			JSONObject targetJson = targets.getJSONObject(i);
			String name = firstString(targetJson, new String[]{"name","targetName","target_name","课程目标","目标"});
			String content = cleanText(firstString(targetJson, new String[]{"content","targetContent","target_content","objective","目标内容","课程目标内容","课程目标正文","正文"}));
			if(!isNotBlank(name)){
				name = "课程目标" + (i+1);
			}
			int splitIndex = findTargetContentSplit(name);
			if(splitIndex > 0){
				String namePart = name.substring(0, splitIndex);
				String contentPart = name.substring(splitIndex + 1);
				if(!isNotBlank(content) && isNotBlank(contentPart)){
					content = cleanText(contentPart);
				}
				name = namePart;
			}
			AnalysisTarget target = new AnalysisTarget();
			target.setTargetName(normalizeName(name));
			target.setTargetContent(content);
			target.setTargetrate(parsePercent(firstString(targetJson, new String[]{"targetrate","targetRate","达成度标准","标准达成度"})));
			target.setSortno(i+1);
			List<AnalysisTargetItem> itemlist = new ArrayList<AnalysisTargetItem>();
			JSONArray items = getArray(targetJson, "items");
			if(items.size() == 0){
				items = getArray(targetJson, "methods");
			}
			for(int j=0; j<items.size(); j++){
				JSONObject itemJson = items.getJSONObject(j);
				String method = firstString(itemJson, new String[]{"method","methodName","method_name","name","考核方式"});
				if(!isNotBlank(method)){
					continue;
				}
				AnalysisTargetItem item = new AnalysisTargetItem();
				item.setMethodName(normalizeName(method));
				item.setWeightRate(parsePercent(firstString(itemJson, new String[]{"weight","weightRate","weight_rate","rate","占比","比例"})));
				item.setCoefficient(parsePercent(firstString(itemJson, new String[]{"coefficient","coef","系数"})));
				item.setSortno(j+1);
				itemlist.add(item);
			}
			target.setItemlist(itemlist);
			list.add(target);
		}
		return list;
	}

	public static JSONObject toJson(List<AnalysisComponent> components, List<AnalysisTarget> targets){
		JSONObject result = new JSONObject();
		JSONArray componentArray = new JSONArray();
		for(AnalysisComponent component : components){
			JSONObject item = new JSONObject();
			item.put("name", component.getComponentName());
			item.put("rate", component.getRate());
			componentArray.add(item);
		}
		JSONArray targetArray = new JSONArray();
		for(AnalysisTarget target : targets){
			JSONObject targetJson = new JSONObject();
			targetJson.put("name", target.getTargetName());
			targetJson.put("content", target.getTargetContent() == null ? "" : target.getTargetContent());
			targetJson.put("targetrate", target.getTargetrate());
			JSONArray itemArray = new JSONArray();
			if(target.getItemlist() != null){
				for(AnalysisTargetItem item : target.getItemlist()){
					JSONObject itemJson = new JSONObject();
					itemJson.put("method", item.getMethodName());
					itemJson.put("weight", item.getWeightRate());
					itemJson.put("coefficient", item.getCoefficient());
					itemArray.add(itemJson);
				}
			}
			targetJson.put("items", itemArray);
			targetArray.add(targetJson);
		}
		result.put("components", componentArray);
		result.put("targets", targetArray);
		return result;
	}

	public static JSONObject normalizeJson(String content){
		String jsonText = extractJson(content);
		JSONObject json = JSONObject.fromObject(jsonText);
		JSONObject result = new JSONObject();
		result.put("components", componentsToJson(parseComponents(json)));
		result.put("targets", targetsToJson(parseTargets(json)));
		return result;
	}

	private static JSONArray componentsToJson(List<AnalysisComponent> list){
		JSONArray array = new JSONArray();
		for(AnalysisComponent component : list){
			JSONObject item = new JSONObject();
			item.put("name", component.getComponentName());
			item.put("rate", component.getRate());
			array.add(item);
		}
		return array;
	}

	private static JSONArray targetsToJson(List<AnalysisTarget> list){
		JSONArray array = new JSONArray();
		for(AnalysisTarget target : list){
			JSONObject targetJson = new JSONObject();
			targetJson.put("name", target.getTargetName());
			targetJson.put("content", target.getTargetContent() == null ? "" : target.getTargetContent());
			targetJson.put("targetrate", target.getTargetrate());
			JSONArray itemArray = new JSONArray();
			if(target.getItemlist() != null){
				for(AnalysisTargetItem item : target.getItemlist()){
					JSONObject itemJson = new JSONObject();
					itemJson.put("method", item.getMethodName());
					itemJson.put("weight", item.getWeightRate());
					itemJson.put("coefficient", item.getCoefficient());
					itemArray.add(itemJson);
				}
			}
			targetJson.put("items", itemArray);
			array.add(targetJson);
		}
		return array;
	}

	public static String extractJson(String content){
		if(content == null){
			return "{}";
		}
		String text = content.trim();
		if(text.startsWith("```")){
			int firstLine = text.indexOf('\n');
			int lastFence = text.lastIndexOf("```");
			if(firstLine >= 0 && lastFence > firstLine){
				text = text.substring(firstLine + 1, lastFence).trim();
			}
		}
		int start = text.indexOf('{');
		int end = text.lastIndexOf('}');
		if(start >= 0 && end > start){
			return text.substring(start, end + 1);
		}
		return text;
	}

	private static JSONArray getArray(JSONObject json, String key){
		if(json != null && json.containsKey(key) && json.get(key) instanceof JSONArray){
			return json.getJSONArray(key);
		}
		return new JSONArray();
	}

	private static String firstString(JSONObject json, String[] keys){
		if(json == null){
			return "";
		}
		for(String key : keys){
			if(json.containsKey(key) && json.get(key) != null){
				String value = json.get(key).toString();
				if(isNotBlank(value) && !"null".equalsIgnoreCase(value)){
					return value;
				}
			}
		}
		return "";
	}

	private static int findTargetContentSplit(String value){
		if(value == null){
			return -1;
		}
		int cn = value.indexOf("：");
		int en = value.indexOf(":");
		int index = cn >= 0 ? cn : en;
		if(cn >= 0 && en >= 0){
			index = Math.min(cn, en);
		}
		if(index > 0 && value.substring(0, index).indexOf("课程目标") >= 0){
			return index;
		}
		return -1;
	}

	public static String cleanText(String value){
		if(value == null){
			return "";
		}
		String text = value.replace("\r", "\n").replace("\t", " ").replace("\f", " ");
		text = text.replaceAll(" *\\n+ *", " ");
		text = text.replaceAll("(?<=[\\u4e00-\\u9fa5，。、；：])\\s+(?=[\\u4e00-\\u9fa5])", "");
		text = text.replaceAll("(?<=[\\u4e00-\\u9fa5])\\s+(?=[\\u4e00-\\u9fa5])", "");
		text = text.replaceAll("\\s+", " ").trim();
		return text;
	}

	public static BigDecimal parsePercent(String value){
		if(value == null){
			return new BigDecimal("0");
		}
		String text = value.replace("%", "").replace("％", "").replace(",", "").trim();
		if(text.length() == 0 || "null".equalsIgnoreCase(text)){
			return new BigDecimal("0");
		}
		try{
			BigDecimal decimal = new BigDecimal(text);
			if(decimal.compareTo(BigDecimal.ONE) <= 0 && value.indexOf("%") < 0 && value.indexOf("％") < 0){
				decimal = decimal.multiply(new BigDecimal("100"));
			}
			return decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
		}catch(Exception e){
			return new BigDecimal("0");
		}
	}

	private static String normalizeName(String value){
		return value == null ? "" : value.replace("：", "").replace(":", "").trim();
	}

	private static boolean isNotBlank(String value){
		return value != null && value.trim().length() > 0;
	}
}

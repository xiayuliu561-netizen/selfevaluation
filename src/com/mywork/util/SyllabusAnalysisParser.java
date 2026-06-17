package com.mywork.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import com.mywork.bean.AnalysisComponent;
import com.mywork.bean.AnalysisTarget;
import com.mywork.bean.AnalysisTargetItem;

public class SyllabusAnalysisParser {
	private static final Pattern PERCENT_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[%％]");
	private static final Pattern TARGET_START_PATTERN = Pattern.compile("^\\s*(\\d+(?:\\.\\d+)*)\\s+.*");
	private static final String[] TARGET_LABELS = new String[]{"考核方式","占比","系数"};

	private SyllabusAnalysisParser(){
	}

	public static JSONObject parse(String text){
		String source = text == null ? "" : text.replace("\r", "\n");
		List<AnalysisTarget> contentTargets = parseTargetContents(source);
		List<AnalysisTarget> targets = parseTargetMatrix(source);
		List<AnalysisComponent> components = componentsFromTargets(targets);
		if(components.size() == 0){
			components = parseFormulaComponents(source);
		}
		if(targets.size() == 0){
			targets = parseTargetBlocks(source);
			if(components.size() == 0){
				components = componentsFromTargets(targets);
			}
		}
		mergeTargetContents(targets, contentTargets);
		if(targets.size() == 0 && contentTargets.size() > 0){
			targets = contentTargets;
		}
		return AnalysisJsonUtil.toJson(components, targets);
	}

	private static List<AnalysisTarget> parseTargetMatrix(String text){
		List<AnalysisTarget> targets = new ArrayList<AnalysisTarget>();
		String[] lines = text.split("\\n");
		int weightIndex = findLineIndex(lines, "各考核方式占总成绩权重");
		if(weightIndex < 0){
			return targets;
		}
		List<BigDecimal> weights = parsePercents(lines[weightIndex]);
		if(weights.size() == 0 && weightIndex + 1 < lines.length){
			weights = parsePercents(lines[weightIndex] + " " + lines[weightIndex + 1]);
		}
		if(weights.size() == 0){
			return targets;
		}
		List<String> methods = findMethodNames(lines, weightIndex, weights.size());
		if(methods.size() != weights.size()){
			return targets;
		}
		int startIndex = findStartBeforeWeights(lines, weightIndex);
		for(int i=startIndex; i<weightIndex; i++){
			Matcher matcher = TARGET_START_PATTERN.matcher(lines[i]);
			if(!matcher.matches() || lines[i].indexOf("合计") >= 0){
				continue;
			}
			int next = findNextTargetOrEnd(lines, i + 1, weightIndex);
			String block = joinLines(lines, i, next);
			List<BigDecimal> coefficients = parsePercents(block);
			if(coefficients.size() < methods.size()){
				continue;
			}
			AnalysisTarget target = new AnalysisTarget();
			target.setTargetName("课程目标" + matcher.group(1));
			target.setTargetrate(new BigDecimal("0"));
			target.setSortno(targets.size() + 1);
			target.setItemlist(buildItems(methods, weights, coefficients));
			targets.add(target);
			i = next - 1;
		}
		return targets;
	}

	private static int findStartBeforeWeights(String[] lines, int weightIndex){
		for(int i=weightIndex; i>=0; i--){
			if(lines[i].indexOf("课程目标在各考核方式中占比") >= 0 || lines[i].indexOf("考核内容、考核方式与课程目标对应关系") >= 0){
				return i;
			}
		}
		return Math.max(0, weightIndex - 80);
	}

	private static int findNextTargetOrEnd(String[] lines, int start, int end){
		for(int i=start; i<end; i++){
			if(lines[i].indexOf("合计") >= 0){
				return i;
			}
			Matcher matcher = TARGET_START_PATTERN.matcher(lines[i]);
			if(matcher.matches()){
				return i;
			}
		}
		return end;
	}

	private static List<String> findMethodNames(String[] lines, int weightIndex, int count){
		int headerIndex = -1;
		for(int i=Math.max(0, weightIndex - 80); i<weightIndex; i++){
			List<String> tokens = splitHeaderTokens(lines[i]);
			List<String> nextTokens = splitHeaderTokens(i + 1 < lines.length ? lines[i + 1] : "");
			if(tokens.size() == count && nextTokens.size() == count && (isLikelyMethodHeader(tokens) || isLikelyMethodHeader(nextTokens))){
				headerIndex = i;
				break;
			}
			if(headerIndex < 0 && tokens.size() == count && isLikelyMethodHeader(tokens)){
				headerIndex = i;
			}
		}
		if(headerIndex < 0){
			return new ArrayList<String>();
		}
		List<String> previous2 = splitHeaderTokens(headerIndex > 1 ? lines[headerIndex - 2] : "");
		List<String> previous = splitHeaderTokens(headerIndex > 0 ? lines[headerIndex - 1] : "");
		List<String> current = splitHeaderTokens(lines[headerIndex]);
		List<String> next = splitHeaderTokens(headerIndex + 1 < lines.length ? lines[headerIndex + 1] : "");
		List<String> afterNext = splitHeaderTokens(headerIndex + 2 < lines.length ? lines[headerIndex + 2] : "");
		List<String> methods = new ArrayList<String>();
		for(int i=0; i<count; i++){
			String name = current.get(i);
			if(next.size() == count){
				name += next.get(i);
			}
			methods.add(normalizeName(name));
		}
		appendAligned(methods, previous2, true);
		appendAligned(methods, previous, true);
		appendAligned(methods, afterNext, false);
		for(int i=0; i<methods.size(); i++){
			methods.set(i, normalizeName(methods.get(i)));
		}
		return methods;
	}

	private static boolean isLikelyMethodHeader(List<String> tokens){
		int hit = 0;
		for(String token : tokens){
			if("平时".equals(token) || "课程".equals(token) || "作业".equals(token)
				|| "阶段".equals(token) || "实验".equals(token) || "期末".equals(token)
				|| "机考".equals(token) || "考试".equals(token)){
				hit++;
			}
		}
		return hit >= 2;
	}

	private static void appendAligned(List<String> methods, List<String> tokens, boolean prefix){
		if(tokens == null || tokens.size() == 0 || tokens.size() > methods.size()){
			return;
		}
		int start = methods.size() - tokens.size();
		for(int i=0; i<tokens.size(); i++){
			String old = methods.get(start + i);
			methods.set(start + i, prefix ? tokens.get(i) + old : old + tokens.get(i));
		}
	}

	private static List<String> splitHeaderTokens(String line){
		List<String> tokens = new ArrayList<String>();
		if(line == null || line.indexOf("%") >= 0){
			return tokens;
		}
		String text = line.replace("（自行赋值）", " ").replace("(自行赋值)", " ");
		String[] array = text.trim().split("\\s+");
		for(String item : array){
			String token = item.replaceAll("[^0-9A-Za-z\\u4e00-\\u9fa5]", "");
			if(token.length() == 0 || token.indexOf("课程目标") >= 0 || token.indexOf("考核内容") >= 0
				|| token.indexOf("占比") >= 0 || token.indexOf("对应关系") >= 0){
				continue;
			}
			tokens.add(token);
		}
		return tokens;
	}

	private static List<AnalysisTarget> parseTargetBlocks(String text){
		List<AnalysisTarget> targets = new ArrayList<AnalysisTarget>();
		Pattern pattern = Pattern.compile("课程\\s*目标\\s*([0-9一二三四五六七八九十]+)");
		Matcher matcher = pattern.matcher(text);
		List<Integer> starts = new ArrayList<Integer>();
		List<String> names = new ArrayList<String>();
		while(matcher.find()){
			starts.add(Integer.valueOf(matcher.start()));
			names.add("课程目标" + matcher.group(1));
		}
		for(int i=0; i<starts.size(); i++){
			int start = starts.get(i).intValue();
			int end = i + 1 < starts.size() ? starts.get(i + 1).intValue() : text.length();
			String block = text.substring(start, end);
			String methodText = extractLabelValue(block, "考核方式");
			String weightText = extractLabelValue(block, "占比");
			String coefficientText = extractLabelValue(block, "系数");
			List<String> methods = splitMethods(methodText);
			List<BigDecimal> weights = parsePercents(weightText);
			List<BigDecimal> coefficients = parsePercents(coefficientText);
			if(methods.size() == 0 || weights.size() == 0 || coefficients.size() == 0){
				continue;
			}
			AnalysisTarget target = new AnalysisTarget();
			target.setTargetName(names.get(i));
			target.setTargetrate(new BigDecimal("0"));
			target.setSortno(targets.size() + 1);
			target.setItemlist(buildItems(methods, weights, coefficients));
			targets.add(target);
		}
		return targets;
	}

	private static List<AnalysisTarget> parseTargetContents(String text){
		List<AnalysisTarget> targets = parseTargetContentTable(text);
		if(targets.size() == 0){
			targets = parseLabeledTargetContents(text);
		}
		return targets;
	}

	private static List<AnalysisTarget> parseTargetContentTable(String text){
		List<AnalysisTarget> targets = new ArrayList<AnalysisTarget>();
		String[] lines = text.split("\\n");
		int start = findCourseTargetSectionStart(lines);
		if(start < 0){
			return targets;
		}
		int end = findCourseTargetSectionEnd(lines, start);
		Pattern rowPattern = Pattern.compile("^\\s*(\\d+)\\.?\\s+(.*)$");
		String currentName = "";
		StringBuilder currentContent = new StringBuilder();
		for(int i=start; i<end; i++){
			String line = cleanLine(lines[i]);
			if(line.length() == 0 || isTargetSectionHeader(line)){
				continue;
			}
			if(line.startsWith("注")){
				break;
			}
			Matcher matcher = rowPattern.matcher(line);
			if(matcher.matches() && isLikelyTargetRow(matcher.group(2))){
				addContentTarget(targets, currentName, currentContent.toString());
				currentName = "课程目标" + matcher.group(1);
				currentContent = new StringBuilder();
				appendObjectiveLine(currentContent, matcher.group(2));
				continue;
			}
			if(currentName.length() > 0){
				appendObjectiveLine(currentContent, line);
			}
		}
		addContentTarget(targets, currentName, currentContent.toString());
		return targets;
	}

	private static int findCourseTargetSectionStart(String[] lines){
		for(int i=0; i<lines.length; i++){
			String line = lines[i] == null ? "" : lines[i];
			if(line.indexOf("课程目标") >= 0 && line.indexOf("课程评价") < 0 && line.indexOf("对应关系") < 0
				&& (line.indexOf("二、") >= 0 || line.indexOf("课程目标（") >= 0 || line.indexOf("课程目标(") >= 0)){
				return i;
			}
		}
		return -1;
	}

	private static int findCourseTargetSectionEnd(String[] lines, int start){
		for(int i=start + 1; i<lines.length; i++){
			String line = lines[i] == null ? "" : lines[i].trim();
			if(line.startsWith("三、") || line.startsWith("四、") || line.startsWith("五、") || line.startsWith("六、")){
				return i;
			}
			if(line.startsWith("注") && i > start + 3){
				return i;
			}
		}
		return lines.length;
	}

	private static boolean isTargetSectionHeader(String line){
		return line.indexOf("序号") >= 0 || line.indexOf("达成途径") >= 0 || line.indexOf("支撑毕业要求") >= 0
			|| line.indexOf("课程目标（") >= 0 || line.indexOf("课程目标(") >= 0 || line.indexOf("课程定位") >= 0
			|| "二、课程目标".equals(line) || line.matches("^\\d+$");
	}

	private static boolean isLikelyTargetRow(String value){
		String text = value == null ? "" : value.trim();
		if(text.length() == 0){
			return false;
		}
		return text.matches("^\\d+(?:\\.\\d+)*\\s+.*") || text.matches("^[\\u4e00-\\u9fa5A-Za-z].*");
	}

	private static void appendObjectiveLine(StringBuilder builder, String line){
		String text = removeTargetCode(stripRightTableColumn(line));
		if(text.length() == 0 || isTeachingMethodText(text)){
			return;
		}
		if(builder.length() > 0){
			builder.append(' ');
		}
		builder.append(text);
	}

	private static String stripRightTableColumn(String line){
		String text = cleanLine(line);
		String[] methods = new String[]{"课堂教学","线上教学","实验教学","案例教学","言传身教","讲授","讨论","演示"};
		for(String method : methods){
			int index = text.indexOf(method);
			if(index >= 0){
				String before = text.substring(0, index);
				if(before.trim().length() == 0 || before.matches(".*\\s{2,}$")){
					text = before;
					break;
				}
			}
		}
		return text.trim();
	}

	private static boolean isTeachingMethodText(String text){
		return "课堂教学".equals(text) || "线上教学".equals(text) || "实验教学".equals(text)
			|| "案例教学".equals(text) || "言传身教".equals(text) || "讲授".equals(text)
			|| "讨论".equals(text) || "演示".equals(text);
	}

	private static String removeTargetCode(String value){
		if(value == null){
			return "";
		}
		return value.replaceFirst("^\\d+(?:\\.\\d+)*\\s*", "").trim();
	}

	private static void addContentTarget(List<AnalysisTarget> targets, String name, String content){
		String cleanContent = cleanObjectiveContent(content);
		if(name == null || name.length() == 0 || cleanContent.length() < 8 || containsPercentText(cleanContent)){
			return;
		}
		AnalysisTarget target = new AnalysisTarget();
		target.setTargetName(name);
		target.setTargetContent(cleanContent);
		target.setTargetrate(new BigDecimal("0"));
		target.setSortno(targets.size() + 1);
		target.setItemlist(new ArrayList<AnalysisTargetItem>());
		targets.add(target);
	}

	private static List<AnalysisTarget> parseLabeledTargetContents(String text){
		List<AnalysisTarget> targets = new ArrayList<AnalysisTarget>();
		Pattern pattern = Pattern.compile("课程\\s*目标\\s*([0-9一二三四五六七八九十]+)\\s*[：:]");
		Matcher matcher = pattern.matcher(text);
		List<Integer> starts = new ArrayList<Integer>();
		List<String> names = new ArrayList<String>();
		while(matcher.find()){
			starts.add(Integer.valueOf(matcher.end()));
			names.add("课程目标" + matcher.group(1));
		}
		for(int i=0; i<starts.size(); i++){
			int start = starts.get(i).intValue();
			int end = i + 1 < starts.size() ? starts.get(i + 1).intValue() : text.length();
			String content = text.substring(start, end);
			int nextSection = findFirstKeywordIndex(content, new String[]{"考核方式","占比","系数","总评成绩","总成绩","课程评价","评分标准"});
			if(nextSection >= 0){
				content = content.substring(0, nextSection);
			}
			addContentTarget(targets, names.get(i), content);
		}
		return targets;
	}

	private static int findFirstKeywordIndex(String text, String[] keywords){
		int result = -1;
		for(String keyword : keywords){
			int index = text.indexOf(keyword);
			if(index >= 0 && (result < 0 || index < result)){
				result = index;
			}
		}
		return result;
	}

	private static void mergeTargetContents(List<AnalysisTarget> targets, List<AnalysisTarget> contentTargets){
		if(targets == null || contentTargets == null || targets.size() == 0 || contentTargets.size() == 0){
			return;
		}
		for(int i=0; i<targets.size(); i++){
			AnalysisTarget target = targets.get(i);
			if(target.getTargetContent() != null && target.getTargetContent().trim().length() > 0){
				continue;
			}
			AnalysisTarget content = findContentTarget(target, contentTargets, i);
			if(content != null){
				target.setTargetContent(content.getTargetContent());
			}
		}
	}

	private static AnalysisTarget findContentTarget(AnalysisTarget target, List<AnalysisTarget> contentTargets, int index){
		String targetNo = firstNumber(target.getTargetName());
		for(AnalysisTarget content : contentTargets){
			String contentNo = firstNumber(content.getTargetName());
			if(targetNo.length() > 0 && contentNo.length() > 0 && targetNo.equals(contentNo)){
				return content;
			}
		}
		if(index < contentTargets.size()){
			return contentTargets.get(index);
		}
		return null;
	}

	private static String firstNumber(String value){
		if(value == null){
			return "";
		}
		Matcher matcher = Pattern.compile("(\\d+)").matcher(value);
		return matcher.find() ? matcher.group(1) : "";
	}

	private static boolean containsPercentText(String text){
		Matcher matcher = PERCENT_PATTERN.matcher(text == null ? "" : text);
		int count = 0;
		while(matcher.find()){
			count++;
		}
		return count >= 2 || text.indexOf("考核方式") >= 0 || text.indexOf("占比") >= 0 || text.indexOf("系数") >= 0;
	}

	private static String cleanObjectiveContent(String value){
		String text = AnalysisJsonUtil.cleanText(value);
		text = text.replaceFirst("^课程\\s*目标\\s*[0-9一二三四五六七八九十]+\\s*[：:]?", "");
		text = removeTargetCode(text);
		return text.trim();
	}

	private static String cleanLine(String value){
		if(value == null){
			return "";
		}
		return value.replace('\f', ' ').replace("\t", " ").replaceAll("\\s+$", "").trim();
	}

	private static String extractLabelValue(String block, String label){
		int start = block.indexOf(label);
		if(start < 0){
			return "";
		}
		start = start + label.length();
		while(start < block.length() && (block.charAt(start) == ':' || block.charAt(start) == '：' || Character.isWhitespace(block.charAt(start)))){
			start++;
		}
		int end = block.length();
		for(String other : TARGET_LABELS){
			if(other.equals(label)){
				continue;
			}
			int index = block.indexOf(other, start);
			if(index >= 0 && index < end){
				end = index;
			}
		}
		return block.substring(start, end).replace("\n", " ");
	}

	private static List<String> splitMethods(String text){
		List<String> methods = new ArrayList<String>();
		if(text == null){
			return methods;
		}
		String[] array = text.replace("、", "，").replace(",", "，").replace("；", "，").replace(";", "，").split("，");
		for(String item : array){
			String name = normalizeName(item);
			if(name.length() > 0){
				methods.add(name);
			}
		}
		if(methods.size() <= 1 && text.trim().indexOf(" ") >= 0){
			methods.clear();
			String[] spaceArray = text.trim().split("\\s+");
			for(String item : spaceArray){
				String name = normalizeName(item);
				if(name.length() > 0){
					methods.add(name);
				}
			}
		}
		return methods;
	}

	private static List<AnalysisComponent> parseFormulaComponents(String text){
		List<AnalysisComponent> components = new ArrayList<AnalysisComponent>();
		String section = text;
		int targetIndex = text.indexOf("课程目标");
		int totalIndex = Math.max(text.indexOf("总评成绩"), text.indexOf("总成绩"));
		if(totalIndex >= 0){
			int end = targetIndex > totalIndex ? targetIndex : Math.min(text.length(), totalIndex + 1000);
			section = text.substring(totalIndex, end);
		}
		Pattern pattern = Pattern.compile("([^+＋=＝\\n，,；;:：]{1,40}?)(?:\\*|×|x|X)?\\s*(\\d+(?:\\.\\d+)?)\\s*[%％]");
		Matcher matcher = pattern.matcher(section);
		while(matcher.find()){
			String name = normalizeName(matcher.group(1));
			if(name.length() == 0 || containsComponent(components, name)){
				continue;
			}
			AnalysisComponent component = new AnalysisComponent();
			component.setComponentName(name);
			component.setRate(AnalysisJsonUtil.parsePercent(matcher.group(2) + "%"));
			component.setSortno(components.size() + 1);
			components.add(component);
		}
		return components;
	}

	private static List<AnalysisComponent> componentsFromTargets(List<AnalysisTarget> targets){
		List<AnalysisComponent> components = new ArrayList<AnalysisComponent>();
		if(targets == null || targets.size() == 0 || targets.get(0).getItemlist() == null){
			return components;
		}
		for(AnalysisTargetItem item : targets.get(0).getItemlist()){
			if(item.getMethodName() == null || containsComponent(components, item.getMethodName())){
				continue;
			}
			AnalysisComponent component = new AnalysisComponent();
			component.setComponentName(item.getMethodName());
			component.setRate(item.getWeightRate());
			component.setSortno(components.size() + 1);
			components.add(component);
		}
		return components;
	}

	private static List<AnalysisTargetItem> buildItems(List<String> methods, List<BigDecimal> weights, List<BigDecimal> coefficients){
		List<AnalysisTargetItem> items = new ArrayList<AnalysisTargetItem>();
		int size = Math.min(methods.size(), Math.min(weights.size(), coefficients.size()));
		for(int i=0; i<size; i++){
			AnalysisTargetItem item = new AnalysisTargetItem();
			item.setMethodName(normalizeName(methods.get(i)));
			item.setWeightRate(weights.get(i));
			item.setCoefficient(coefficients.get(i));
			item.setSortno(i + 1);
			items.add(item);
		}
		return items;
	}

	private static List<BigDecimal> parsePercents(String text){
		List<BigDecimal> values = new ArrayList<BigDecimal>();
		Matcher matcher = PERCENT_PATTERN.matcher(text == null ? "" : text);
		while(matcher.find()){
			values.add(AnalysisJsonUtil.parsePercent(matcher.group()));
		}
		return values;
	}

	private static int findLineIndex(String[] lines, String keyword){
		for(int i=0; i<lines.length; i++){
			if(lines[i].indexOf(keyword) >= 0){
				return i;
			}
		}
		return -1;
	}

	private static String joinLines(String[] lines, int start, int end){
		StringBuilder builder = new StringBuilder();
		for(int i=start; i<end && i<lines.length; i++){
			builder.append(lines[i]).append('\n');
		}
		return builder.toString();
	}

	private static boolean containsComponent(List<AnalysisComponent> components, String name){
		for(AnalysisComponent component : components){
			if(component.getComponentName() != null && component.getComponentName().equals(name)){
				return true;
			}
		}
		return false;
	}

	private static String normalizeName(String value){
		if(value == null){
			return "";
		}
		String name = value.replace("：", "").replace(":", "").replace("，", "").replace(",", "").replace("；", "").replace(";", "").trim();
		name = name.replaceAll("\\s+", "");
		name = name.replace("总评成绩", "").replace("总成绩", "").replace("占总成绩权重", "").replace("自行赋值", "");
		while(name.startsWith("=") || name.startsWith("＝") || name.startsWith("+") || name.startsWith("＋")){
			name = name.substring(1).trim();
		}
		if(name.endsWith("成绩") && name.length() > 2){
			name = name.substring(0, name.length() - 2);
		}
		return name.trim();
	}
}

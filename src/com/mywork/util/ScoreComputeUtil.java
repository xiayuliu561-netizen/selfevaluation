package com.mywork.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

import com.mywork.bean.AnalysisComponent;
import com.mywork.bean.AnalysisTarget;
import com.mywork.bean.AnalysisTargetItem;

public class ScoreComputeUtil {
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	private ScoreComputeUtil(){
	}

	public static BigDecimal calculateTotalScore(java.util.List<AnalysisComponent> components, Map<String, BigDecimal> scoreMap){
		BigDecimal total = new BigDecimal("0");
		for(AnalysisComponent component : components){
			BigDecimal score = getScore(scoreMap, component.getComponentName());
			BigDecimal rate = nvl(component.getRate());
			total = total.add(score.multiply(rate).divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP));
		}
		return total.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public static BigDecimal calculateTargetFullScore(AnalysisTarget target){
		BigDecimal total = new BigDecimal("0");
		if(target == null || target.getItemlist() == null){
			return total;
		}
		for(AnalysisTargetItem item : target.getItemlist()){
			total = total.add(new BigDecimal("100").multiply(nvl(item.getWeightRate())).multiply(nvl(item.getCoefficient())).divide(new BigDecimal("10000"), 6, BigDecimal.ROUND_HALF_UP));
		}
		return total.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public static BigDecimal calculateTargetScore(AnalysisTarget target, Map<String, BigDecimal> scoreMap){
		BigDecimal total = new BigDecimal("0");
		if(target == null || target.getItemlist() == null){
			return total;
		}
		for(AnalysisTargetItem item : target.getItemlist()){
			BigDecimal score = getScore(scoreMap, item.getMethodName());
			total = total.add(score.multiply(nvl(item.getWeightRate())).multiply(nvl(item.getCoefficient())).divide(new BigDecimal("10000"), 6, BigDecimal.ROUND_HALF_UP));
		}
		return total.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public static BigDecimal calculateAchievement(AnalysisTarget target, Map<String, BigDecimal> scoreMap){
		BigDecimal fullScore = calculateTargetFullScore(target);
		if(fullScore.compareTo(BigDecimal.ZERO) == 0){
			return BigDecimal.ZERO;
		}
		return calculateTargetScore(target, scoreMap).divide(fullScore, 4, BigDecimal.ROUND_HALF_UP);
	}

	public static String format(BigDecimal decimal){
		if(decimal == null){
			return "0.00";
		}
		return DF.format(decimal);
	}

	public static BigDecimal getScore(Map<String, BigDecimal> scoreMap, String methodName){
		if(scoreMap == null || methodName == null){
			return BigDecimal.ZERO;
		}
		BigDecimal score = scoreMap.get(methodName);
		if(score != null){
			return score;
		}
		String normalized = normalize(methodName);
		for(String key : scoreMap.keySet()){
			String keyName = normalize(key);
			if(keyName.equals(normalized) || keyName.indexOf(normalized) >= 0 || normalized.indexOf(keyName) >= 0){
				return scoreMap.get(key);
			}
		}
		return BigDecimal.ZERO;
	}

	public static String normalize(String value){
		if(value == null){
			return "";
		}
		return value.replace("成绩", "").replace("考核", "").replace("方式", "").replace("：", "").replace(":", "").replace(" ", "").trim();
	}

	private static BigDecimal nvl(BigDecimal value){
		return value == null ? BigDecimal.ZERO : value;
	}
}

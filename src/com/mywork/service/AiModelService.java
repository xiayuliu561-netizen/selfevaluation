package com.mywork.service;

import java.util.List;
import java.util.Map;

import com.mywork.bean.AiModel;

public interface AiModelService {
	public List<AiModel> getList(Map<String, Object> map);
	public AiModel getById(String id);
	public boolean insert(AiModel aiModel);
	public boolean update(AiModel aiModel);
	public boolean clearDefault(Integer excludeId);
	public boolean clearEnabled(Integer excludeId);
	public boolean delete(String id);
	public AiModel getActiveModel();
	public String testCall(String id);
	public String callPrompt(String prompt);
	public String callPrompt(String prompt, int maxTokens, int readTimeoutMillis);
}

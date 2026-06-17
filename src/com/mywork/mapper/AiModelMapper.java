package com.mywork.mapper;

import java.util.List;
import java.util.Map;

import com.mywork.bean.AiModel;

public interface AiModelMapper extends SqlMapper{
	public List<AiModel> getList(Map<String, Object> map);
	public AiModel getById(String id);
	public void insert(AiModel aiModel);
	public void update(AiModel aiModel);
	public void clearDefault(Integer excludeId);
	public void clearEnabled(Integer excludeId);
	public void delete(String id);
	public AiModel getActiveModel();
}

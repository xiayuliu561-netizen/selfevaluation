package com.mywork.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mywork.bean.AiModel;
import com.mywork.mapper.AiModelMapper;
import com.mywork.service.AiModelService;

@Service("aiModelService")
public class AiModelServiceImpl implements AiModelService{
	@Autowired
	private AiModelMapper aiModelMapper;

	public List<AiModel> getList(Map<String, Object> map) {
		return aiModelMapper.getList(map);
	}

	public AiModel getById(String id) {
		return aiModelMapper.getById(id);
	}

	public boolean insert(AiModel aiModel) {
		aiModelMapper.insert(aiModel);
		return true;
	}

	public boolean update(AiModel aiModel) {
		aiModelMapper.update(aiModel);
		return true;
	}

	public boolean clearDefault(Integer excludeId) {
		aiModelMapper.clearDefault(excludeId);
		return true;
	}

	public boolean clearEnabled(Integer excludeId) {
		aiModelMapper.clearEnabled(excludeId);
		return true;
	}

	public boolean delete(String id) {
		aiModelMapper.delete(id);
		return true;
	}

	public AiModel getActiveModel() {
		return aiModelMapper.getActiveModel();
	}

	public String testCall(String id) {
		AiModel aiModel = null;
		if(isNotBlank(id)){
			aiModel = aiModelMapper.getById(id);
		}
		if(aiModel == null){
			aiModel = aiModelMapper.getActiveModel();
		}
		validateCallableModel(aiModel);

		try{
			String responseText;
			if(isAnthropic(aiModel.getProvider(), aiModel.getApiUrl())){
				responseText = callAnthropic(aiModel);
			}else{
				responseText = callOpenAiCompatible(aiModel);
			}
			String content = extractContent(responseText);
			if(isNotBlank(content)){
				return "调用成功：" + content;
			}
			return "调用成功：" + responseText;
		}catch(Exception e){
			return "调用失败：" + e.getMessage();
		}
	}

	public String callPrompt(String prompt) {
		return callPrompt(prompt, 4096, 90000);
	}

	public String callPrompt(String prompt, int maxTokens, int readTimeoutMillis) {
		AiModel aiModel = aiModelMapper.getActiveModel();
		validateCallableModel(aiModel);
		try{
			String responseText;
			if(isAnthropic(aiModel.getProvider(), aiModel.getApiUrl())){
				responseText = callAnthropic(aiModel, prompt, maxTokens, readTimeoutMillis);
			}else{
				responseText = callOpenAiCompatible(aiModel, prompt, maxTokens, readTimeoutMillis);
			}
			String content = extractContent(responseText);
			return isNotBlank(content) ? content : responseText;
		}catch(Exception e){
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private void validateCallableModel(AiModel aiModel) {
		if(aiModel == null){
			throw new IllegalArgumentException("未找到可调用的 AI 模型配置");
		}
		if(!isNotBlank(aiModel.getApiUrl())){
			throw new IllegalArgumentException("接口地址不能为空");
		}
		if(!isNotBlank(aiModel.getApiKey())){
			throw new IllegalArgumentException("API Key 不能为空");
		}
		if(!isNotBlank(aiModel.getModelName())){
			throw new IllegalArgumentException("模型名称不能为空");
		}
	}

	private String callOpenAiCompatible(AiModel aiModel) throws Exception {
		return callOpenAiCompatible(aiModel, "请仅回复：AI_TEST_OK", 64);
	}

	private String callOpenAiCompatible(AiModel aiModel, String prompt, int maxTokens) throws Exception {
		return callOpenAiCompatible(aiModel, prompt, maxTokens, 90000);
	}

	private String callOpenAiCompatible(AiModel aiModel, String prompt, int maxTokens, int readTimeoutMillis) throws Exception {
		JSONObject message = new JSONObject();
		message.put("role", "user");
		message.put("content", prompt);

		net.sf.json.JSONArray messages = new net.sf.json.JSONArray();
		messages.add(message);

		JSONObject payload = new JSONObject();
		payload.put("model", aiModel.getModelName());
		payload.put("messages", messages);
		payload.put("temperature", 0);
		payload.put("max_tokens", maxTokens);

		String apiUrl = buildUrl(aiModel.getApiUrl(), "chat/completions");
		return postJson(apiUrl, aiModel.getApiKey(), payload.toString(), false, readTimeoutMillis);
	}

	private String callAnthropic(AiModel aiModel) throws Exception {
		return callAnthropic(aiModel, "请仅回复：AI_TEST_OK", 64);
	}

	private String callAnthropic(AiModel aiModel, String prompt, int maxTokens) throws Exception {
		return callAnthropic(aiModel, prompt, maxTokens, 90000);
	}

	private String callAnthropic(AiModel aiModel, String prompt, int maxTokens, int readTimeoutMillis) throws Exception {
		JSONObject message = new JSONObject();
		message.put("role", "user");
		message.put("content", prompt);

		net.sf.json.JSONArray messages = new net.sf.json.JSONArray();
		messages.add(message);

		JSONObject payload = new JSONObject();
		payload.put("model", aiModel.getModelName());
		payload.put("messages", messages);
		payload.put("max_tokens", maxTokens);

		String apiUrl = buildUrl(aiModel.getApiUrl(), "messages");
		return postJson(apiUrl, aiModel.getApiKey(), payload.toString(), true, readTimeoutMillis);
	}

	private String postJson(String url, String apiKey, String payload, boolean anthropic) throws Exception {
		return postJson(url, apiKey, payload, anthropic, 90000);
	}

	private String postJson(String url, String apiKey, String payload, boolean anthropic, int readTimeoutMillis) throws Exception {
		HttpURLConnection connection = null;
		try{
			URL requestUrl = URI.create(url).toURL();
			connection = (HttpURLConnection)requestUrl.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(15000);
			connection.setReadTimeout(readTimeoutMillis > 0 ? readTimeoutMillis : 90000);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			connection.setRequestProperty("Accept", "application/json");
			if(anthropic){
				connection.setRequestProperty("x-api-key", apiKey);
				connection.setRequestProperty("anthropic-version", "2023-06-01");
			}else{
				connection.setRequestProperty("Authorization", "Bearer " + apiKey);
			}

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(payload.getBytes("UTF-8"));
			outputStream.flush();
			outputStream.close();

			int status = connection.getResponseCode();
			String responseText = readResponse(status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream());
			if(status < 200 || status >= 300){
				throw new IllegalStateException("HTTP " + status + "，" + responseText);
			}
			return responseText;
		}finally{
			if(connection != null){
				connection.disconnect();
			}
		}
	}

	private String readResponse(InputStream inputStream) throws Exception {
		if(inputStream == null){
			return "";
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
		StringBuilder builder = new StringBuilder();
		String line;
		while((line = reader.readLine()) != null){
			builder.append(line);
		}
		reader.close();
		return builder.toString();
	}

	private String extractContent(String responseText) {
		try{
			JSONObject json = JSONObject.fromObject(responseText);
			if(json.containsKey("choices") && json.getJSONArray("choices").size() > 0){
				JSONObject choice = json.getJSONArray("choices").getJSONObject(0);
				if(choice.containsKey("message")){
					JSONObject message = choice.getJSONObject("message");
					if(message.containsKey("content") && isNotBlank(message.getString("content"))){
						return message.getString("content");
					}
					if(message.containsKey("reasoning_content") && isNotBlank(message.getString("reasoning_content"))){
						return "模型已返回响应，但正文为空。reasoning_content：" + message.getString("reasoning_content");
					}
				}
				if(choice.containsKey("text")){
					return choice.getString("text");
				}
			}
			if(json.containsKey("content") && json.getJSONArray("content").size() > 0){
				JSONObject content = json.getJSONArray("content").getJSONObject(0);
				if(content.containsKey("text")){
					return content.getString("text");
				}
			}
		}catch(Exception e){
			return responseText;
		}
		return responseText;
	}

	private String buildUrl(String apiUrl, String suffix) {
		String url = apiUrl.trim();
		if(url.endsWith("/")){
			url = url.substring(0, url.length() - 1);
		}
		if(url.endsWith("/chat/completions") || url.endsWith("/messages")){
			return url;
		}
		return url + "/" + suffix;
	}

	private boolean isAnthropic(String provider, String apiUrl) {
		String source = ((provider == null ? "" : provider) + " " + (apiUrl == null ? "" : apiUrl)).toLowerCase();
		return source.indexOf("anthropic") >= 0 || source.indexOf("claude") >= 0;
	}

	private boolean isNotBlank(String value) {
		return value != null && value.trim().length() > 0;
	}
}

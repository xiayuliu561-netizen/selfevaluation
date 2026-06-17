package com.mywork.bean;

public class SystemStatistics {
	private Integer id;
	private String statKey;
	private Long statValue;
	private String description;
	private String createTime;
	private String updateTime;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getStatKey() {
		return statKey;
	}
	public void setStatKey(String statKey) {
		this.statKey = statKey;
	}
	public Long getStatValue() {
		return statValue;
	}
	public void setStatValue(Long statValue) {
		this.statValue = statValue;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
}

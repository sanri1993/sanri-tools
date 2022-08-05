package com.sanri.tools.modules.core.service.plugin.dtos;

import lombok.Data;

@Data
public class PluginCallInfo {

	/**
	 * 插件标识
	 */
	private String pluginId;
	/**
	 * 总共调用次数
	 */
	public int totalCalls;
	/**
	 * 上次调用时间
	 */
	public long lastCallTime;

	public PluginCallInfo(String pluginId, int totalCalls, long lastCallTime) {
		this.pluginId = pluginId;
		this.totalCalls = totalCalls;
		this.lastCallTime = lastCallTime;
	}
}
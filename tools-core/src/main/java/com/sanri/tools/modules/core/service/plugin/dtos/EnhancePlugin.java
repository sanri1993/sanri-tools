package com.sanri.tools.modules.core.service.plugin.dtos;

import com.sanri.tools.modules.core.dtos.PluginRegister;
import lombok.Data;

@Data
public class EnhancePlugin {
	/**
	 * 插件基础信息
	 */
	private PluginRegister pluginRegister;

	/**
	 * 插件调用信息
	 */
	public PluginCallInfo pluginCallInfo;

	public EnhancePlugin(PluginRegister pluginRegister, PluginCallInfo pluginCallInfo) {
		this.pluginRegister = pluginRegister;
		this.pluginCallInfo = pluginCallInfo;
	}

	public EnhancePlugin(PluginRegister pluginRegister) {
		this.pluginRegister = pluginRegister;
	}

}
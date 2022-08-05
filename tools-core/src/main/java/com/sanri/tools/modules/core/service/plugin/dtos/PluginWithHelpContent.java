package com.sanri.tools.modules.core.service.plugin.dtos;

import lombok.Data;

@Data
public class PluginWithHelpContent {
	private EnhancePlugin enhancePlugin;
	private String helpContent;

	public PluginWithHelpContent(EnhancePlugin enhancePlugin) {
		this.enhancePlugin = enhancePlugin;
	}
}
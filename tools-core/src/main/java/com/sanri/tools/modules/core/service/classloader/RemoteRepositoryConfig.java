package com.sanri.tools.modules.core.service.classloader;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 远程仓库配置
 */
@ConfigurationProperties(prefix = "maven.config")
@Getter
@Setter
@Component
public class RemoteRepositoryConfig {

	/**
	 * 远程仓库配置
	 */
	private List<RemoteRepository> repositories = new ArrayList<>();

	@Data
	public static final class RemoteRepository {
		private String id;
		private String type;
		private String url;
	}

}

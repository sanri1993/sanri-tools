package com.sanri.tools.modules.fastdfs.service;

import lombok.Data;

@Data
public class FastDfsConfig {
	private int connectTimeout;
	private int networkTimeout;
	private String charset;
	private String trackerServer;
	private Http http;

	@Data
	public static final class Http {
		private int trackerHttpPort;
		private String antiStealToken;
		private String securetKey;
	}
}
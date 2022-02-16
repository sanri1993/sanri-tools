package com.sanri.tools.modules.codepatch.controller.dtos;

import lombok.Data;

/**
 * 编译消息
 */
@Data
public class CompileMessage {
	private String group;
	private String repository;
	private String websocketId;
	private String relativePath;
	/**
	 * maven 命令, 默认是编译命令
	 */
	private String mvnCommand = "clean compile";
}
package com.sanri.tools.modules.core.service.localtask.dtos;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TaskInfo {
	private TaskState taskState;
	private long beginTime;
	private ProcessPercent processPercent;
	private Map<String,Object> extraDatas = new HashMap<>();

	@Data
	public static final class ProcessPercent {
		private int total;
		private int current;
	}

	public enum TaskState {
		RUNNING,
		PAUSE,
		FAIL
	}
}
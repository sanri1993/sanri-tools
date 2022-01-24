package com.sanri.tools.modules.jvm.service.dtos;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 线程池
 */
@Data
public class ThreadPoolInfo {
    /**
     * 线程池名称
     */
    private String name;
    /**
     * 线程列表
     */
    private List<ThreadInfo> children = new ArrayList<>();

    public ThreadPoolInfo() {
    }

    public ThreadPoolInfo(String name) {
        this.name = name;
    }

    @Data
    public static final class ThreadInfo implements Comparable<ThreadInfo> {
        /**
         * 线程 Id
         */
        private long id;
        /**
         * 线程名称
         */
        private String name;

        /**
         * 线程状态
         */
        private Thread.State threadState;

        public ThreadInfo() {
        }

        public ThreadInfo(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public ThreadInfo(long id, String name, Thread.State threadState) {
            this.id = id;
            this.name = name;
            this.threadState = threadState;
        }

        @Override
        public int compareTo(ThreadInfo o) {
            if (StringUtils.isBlank(o.name)){
                return -1;
            }
            return this.name.compareTo(o.name);
        }
    }
}

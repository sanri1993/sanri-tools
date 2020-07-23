package com.sanri.tools.modules.kafka.dtos;

public class MBeanMonitorInfo {
    private double fifteenMinute;
    private double fiveMinute;
    private double meanRate;
    private double oneMinute;
    private String mBean;

    public MBeanMonitorInfo() {
    }

    public MBeanMonitorInfo(String mBean, double fifteenMinute, double fiveMinute, double meanRate, double oneMinute) {
        this.fifteenMinute = fifteenMinute;
        this.fiveMinute = fiveMinute;
        this.meanRate = meanRate;
        this.oneMinute = oneMinute;
        this.mBean = mBean;
    }

    public double getFifteenMinute() {
        return fifteenMinute;
    }

    public double getFiveMinute() {
        return fiveMinute;
    }

    public double getMeanRate() {
        return meanRate;
    }

    public double getOneMinute() {
        return oneMinute;
    }

    public String getmBean() {
        return mBean;
    }

    /**
     * 合并多个 broker 的数据
     * @param mBeanInfo
     */
    public void addData(MBeanMonitorInfo mBeanInfo) {
        this.fifteenMinute += mBeanInfo.fifteenMinute;
        this.fiveMinute += mBeanInfo.fiveMinute;
        this.meanRate += mBeanInfo.meanRate;
        this.oneMinute += mBeanInfo.oneMinute;
    }
}

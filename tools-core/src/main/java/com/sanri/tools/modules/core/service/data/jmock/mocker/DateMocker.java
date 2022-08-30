package com.sanri.tools.modules.core.service.data.jmock.mocker;

import java.text.ParseException;
import java.util.Date;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.MockException;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import org.apache.commons.lang3.RandomUtils;

/**
 * Date对象模拟器
 */
public class DateMocker implements Mocker<Date> {
  protected Long startTime;
  protected Long endTime;
  @Override
  public Date mock(DataConfig mockConfig) {
//    try {
//      this.startTime = DateTool.getString2DateAuto(mockConfig.dateRange()[0]).getTime();
//      this.endTime = DateTool.getString2DateAuto(mockConfig.dateRange()[1]).getTime();
//    } catch (ParseException e) {
//      throw new MockException("不支持的日期格式，或者使用了错误的日期", e);
//    }
    return new Date(RandomUtils.nextLong(this.startTime,this.endTime));
  }

}

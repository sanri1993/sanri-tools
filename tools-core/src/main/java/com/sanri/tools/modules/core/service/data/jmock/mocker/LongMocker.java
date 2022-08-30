package com.sanri.tools.modules.core.service.data.jmock.mocker;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import com.sanri.tools.modules.core.service.data.randomstring.RandomStringGenerator;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * 模拟Long对象
 */
public class LongMocker implements Mocker<Long> {
  RandomStringGenerator generator = new RandomStringGenerator();
  @Override
  public Long mock(DataConfig mockConfig) {
    /**
     * 若根据正则模拟
     */
    if(StringUtils.isNotEmpty(mockConfig.numberRegex())){
      return new BigDecimal(generator.generateByRegex(mockConfig.numberRegex())).longValue();
    }
    return RandomUtils.nextLong(mockConfig.longRange()[0], mockConfig.longRange()[1]);
  }

}

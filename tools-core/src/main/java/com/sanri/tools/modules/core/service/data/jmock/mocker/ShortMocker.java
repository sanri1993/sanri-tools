package com.sanri.tools.modules.core.service.data.jmock.mocker;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import com.sanri.tools.modules.core.service.data.randomstring.RandomStringGenerator;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * 模拟Short对象
 */
public class ShortMocker implements Mocker<Short> {
  RandomStringGenerator generator = new RandomStringGenerator();
  @Override
  public Short mock(DataConfig mockConfig) {
    /**
     * 若根据正则模拟
     */
    if(StringUtils.isNotEmpty(mockConfig.numberRegex())){
      return new BigDecimal(generator.generateByRegex(mockConfig.numberRegex())).shortValue();
    }
    return (short) RandomUtils.nextInt(mockConfig.shortRange()[0], mockConfig.shortRange()[1]);
  }

}

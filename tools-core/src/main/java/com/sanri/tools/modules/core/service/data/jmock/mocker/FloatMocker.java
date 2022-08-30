package com.sanri.tools.modules.core.service.data.jmock.mocker;

import java.math.BigDecimal;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import com.sanri.tools.modules.core.service.data.randomstring.RandomStringGenerator;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Float对象模拟器
 */
public class FloatMocker implements Mocker<Float> {
  RandomStringGenerator generator = new RandomStringGenerator();
  @Override
  public Float mock(DataConfig mockConfig) {
    /**
     * 若根据正则模拟
     */
    if(StringUtils.isNotEmpty(mockConfig.numberRegex())){
      return new BigDecimal(generator.generateByRegex(mockConfig.numberRegex())).setScale(mockConfig.decimalScale(), BigDecimal.ROUND_FLOOR).floatValue();
    }
    return new BigDecimal(RandomUtils.nextFloat(mockConfig.floatRange()[0], mockConfig.floatRange()[1])).setScale(mockConfig.decimalScale(),BigDecimal.ROUND_FLOOR).floatValue();
  }
}

package com.sanri.tools.modules.core.service.data.jmock.mocker;

import java.math.BigDecimal;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;

/**
 * BigDecimal对象模拟器
 */
public class BigDecimalMocker implements Mocker<BigDecimal> {

  @Override
  public BigDecimal mock(DataConfig mockConfig) {
    return BigDecimal.valueOf(mockConfig.globalConfig().getMocker(Double.class).mock(mockConfig));
  }

}

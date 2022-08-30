package com.sanri.tools.modules.core.service.data.jmock.mocker;

import java.math.BigInteger;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;

/**
 * BigInteger对象模拟器
 */
public class BigIntegerMocker implements Mocker<BigInteger> {
  @Override
  public BigInteger mock(DataConfig mockConfig) {
   return BigInteger.valueOf(mockConfig.globalConfig().getMocker(Long.class).mock(mockConfig));
  }

}

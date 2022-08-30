package com.sanri.tools.modules.core.service.data.jmock.mocker;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import org.apache.commons.lang3.RandomUtils;

/**
 * Boolean对象模拟器
 */
public class BooleanMocker implements Mocker<Boolean> {

  @Override
  public Boolean mock(DataConfig mockConfig) {
    boolean[] booleanSeed = mockConfig.booleanSeed();
    return booleanSeed[RandomUtils.nextInt(0, booleanSeed.length)];
  }

}

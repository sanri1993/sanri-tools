package com.sanri.tools.modules.core.service.data.jmock.mocker;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import com.sanri.tools.modules.core.service.data.randomstring.RandomStringGenerator;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Integer对象模拟器
 */
public class IntegerMocker implements Mocker<Integer> {
  RandomStringGenerator generator = new RandomStringGenerator();
  @Override
  public Integer mock(DataConfig mockConfig) {
    /**
     * 若根据正则模拟
     */
    if(StringUtils.isNotEmpty(mockConfig.numberRegex())){
      return NumberUtils.toInt(generator.generateByRegex(mockConfig.numberRegex()),0);
    }
    return RandomUtils.nextInt(mockConfig.intRange()[0], mockConfig.intRange()[1]);
  }

}

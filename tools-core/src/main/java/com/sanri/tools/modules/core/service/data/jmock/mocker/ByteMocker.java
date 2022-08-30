package com.sanri.tools.modules.core.service.data.jmock.mocker;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import com.sanri.tools.modules.core.service.data.randomstring.RandomStringGenerator;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Byte对象模拟器
 */
public class ByteMocker implements Mocker<Byte> {
  RandomStringGenerator generator = new RandomStringGenerator();
  @Override
  public Byte mock(DataConfig mockConfig) {
    /**
     * 若根据正则模拟
     */
    if(StringUtils.isNotEmpty(mockConfig.numberRegex())){
      final String generateByRegex = generator.generateByRegex(mockConfig.numberRegex());
      return NumberUtils.toByte(generateByRegex, (byte) 0);
    }
    return (byte) RandomUtils.nextInt(mockConfig.byteRange()[0], mockConfig.byteRange()[1]);
  }

}

package com.sanri.tools.modules.core.service.data.jmock.mocker;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import com.sanri.tools.modules.core.service.data.randomstring.RandomStringGenerator;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 模拟String对象
 */
public class StringMocker implements Mocker<String> {
  RandomStringGenerator generator = new RandomStringGenerator();
  @Override
  public String mock(DataConfig mockConfig) {
    /**
     * 若根据正则模拟
     */
    if(StringUtils.isNotEmpty(mockConfig.stringRegex())){
      return generator.generateByRegex(mockConfig.stringRegex());
    }

    int size = RandomUtils.nextInt(mockConfig.sizeRange()[0], mockConfig.sizeRange()[1]);
    String[] stringSeed = mockConfig.stringSeed();
    StringBuilder sb = new StringBuilder(size);
    for (int i = 0; i < size; i++) {
      sb.append(stringSeed[RandomUtils.nextInt(0, stringSeed.length)]);
    }
    return sb.toString();
  }

}

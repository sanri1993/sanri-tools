package com.sanri.tools.modules.core.service.data.jmock.mocker;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import org.apache.commons.lang3.RandomUtils;

/**
 * Character对象模拟器
 */
public class CharacterMocker implements Mocker<Character> {

  @Override
  public Character mock(DataConfig mockConfig) {
    char[] charSeed = mockConfig.charSeed();
    return charSeed[RandomUtils.nextInt(0, charSeed.length)];
  }

}

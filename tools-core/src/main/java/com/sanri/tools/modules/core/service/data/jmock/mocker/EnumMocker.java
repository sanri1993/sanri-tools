package com.sanri.tools.modules.core.service.data.jmock.mocker;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.MockException;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import org.apache.commons.lang3.RandomUtils;

/**
 * Enum对象模拟器
 */
public class EnumMocker<T extends Enum> implements Mocker<Object> {

  private Class<?> clazz;

  public EnumMocker(Class<?> clazz) {
    this.clazz = clazz;
  }

  @Override
  public T mock(DataConfig mockConfig) {

    Enum[] enums = mockConfig.globalConfig().getcacheEnum(clazz.getName());
    if (enums == null) {
      //  Field field = clazz.getDeclaredField("$VALUES");
       // field.setAccessible(true);
        enums =(Enum[]) clazz.getEnumConstants();
        if (enums.length == 0) {
          throw new MockException("空的enum不能模拟");
        }
        mockConfig.globalConfig().cacheEnum(clazz.getName(), enums);
    }
    return (T) enums[RandomUtils.nextInt(0, enums.length)];
  }

}

package com.sanri.tools.modules.core.service.data.jmock.mocker;

import java.lang.reflect.ParameterizedType;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;

/**
 * 模拟泛型
 */
public class GenericMocker implements Mocker<Object> {

  private ParameterizedType type;

  GenericMocker(ParameterizedType type) {
    this.type = type;
  }

  @Override
  public Object mock(DataConfig mockConfig) {
    return new BaseMocker(type.getRawType(), type.getActualTypeArguments()).mock(mockConfig);
  }

}

package com.sanri.tools.modules.core.service.data.jmock.mocker;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import org.apache.commons.lang3.RandomUtils;

/**
 * 模拟Map
 */
public class MapMocker implements Mocker<Object> {

  private Type[] types;

  MapMocker(Type[] types) {
    this.types = types;
  }

  @Override
  public Object mock(DataConfig mockConfig) {
    int size = RandomUtils.nextInt(mockConfig.sizeRange()[0], mockConfig.sizeRange()[1]);
    Map<Object, Object> result = new HashMap<>(size);
    BaseMocker keyMocker = new BaseMocker(types[0]);
    BaseMocker valueMocker = new BaseMocker(types[1]);
    for (int index = 0; index < size; index++) {
      result.put(keyMocker.mock(mockConfig), valueMocker.mock(mockConfig));
    }
    return result;
  }

}

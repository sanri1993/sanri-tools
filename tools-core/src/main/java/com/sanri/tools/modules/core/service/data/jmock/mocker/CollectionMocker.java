package com.sanri.tools.modules.core.service.data.jmock.mocker;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import org.apache.commons.lang3.RandomUtils;

/**
 * 模拟Collection
 */
public class CollectionMocker implements Mocker<Object> {

  private Class clazz;

  private Type genericType;

  CollectionMocker(Class clazz, Type genericType) {
    this.clazz = clazz;
    this.genericType = genericType;
  }

  @Override
  public Object mock(DataConfig mockConfig) {
    int size = RandomUtils.nextInt(mockConfig.sizeRange()[0], mockConfig.sizeRange()[1]);
    Collection<Object> result;
    if (List.class.isAssignableFrom(clazz)) {
      result = new ArrayList<>(size);
    } else {
      result = new HashSet<>(size);
    }
    BaseMocker baseMocker = new BaseMocker(genericType);
    for (int index = 0; index < size; index++) {
      result.add(baseMocker.mock(mockConfig));
    }
    return result;
  }

}

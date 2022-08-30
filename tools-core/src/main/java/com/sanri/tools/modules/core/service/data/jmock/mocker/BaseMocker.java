package com.sanri.tools.modules.core.service.data.jmock.mocker;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;

@SuppressWarnings("unchecked")
public class BaseMocker<T> implements Mocker<T> {

  private Type type;

  private Type[] genericTypes;

  public BaseMocker(Type type, Type... genericTypes) {
    this.type = type;
    this.genericTypes = genericTypes;
  }

  @Override
  public T mock(DataConfig mockConfig) {
    Mocker mocker;
    if (type instanceof ParameterizedType) {
      mocker = new GenericMocker((ParameterizedType) type);
    } else if (type instanceof GenericArrayType) {
      mocker = new ArrayMocker(type);
    } else if (type instanceof TypeVariable) {
      mocker = new BaseMocker(mockConfig.globalConfig().getVariableType(((TypeVariable) type).getName()));
    } else {
      mocker = new ClassMocker((Class) type, genericTypes);
    }
    return (T) mocker.mock(mockConfig);
  }
}

package com.sanri.tools.modules.core.service.data.jmock.mocker;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sanri.tools.modules.core.service.data.jmock.DataConfig;
import com.sanri.tools.modules.core.service.data.jmock.MockException;
import com.sanri.tools.modules.core.service.data.jmock.Mocker;
import com.sanri.tools.modules.core.service.data.jmock.annotation.MockIgnore;
import org.springframework.util.ReflectionUtils;

public class BeanMocker implements Mocker<Object> {

	private final Class clazz;

	BeanMocker(Class clazz) {
		this.clazz = clazz;
	}

	@Override
	public Object mock(DataConfig mockConfig) {

		try {
			// fixme 解决方案不够优雅
			if (mockConfig.globalConfig().isEnabledCircle()) {
				Object cacheBean = mockConfig.globalConfig().getcacheBean(clazz.getName());
				if (cacheBean != null) {
					return cacheBean;
				}
			}
			Object result = clazz.newInstance();
			mockConfig.globalConfig().cacheBean(clazz.getName(), result);
			/**
			 * 是否配置排除整个类
			 */

			if (mockConfig.globalConfig().isConfigExcludeMock(clazz)) {
				return result;
			}
			for (Class<?> currentClass = clazz; currentClass != Object.class; currentClass = currentClass
					.getSuperclass()) {
				// 模拟有setter方法的字段
				for (Entry<Field, Method> entry : fieldAndSetterMethod(currentClass).entrySet()) {
					Field field = entry.getKey();
					if (field.isAnnotationPresent(MockIgnore.class)) {
						continue;
					}
					/**
					 * 是否配置排除这个属性
					 */
					if (mockConfig.globalConfig().isConfigExcludeMock(clazz, field.getName())) {
						continue;
					}
					entry.getValue().invoke(result, new BaseMocker(field.getGenericType())
							.mock(mockConfig.globalConfig().getDataConfig(currentClass, field.getName())));
				}
			}
			return result;
		} catch (Exception e) {
			throw new MockException(e);
		}
	}

	/**
	 * 有setter方法的字段及其setter方法
	 *
	 * @param clazz Class对象
	 * @return 有setter方法的 字段及其setter方法
	 * @throws IntrospectionException 内省异常
	 */
	public static Map<Field, Method> fieldAndSetterMethod(Class clazz) throws IntrospectionException {
		Map<Field, Method> map = new LinkedHashMap<>();
		BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (Field field : clazz.getDeclaredFields()) {
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				if (propertyDescriptor.getName().equals(field.getName())
						&& propertyDescriptor.getWriteMethod() != null) {
					map.put(field, propertyDescriptor.getWriteMethod());
				}
			}
		}
		return map;
	}

}

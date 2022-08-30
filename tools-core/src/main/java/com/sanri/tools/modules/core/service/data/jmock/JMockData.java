package com.sanri.tools.modules.core.service.data.jmock;

import com.sanri.tools.modules.core.service.data.jmock.mocker.BaseMocker;

import java.lang.reflect.Type;

/**
 * 模拟对象门面类
 *
 * @author jsonzou, kanyuxia, TaoYu
 */
@SuppressWarnings("unchecked")
public class JMockData {

	/**
	 * 模拟数据
	 *
	 * @param clazz 模拟数据类型
	 * @return 模拟数据对象
	 */
	public static <T> T mock(Class<T> clazz) {
		return mock(clazz, new MockConfig());
	}

	/**
	 * 模拟数据
	 *
	 * @param clazz 模拟数据类型
	 * @param mockConfig 模拟数据配置
	 * @return 模拟数据对象
	 */
	public static <T> T mock(Class<T> clazz, MockConfig mockConfig) {
		DataConfig config = mockConfig.getDataConfig(clazz, null);
		return new BaseMocker<T>(clazz).mock(config);
	}

	/**
	 * 模拟数据
	 * <pre>
	 * 注意typeReference必须以{}结尾
	 * </pre>
	 *
	 * @param typeReference 模拟数据包装类型
	 * @return 模拟数据对象
	 */
	public static <T> T mock(TypeReference<T> typeReference) {
		return mock(typeReference, new MockConfig());
	}

	/**
	 * 模拟数据
	 * <pre>
	 * 注意typeReference必须以{}结尾
	 * </pre>
	 *
	 * @param typeReference 模拟数据类型
	 * @param mockConfig 模拟数据配置
	 * @return 模拟数据对象
	 */
	public static <T> T mock(TypeReference<T> typeReference, MockConfig mockConfig) {
		mockConfig.init(typeReference.getType());
		DataConfig config = mockConfig.globalDataConfig();
		return new BaseMocker<T>(typeReference.getType()).mock(config);
	}

	/**
	 * 模拟类型对象
	 * @param type
	 * @param mockConfig
	 * @return
	 */
	public static Object mock(Type type, MockConfig mockConfig){
		mockConfig.init(type);
		DataConfig config = mockConfig.globalDataConfig();
		return new BaseMocker(type).mock(config);
	}
}

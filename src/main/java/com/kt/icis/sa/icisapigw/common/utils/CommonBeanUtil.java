package com.kt.icis.sa.icisapigw.common.utils;

import com.kt.icis.sa.icisapigw.common.context.ApplicationContextProvider;

public class CommonBeanUtil {

	public static Object getBean(String beanName) {
		return ApplicationContextProvider.getContext().getBean(beanName);
	}
	public static <T> Object getBean(Class<T> clz) {
		return ApplicationContextProvider.getContext().getBean(clz);
	}
}

package com.beidao.netty.dubbo.client;

import java.lang.reflect.Proxy;

/**
 * Dubbo代理类
 * @author 0200759
 *
 */
public class DubboProxy {

	public static Object getProxyInstance(Class<?> clazz) {
		return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new DubboConsumerHandler());
	}
}

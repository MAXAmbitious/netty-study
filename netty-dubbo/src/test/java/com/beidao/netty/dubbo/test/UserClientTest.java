package com.beidao.netty.dubbo.test;

import com.beidao.netty.dubbo.client.DubboProxy;
import com.beidao.netty.dubbo.facade.api.IUserFacade;

import junit.framework.TestCase;

/**
 * 测试用户远程调用
 * @author beidao
 *
 */
public class UserClientTest extends TestCase{
	
	IUserFacade userFacade = (IUserFacade) DubboProxy.getProxyInstance(IUserFacade.class);

	/**
	 * 获取用户
	 */
	public void getUser() {
		userFacade.getUserName(1L);
	}
}

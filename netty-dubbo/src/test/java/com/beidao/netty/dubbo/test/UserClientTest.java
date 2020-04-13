package com.beidao.netty.dubbo.test;

import com.alibaba.fastjson.JSON;
import com.beidao.netty.dubbo.client.DubboProxy;
import com.beidao.netty.dubbo.facade.api.IUserFacade;
import com.beidao.netty.dubbo.facade.api.UserDTO;

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
		UserDTO userDTO = userFacade.getUserDTO(1L);
		System.out.println(JSON.toJSONString(userDTO));
	}
	
	/**
	 * 获取用户名称
	 */
	public void getUserName() {
		String name = userFacade.getUserName(1L);
		System.out.println(JSON.toJSONString(name));
	}
}

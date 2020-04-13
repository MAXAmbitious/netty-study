package com.beidao.netty.dubbo.client;

import com.beidao.netty.dubbo.facade.api.IUserFacade;

/**
 * dubbo客户端(消费者)
 * @author beidao
 *
 */
public class DubboClient {
 
	public static void main(String[] args){
		IUserFacade userFacade = (IUserFacade) DubboProxy.getProxyInstance(IUserFacade.class);
		
		System.out.println(userFacade.getUserName(520L));
		System.out.println(userFacade.getUserName(1314L));
		System.out.println(userFacade.getUserName(1314520L));
	}
}

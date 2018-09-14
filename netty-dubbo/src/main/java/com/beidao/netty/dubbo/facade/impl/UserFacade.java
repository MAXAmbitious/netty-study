package com.beidao.netty.dubbo.facade.impl;

import com.beidao.netty.dubbo.facade.api.IUserFacade;

/**
 * dubbo服务端实现类
 * @author 0200759
 *
 */
public class UserFacade implements IUserFacade {

	public String getUserName(Long id) {
		
		return "I love you, "+id;
	}

}

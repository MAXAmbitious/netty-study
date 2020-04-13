package com.beidao.netty.dubbo.facade.impl;

import java.util.Date;

import com.beidao.netty.dubbo.facade.api.IUserFacade;
import com.beidao.netty.dubbo.facade.api.UserDTO;

/**
 * dubbo服务端实现类
 * 
 * @author beidao
 *
 */
public class UserFacade implements IUserFacade {

	public String getUserName(Long id) {

		return "I love you, " + id;
	}

	public UserDTO getUserDTO(Long id) {
		UserDTO userDTO = new UserDTO();
		userDTO.setId(id);
		userDTO.setName("北国的候鸟");
		userDTO.setCreatedDate(new Date());
		return userDTO;
	}

}

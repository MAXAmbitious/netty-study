package com.beidao.netty.dubbo.facade.api;

/**
 * dubbo api接口
 * @author 0200759
 *
 */
public interface IUserFacade {

	/**
	 * 返回用户名接口
	 * @param string 
	 * @return
	 */
	public String getUserName(Long id);

}

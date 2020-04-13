package com.beidao.netty.dubbo.facade.api;

/**
 * dubbo api接口
 * @author beidao
 *
 */
public interface IUserFacade {

	/**
	 * 根据id查询用户名称
	 * @param string 
	 * @return
	 */
	public String getUserName(Long id);

	/**
	 * 根据id查询用户信息
	 * @param id
	 * @return
	 */
	public UserDTO getUserDTO(Long id);

}

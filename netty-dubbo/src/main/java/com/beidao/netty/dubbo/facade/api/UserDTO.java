package com.beidao.netty.dubbo.facade.api;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生实体类
 * @author beidao
 *
 */
public class UserDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3837463104209285025L;

	/**
	 * 主键id
	 */
	private Long id;
	
	/**
	 * 学生姓名
	 */
	private String name;
	
	/**
	 * 创建日期
	 */
	private Date createdDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	

}

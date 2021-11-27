package com.techelevator.tenmo.model;

public class User {

	private Integer userId;
	private String username;

	public Integer getId() {
		return userId;
	}
	
	public void setId(Integer id) {
		this.userId = id;
	}

	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
}

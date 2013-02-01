package com.inforstack.openstack.controller.model;

public class CartItemModel {

	private String name;
	
	private Integer itemSpecificationId;
	
	private Integer number;
	
	private Float price;
	
	private Integer status; 
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getItemSpecificationId() {
		return itemSpecificationId;
	}

	public void setItemSpecificationId(Integer itemSpecificationId) {
		this.itemSpecificationId = itemSpecificationId;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
}

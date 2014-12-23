package com.bin.hibernate.n21.both;

import java.util.HashSet;
import java.util.Set;

public class Customer {
	
	private Integer customerId ;
	private String customerName ;
	/**
	 * 两点注意:
	 * 1. 需要把集合进行初始化,可以防止发生空指针异常.
	 * 2. 集合的定义类型必须是接口类型(Set,List等),不能是HashSet,否则类造型异常.因为返回的是Hibernate内置的集合类型.
	 * 3. 
	 * 
	 */
	private Set<Order> orders = new HashSet<Order>() ;
	
	public Set<Order> getOrders() {
		return orders;
	}
	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}
	public Integer getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	
	
	

}

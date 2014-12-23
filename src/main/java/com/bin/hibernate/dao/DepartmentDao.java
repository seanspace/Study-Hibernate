package com.bin.hibernate.dao;

import org.hibernate.Session;

import com.bin.hibernate.hql.entities.Department;
import com.bin.hibernate.utils.HibernateUtils;

/**
 * Session的管理.
 */
public class DepartmentDao {
	
	/**
	 * 1. 普通方式
	 * 若需要传入一个Session对象,则意味着上一层(Service)需要获取Session对象.
	 * 这说明上一层需要和Hibernate的API紧密耦合.所以不推荐使用此种方式.
	 */
	public void save(Session session,Department dept){
		
	}
	
	/**
	 * 2.与本地化线程绑定.使用Thread方式来管理.内部获取Session对象.获取和当前线程绑定的session对象.
	 * 	 好处:
	 * 1. 不需要从外部传入Session
	 * 2. 多个Dao方法也可以使用一个事务.
	 */
	public void save(Department dept){
		Session session = HibernateUtils.getInstance().getSession() ;
		System.out.println(session.hashCode());
		
		session.save(dept) ;
	}

}

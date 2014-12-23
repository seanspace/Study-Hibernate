package com.bin.hibernate.helloworld;

import java.sql.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.Test;


public class HibernateTest {

	@Test
	public void test() {
		// 1. 创建一个SessinonFactory对象.
		SessionFactory sessionFactory = null ;
		//  1).创建Configuration对象:对应hibernate的基本配置信息和对象的关系映射信息.
		Configuration configuration = new Configuration().configure() ;
		// 		4.0以前这样创建.
		//sessionFactory = configuration.buildSessionFactory() ;
		// 	2).创建一个ServiceRegistry对象:hibernate 4.x新添加的对象.hibernate的任何配置和服务都需要在该对象注册后才能有效.
		// 童刚的版本没过期,我的版本这个类已经过期了.
//		ServiceRegistry serviceRegistry = 
//				new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry() ;
		// 没过期.===>applySettings方法一定要使用,不然配置信息失效
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
		.applySettings(configuration.getProperties()).build() ;
		
		//  3). 
		sessionFactory = configuration.buildSessionFactory(serviceRegistry) ;
		// 2. 创建一个Session对象
		Session session = sessionFactory.openSession() ;
		// 3. 开启事务,所有操作都添加上.没有开事务是执行不了数据操作的.
		Transaction transaction = session.beginTransaction() ;
		// 4. 执行事务
		News news = new News("Java","ATguigu",new Date(new java.util.Date().getTime())) ;
		session.save(news) ;
		// 5. 提交事务
		transaction.commit();
		// 6. 关闭session
		session.close() ;
		// 7.关闭SessionFactory对象
		sessionFactory.close();
	}
	


}

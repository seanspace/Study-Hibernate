package com.bin.hibernate.n2n;

import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HibernateTest {
	
	private SessionFactory sessionFactory ;
	// 实际开发时,以下两个成员变量是不允许这样的,有并发的问题.
	private Session session ;
	private Transaction transaction ;

	
	@Before
	public void init(){
		Configuration configuration = new Configuration().configure() ;
		StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build() ;
		sessionFactory = configuration.buildSessionFactory(serviceRegistry) ;
		session = sessionFactory.openSession() ;
		transaction = session.beginTransaction() ;
	}
	
	@After
	public void destroy(){
		// commit前会先执行flush()再提交.
		transaction.commit();
		session.close() ;
		sessionFactory.close();
	}
	
	@Test
	public void testSave(){
		Category category1 = new Category() ;
		category1.setName("C-AA");
		Category category2 = new Category() ;
		category1.setName("C-BB");
		
		Item item1 = new Item() ;
		item1.setName("I-AA");
		Item item2 = new Item() ;
		item1.setName("I-BB");
		
		category1.getItems().add(item1) ;
		category1.getItems().add(item2) ;
		
		category2.getItems().add(item1) ;
		category2.getItems().add(item2) ;
		
		item1.getCategories().add(category1) ;
		item1.getCategories().add(category2) ;
		
		item2.getCategories().add(category1) ;
		item2.getCategories().add(category2) ;
		
		session.save(category1) ;
		session.save(category2) ;
		
		session.save(item1) ;
		session.save(item2) ;
	}
	
	@Test
	public void testGet(){
		// 支持懒加载
		Category category = (Category) session.get(Category.class, 1) ;
		System.out.println(category);
		
		// 这里执行了,中间表和items表的内连接.
		Set<Item> items = category.getItems() ;
		System.out.println(items.size());
	}
	
	
}

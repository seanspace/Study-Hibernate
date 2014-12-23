package com.bin.hibernate.subclass;

import java.util.List;

import org.hibernate.Query;
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
	
	/**
	 * susclass缺点:
	 * 1.使用了辨别者列.
	 * 2.子类独有的字段不能添加非空约束.
	 * 3.若继承层次较深,则数据表的字段也比较多.
	 */
	
	
	/**
	 * 插入操作:
	 * 1. 对于子类对象只需要把记录插入到一张表中
	 * 2. 辨别者列有Hibernate自动维护.
	 */
	@Test
	public void testSave(){
		Person person = new Person() ;
		person.setAge(11);
		person.setName("AA");
		
		session.save(person) ;
		
		Student student = new Student() ;
		student.setAge(11);
		student.setName("BB"); 
		student.setSchool("清华xiaoxue");
		session.save(student) ;
		
	}
	

	
	/**
	 * 查询:
	 * 1. 查询父类记录,只需要查询一张数据表.
	 * 2. 对于子类记录,也只要查询一张表.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testQuery(){
		Query query = (Query) session.createQuery("From Person") ;
		List<Person> persons = query.list() ;
		
		System.out.println(persons.size());
		
		List<Student> students = (List<Student>) session.createQuery("From Student").list() ;
		System.out.println(students.size());
		
	}
	
	
	
	
}

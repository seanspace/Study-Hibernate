package com.bin.hibernate.joined.subclass;

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
	 * 插入操作:
	 * 1. 插入子类,需要插入到至少两张表中.
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
	 * 优点:
	 * 1. 不需要使用辨别者,
	 * 2. 子类独有的字段,
	 * 3. 没有冗余.
	 */
	
	/**
	 * 1.查询父类记录,做一个左外连接.
	 * 2.对于子类记录,做一个内连接查询.
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

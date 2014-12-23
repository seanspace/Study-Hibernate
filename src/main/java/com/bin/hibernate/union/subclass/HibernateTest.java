package com.bin.hibernate.union.subclass;

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
	 * unionsubclass优点:
	 * 1. 无需使用辨别者列.
	 * 2. 子类独有的字段能添加非空约束.
	 * 
	 * 缺点:
	 * 1. 存在冗余的字段.
	 * 2. 更新父表的字段效率较低.
	 */
	
	
	
	@Test
	public void testUpdate(){
		String hql = "update Person p set p.age = 20" ;
		session.createQuery(hql).executeUpdate() ;
	}
	
	/**
	 * 
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
// 查询父类:
//	select
//    person0_.ID as ID1_0_,
//    person0_.NAME as NAME2_0_,
//    person0_.AGE as AGE3_0_,
//    person0_.school as school1_1_,
//    person0_.clazz_ as clazz_ 
//from
//    ( select
//        ID,
//        NAME,
//        AGE,
//        null as school,
//        0 as clazz_ 
//    from
//        PERSONS 
//    union
//    select
//        ID,
//        NAME,
//        AGE,
//        school,
//        1 as clazz_ 
//    from
//        Student 
//) person0_
	
	
	
	/**
	 * unionsubclass查询:
	 * 1. 查询父类:如上sql,查询父类,把两个表中的数据都查询,然后union在一起.性能稍差.
	 * 2. 查询子类:只查一张表.
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

package com.bin.hibernate.one2one.primary;

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
		Department department = new Department() ;
		department.setDeptName("DEPT-CC");
		
		Manager manager = new Manager() ;
		manager.setMgrName("Mana-CC");
		
		// 设定关联关系
		department.setMgr(manager);
		manager.setDepartment(department);
		
		// 先插入哪个都不会有多余的update语句.
		session.save(manager) ;
		session.save(department) ;
	}
	
	@Test
	public void testGet(){
		// 默认懒加载,查询department是会
		Department department = (Department) session.get(Department.class, 1) ;
		System.out.println(department.getDeptName());
		
		/**
		 * 也是有左外连接.
		 */
		Manager mgr = department.getMgr();
		System.out.println(mgr.getMgrName());// on manager0_.MGR_ID=department1_.DEPT_ID 连接条件是有问题的.
	}
	
	/**
	 * 在查询没有外键的实体对象时,使用的左外链接查询,一并查询出其关联对象..
	 * 并已经进行初始化.
	 */
	@Test
	public void testGet2(){
		Manager mgr = (Manager) session.get(Manager.class, 1) ;
		System.out.println(mgr.getMgrName());
		System.out.println(mgr.getDepartment().getDeptName());
	}
	
	
}

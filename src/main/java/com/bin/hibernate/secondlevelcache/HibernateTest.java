package com.bin.hibernate.secondlevelcache;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bin.hibernate.dao.DepartmentDao;
import com.bin.hibernate.hql.entities.Department;
import com.bin.hibernate.hql.entities.Employee;
import com.bin.hibernate.utils.HibernateUtils;

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
	 * 关闭sessionFactory的时候,会自动清除内存和硬盘的缓存.
	 */
	@Test
	public void testCollectionSecondLevelCache(){
		Department department = (Department) session.get(Department.class, 80) ;
		System.out.println(department.getName());
		System.out.println(department.getEmps().size());
		transaction.commit();
		session.close() ;
		
		session = sessionFactory.openSession() ;
		transaction = session.beginTransaction() ;
		Department department2 = (Department) session.get(Department.class, 80) ;
		System.out.println(department2.getName());
		System.out.println(department2.getEmps().size());
	}
	
	/**
	 * 查询缓存.默认情况下,设置的缓存对HQL及QBC查询是无效的.需要开启查询缓存.
	 */
	@Test
	public void testQueryCache(){
		Query query = session.createQuery("From Employee") ;
		query.setCacheable(true) ;// 设置查询缓存.依赖二级缓存.
		
		List<Employee> emps = query.list() ;
		System.out.println(emps.size());
		
		emps = query.list() ;
		System.out.println(emps.size());
		
		
		Criteria criteria = session.createCriteria(Employee.class) ;
		query.setCacheable(true) ;
//		criteria.list() ;
		
	}
	/**
	 * 更新时间戳缓存.自动添加时间戳,确定缓存是否过期,是否需要到数据库中重新取.了解就行.
	 * select --> update ---> select 发送这三条SQL语句.
	 */
	@Test
	public void testUpdateTimeStampCache(){
		Query query = session.createQuery("From Employee") ;
		query.setCacheable(true) ;// 设置查询缓存.依赖二级缓存.
		
		List<Employee> emps = query.list() ;
		System.out.println(emps.size());
		
		Employee employee = (Employee) session.get(Employee.class,100) ;
		employee.setSalary(30000);
		
		emps = query.list() ;
		System.out.println(emps.size());
		
		
		Criteria criteria = session.createCriteria(Employee.class) ;
		query.setCacheable(true) ;
		criteria.list() ;
		
	}
	
	/**
	 * Iterator只在数据库中查询ID,再通过id到二级缓存查询,如果二级缓存没有,则通过id,到数据库中查询.
	 * (省去了装配)
	 * 但是,加入缓存的命中率低的话,性能可能还会更差.所以,对这个了解就行,不需要使用.
	 */
	@Test
	public void testQueryIterator(){
		Department department = (Department) session.get(Department.class, 80) ;
		System.out.println(department.getName());
		System.out.println(department.getEmps().size());
		
		Query query = session.createQuery("From Employee e where e.dept.id = 80") ;
//		List<Employee> emps = query.list() ;
//		System.out.println(emps);
		Iterator<Employee> empIt = query.iterate();
		while (empIt.hasNext()) {
			System.out.println(empIt.next().getName());
			
		}
	}
	
	@Test
	public void testManageSession(){
		Session session = HibernateUtils.getInstance().getSession() ;
		System.out.println("-->" + session.hashCode());
		Transaction transaction = session.beginTransaction() ;
		DepartmentDao dao = new DepartmentDao() ;
		dao.save(null);
		dao.save(null);
		dao.save(null);
		
		// 若Session是由thread管理的,则在提交或回滚事务的时候已经关闭Session.
		transaction.commit();
		System.out.println(session.isOpen());
	}
	
	@Test
	public void testBatch(){
		session.doWork(new Work() {
			
			@Override
			public void execute(Connection connection) throws SQLException {
				// 通过JDBC原生的API进行操作,效率最高.
			}
		});
	}
	
	
	
}

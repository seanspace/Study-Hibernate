package com.bin.hibernate.hql;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
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

import com.bin.hibernate.hql.entities.Department;
import com.bin.hibernate.hql.entities.Employee;

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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testHQLNamedParameter(){
		/*
		 * 基于参数名称.
		 * 1. 创建Query对象.
		 * 2. 绑定参数.支持方法链的编程风格.
		 * 3. 执行查询.
		 */
		String hql = "From Employee e where e.salary > :sal and e.email like :email order by e.salary" ;
		Query query = session.createQuery(hql) ;
		query.setFloat("sal", 300).setString("email", "%A%") ;
		
		List<Employee> emps = query.list() ;
		System.out.println(emps.size());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testHQL(){
		/*
		 * 基于位置参数.
		 * 1. 创建Query对象.
		 * 2. 绑定参数.
		 * 3. 执行查询.
		 */
		String hql = "FROM Employee e WHERE e.salary > ? AND e.email LIKE ? AND e.dept = ? " ;
		Query query = session.createQuery(hql) ;
		Department dept = new Department() ;
		dept.setId(80);
		query.setFloat(0, 300)
			 .setString(1, "%A%")
			 .setEntity(2, dept);// 自动对这个实体类找到id进行匹配.
		
		List<Employee> emps = query.list() ;
		System.out.println(emps.size());
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPageQuery(){
		String hql = "from Employee" ;
		Query query = session.createQuery(hql) ;
		
		int pageNo = 22 ;
		int pageSize = 5 ;
		
		List<Employee> emps = query.setFirstResult((pageNo-1) * pageSize)
		.setMaxResults(pageSize).list();
		
		System.out.println(emps);
		
	}
	
	@Test
	public void testNamedQury(){
		Query query = session.getNamedQuery("salaryEmps") ;
		List<Employee> emps = query.setFloat("minSal", 5000).setFloat("maxSal", 10000).list() ;
		System.out.println(emps.size());
	}
	
	/**
	 * 投影查询
	 */
	@SuppressWarnings("unused")
	@Test
	public void testFieldQuery(){
		String hql = "select e.email,e.salary,e.dept from Employee e where e.dept = :dept " ;
		Query query = session.createQuery(hql) ;
		
		Department dept = new Department() ;
		dept.setId(80);
		List<Object[]> result = query.setEntity("dept", dept).list() ;
		for (Object[] objects:result) {
			System.out.println(Arrays.asList(objects));
		}
	}
	/**
	 * 投影查询2:查询结果封装成实体类.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFieldQuery2(){
		String hql = "select new Employee(e.email,e.salary,e.dept) from Employee e where e.dept = :dept " ;
		Query query = session.createQuery(hql) ;
		
		Department dept = new Department() ;
		dept.setId(80);
		List<Employee> result = query.setEntity("dept", dept).list() ;
		for (Employee emp:result) {
			System.out.println(emp.getSalary() + "," + emp.getSalary());
		}
	}
	
	/**
	 * 报表查询
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGroupBy(){
		String hql = "select min(e.salary),max(e.salary) "
				+ "from Employee e "
				+ "group by e.dept "
				+ "having min(salary) > :minSal " ;
		Query query = session.createQuery(hql).setFloat("minSal", 10000) ;
		List<Object[]> result = query.list() ;
		for (Object [] objects:result) {
			System.out.println(Arrays.asList(objects));
		}
	}
	
	/**
	 * 迫切左外连接,可以去重(自动组装),并且去重后,emp自动封装到department中.比较常用.
	 * 并且只发送一条sql
	 */
	@Test
	public void testLeftJoinFetch(){
		// 这里distinct在程序中可以去除主表的重复项,但在oracle的sql语句中没有这个效果.
//		String hql = "select distinct d From Department d left join fetch d.emps" ;
		String hql = "From Department d left join fetch d.emps" ;
		Query query = session.createQuery(hql) ;
		
		List<Department> depts = query.list() ;
		depts = new ArrayList<>(new LinkedHashSet(depts)) ;
		System.out.println(depts.size());
		for (Department dept:depts) {
			System.out.println(dept.getName() + "-" + dept.getEmps());
		}
	}
	/**
	 * 迫切内连接,和迫切左外连接一样的,只是不返回左表有而右表没有的数据.
	 */
	@Test
	public void testInnerJoinFetch(){
		// 这里distinct在程序中可以去除主表的重复项,但在oracle的sql语句中没有这个效果.
//		String hql = "select distinct d From Department d left join fetch d.emps" ;
		String hql = "From Department d left join fetch d.emps" ;
		Query query = session.createQuery(hql) ;
		
		List<Department> depts = query.list() ;
		depts = new ArrayList<>(new LinkedHashSet(depts)) ;
		System.out.println(depts.size());
		for (Department dept:depts) {
			System.out.println(dept.getName() + "-" + dept.getEmps());
		}
	}
	
	/**
	 * 左外连接,和sql中的左连接效果一样.不能去重.
	 * 遍历时多次发送请求.(查出来的emp都没有初始化.)
	 */
	@Test
	public void testLeftJoin(){
		String hql = "FROM Department d left join d.emps" ;
		Query query = session.createQuery(hql) ;
		
		List<Object[]> res = query.list() ;
		System.out.println(res.size());
		//depts = new ArrayList<>(new LinkedHashSet(depts)) ;// 无法去重
		for (Object[] objects:res) {
			// objects = [depart,emps]
			System.out.println(Arrays.asList(objects));
		}
	}
	
	/**
	 * 从n端左连接到1端,道理也是一样的.
	 */
	@Test
	public void testLeftJoinFetch2(){
		String hql = "From Employee e left join fetch e.dept" ;
		Query query = session.createQuery(hql) ;
		
		List<Employee> emps = query.list() ;
		System.out.println(emps.size());
		
		for (Employee emp:emps) {
			System.out.println(emp.getName() + "," + emp.getDept());
		}
	}
	
	
	
	@Test
	public void testQBC(){
		// 1.创建一个Criteria对象.
		
		// 2.添加查询条件.
		
		// 3.执行查询.
	}
	
	
}

package com.bin.hibernate.qbc;


import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
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
	
	/**
	 * 如何添加查询条件.
	 */
	@Test
	public void testQBC(){
		// 1.创建一个Criteria对象.
		Criteria criteria = session.createCriteria(Employee.class) ;
		
		// 2.添加查询条件.在QBC中查询条件使用Criterion来表示.Criterion可以通过Restrictions静态方法得到.
		criteria.add(Restrictions.eq("email", "SQKUMAR")) ;
		criteria.add(Restrictions.gt("salary", 5000F)) ;
		
		// 3.执行查询.
		Employee employee = (Employee) criteria.uniqueResult() ;
		System.out.println(employee);
	}
	/**
	 * And和Or的效果.
	 */
	@Test
	public void testQBC2(){
		Criteria criteria = session.createCriteria(Employee.class) ;
		
		// 1.And:  使用conjunction本身就是一个Criteriaon对象,且其中还可以添加criterion对象.
		Conjunction conjunction = Restrictions.conjunction() ;
		conjunction.add(Restrictions.like("name", "a",MatchMode.ANYWHERE)) ;
		Department dept = new Department();
		dept.setId(80);
		conjunction.add(Restrictions.eq("dept", dept)) ;
		System.out.println(conjunction);
		
		// 2.Or :  
		Disjunction disjunction = Restrictions.disjunction() ;
		disjunction.add(Restrictions.eq("salary", 6000F)) ;
		disjunction.add(Restrictions.isNull("email")) ;
		
		criteria.add(disjunction) ;
		criteria.add(conjunction) ;
		
		criteria.list() ;
//		查询条件.
//		   select
//	        this_.ID as ID1_1_0_,
//	        this_.NAME as NAME2_1_0_,
//	        this_.SALARY as SALARY3_1_0_,
//	        this_.EMAIL as EMAIL4_1_0_,
//	        this_.DEPT_ID as DEPT_ID5_1_0_ 
//	    from
//	        GG_EMPLOYEE this_ 
//	    where
//	        (
//	            this_.SALARY=? 
//	            or this_.EMAIL is null
//	        ) 
//	        and (
//	            this_.NAME like ? 
//	            and this_.DEPT_ID=?
//	        )
	}
	
	@Test
	public void testQBC3(){
		Criteria criteria = session.createCriteria(Employee.class) ;
		
		// 统计查询:使用Projection来表示:可以有Projections的静态方法得到.
		criteria.setProjection(Projections.max("salary")) ;
		
		System.out.println(criteria.uniqueResult());
//	    select
//        max(this_.SALARY) as y0_ 
//    from
//        GG_EMPLOYEE this_
//24000.0
	}
	
	@Test
	public void testQBC4(){
		Criteria criteria = session.createCriteria(Employee.class) ;
		
		// 1.添加排序
		criteria.addOrder(Order.asc("salary")) ;
		criteria.addOrder(Order.desc("email")) ;
		
		// 2. 添加翻页方法
		int pageSize = 5 ;
		int pageNo = 3 ;
		
		criteria.setFirstResult((pageNo -1) * pageSize) ;
		criteria.setMaxResults(pageSize) ;
		
		System.out.println(criteria.list()); ;
	}
	
	/**
	 * hql支持查询,delete,update操作.但是不支持insert.
	 */
	@Test
	public void testHQLUpdate(){
		String hql = "Delete from Department d where d.id = :id" ;
		session.createQuery(hql).setInteger("id",280).executeUpdate();
	}
	
	/**
	 * hql本身是不支持insert语句,使用本地sql可以执行.
	 */
	@Test
	public void testNativeSQL(){
		String sql = "Insert into gg_department values(?,?)";
		Query query = session.createSQLQuery(sql) ;
		
		query.setInteger(0, 280)
			.setString(1, "atguigu").executeUpdate();
		
		
		
	}
	
	
}

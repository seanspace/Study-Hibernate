package com.bin.hibernate.strategy;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.org.apache.xpath.internal.operations.Or;

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
	 * <class>标签的lazy=false关闭懒加载..而且这个属性仅对load()方法有效.
	 * 类级别的懒加载,开发时基本使用默认.
	 */
	@Test
	public void testClassLevelStrategy(){
		Customer customer = (Customer) session.load(Customer.class, 1) ;
		System.out.println(customer.getClass());
	}
	
	@Test
	public void testOne2ManyLevelStrategy(){
		Customer customer = (Customer) session.get(Customer.class, 1) ;
		System.out.println(customer.getCustomerName());
		
		System.out.println(customer.getOrders().size());
		/// Set的延迟加载:大部分情况也是使用默认值.
		// 1. 1-n 或 n-n的集合属性,默认使用懒加载属性.
		// 2. 可以通过修改set的lazy属性来修改默认的检索策略.默认为true.并不建议设置为false.
		// 3. lazy还可以设置为extra.增强的延迟检索.该取值会净可能延迟加载集合.比如:size()会调用count(*)
		System.out.println(customer.getOrders().contains(new Order()));
		
		// 手工初始化一个代理对象.比较常用.因为lazy属性我们大部分时候使用默认值(懒加载)
		Hibernate.initialize(customer.getOrders());
		
	}
	
	/**
	 * batch-size="5":一次初始化5个set集合.也就是说遍历5个customer,那么5个Customer的Orders集合是一次进行查询的.
	 * 可以设置一次初始化set集合的数量.可以有效减少io,也就是select语句.
	 */
	@Test
	public void testSetBatchSize(){
		List<Customer> customers = session.createQuery("From Customer").list() ;
		System.out.println(customers.size());
		
		for (Customer customer:customers) {
			if (customer.getOrders() != null) {
				System.out.println(customer.getOrders().size());
			}
		}
	}
	
	/**
	 * set的fetch属性:
	 * fecth:取值subselect将会忽略batch-size属性-->会把内存中的所有Set集合全部查询出来.通过子查询的方式,得到符合条件的customer_id
	 * fecth:取值join,采用迫切左外连接,忽略lazy属性.但是Query的list()方法会忽略这个取值,依旧采用懒加载.
	 * 
	 */
	@Test
	public void testSetFetch(){
		List<Customer> customers = session.createQuery("From Customer").list() ;
		System.out.println(customers.size());
		
		for (Customer customer:customers) {
			if (customer.getOrders() != null) {
				System.out.println(customer.getOrders().size());
			}
		}
	}
	
	/**
	 * fecth:取值join,采用迫切左外连接.
	 */
	@Test
	public void tetSetFetch2(){
		Customer customer = (Customer) session.get(Customer.class, 1) ;
		System.out.println(customer.getOrders().size());
//		   select
//	        customer0_.CUSTOMER_ID as CUSTOMER1_0_0_,
//	        customer0_.CUSTOMER_NAME as CUSTOMER2_0_0_,
//	        orders1_.CUSTOMER_ID as CUSTOMER3_0_1_,
//	        orders1_.ORDER_ID as ORDER_ID1_1_1_,
//	        orders1_.ORDER_ID as ORDER_ID1_1_2_,
//	        orders1_.ORDER_NAME as ORDER_NA2_1_2_,
//	        orders1_.CUSTOMER_ID as CUSTOMER3_1_2_ 
//	    from
//	        CUSTOMERS customer0_ 
//	    left outer join
//	        ORDERS orders1_ 
//	            on customer0_.CUSTOMER_ID=orders1_.CUSTOMER_ID 
//	    where
//	        customer0_.CUSTOMER_ID=? 
//	    order by
//	        orders1_.ORDER_NAME
	}
	
	/*
	 * 多对一:对主表记录,也是懒加载.
	 * 
	 */
	@Test
	public void testMany2OneStrategy(){
		Order order = (Order) session.get(Order.class, 1) ;
		
		// 1.lazy取值为proxy和false分别代表对应的属性采用延迟和立即检索.
		// 2. fecth取值join,忽略lazy..采用迫切左外连接.
		// 3. bacth-size,该属性需要设置在1的那一端的class元素中,如下:
		// 4. join时同样会忽略bacth-size
//		 <class name="Customer" table="CUSTOMERS" batch-size="5">,作用:一次初始化1这一端的代理对象的个数.
		List<Order> orders = session.createQuery("FROM Order o").list() ;
		for (Order order2:orders) {
			if (order2.getCustomer() != null) {
				System.out.println(order.getCustomer().getCustomerName());
			}
		}
	}
	
}

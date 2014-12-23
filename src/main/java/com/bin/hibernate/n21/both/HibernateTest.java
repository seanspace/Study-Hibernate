package com.bin.hibernate.n21.both;

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
	public void testMany2OneBothSave(){
		Customer customer = new Customer() ;
		customer.setCustomerName("AA");
		
		Order order1 = new Order() ;
		order1.setOrderName("order-3");
		
		Order order2 = new Order() ;
		order2.setOrderName("order-4");
		
		// 设定关联关系.
		order1.setCustomer(customer);
		order2.setCustomer(customer);
		customer.getOrders().add(order1) ;
		customer.getOrders().add(order2) ;
		
		// 执行save操作:先插入Customer,再插入Order,先1的一端.
		// 结果是:三个insert语句,两个update.
		// 因为1的一端和多的一端都维护关联关系.所以会多出update!
		// 应该让1的那端放弃维护关系.
		//<set name="orders" table="ORDERS" inverse="true">使1的一端放弃维护权.
		//且建议,先插入1的一端,好处是不会多出update语句.
		session.save(customer) ;
		session.save(order1) ;
		session.save(order2) ;
		
		// 先order,再Customer.先插入n的一端,
		// 结果是:先三个insert语句,然后四个update语句.
//		session.save(order1) ;
//		session.save(order2) ;
//		session.save(customer) ;
	}
	
	@Test
	public void testOne2ManyGet(){
		// 1. 对n的一端的集合使用延迟加载.
		Customer customer = (Customer) session.get(Customer.class, 1) ;
		System.out.println(customer.getCustomerName());
		
		// 2. 返回的多的一端的集合是Hibernate内置的集合类型.
		//   该类具有延迟加载和存放代理对象的功能.
		System.out.println(customer.getOrders().getClass());
		
		// 3.可能会抛出懒加载异常.
//		session.close() ;
//		System.out.println(customer.getOrders().size());
		
		// 4.在需要使用集合中元素的时候进行初始化.
	}
	
	
	@Test
	public void testMany2OneBothGet(){
		// 1.若查询多的一端的一个对象,则默认情况下,只查询多的一端的对象,而没有查询关联的1的那一端的对象.
		Order order = (Order) session.get(Order.class, 1) ;
		System.out.println(order.getOrderName());
		//session.close() ;// 懒加载异常.
		System.out.println(order.getCustomer().getClass().getName());//代理对象.
		
		// 2.在需要使用到关联的对象时,才发送对应的sql语句.
		Customer customer = order.getCustomer() ;
		System.out.println(customer.getCustomerName());
		
		// 3.在查询customer对象时,由多的一端得到1的一端时,sesion关闭了,会发生懒加载异常.
		
		// 4.获取Order对象时,默认情况下,其关联的Customer对象时一个代理对象.
		
		
	}
	
	@Test
	public void testMany2OneBothUpdate(){
//		Order order = (Order) session.get(Order.class, 1) ;
//		order.getCustomer().setCustomerName("AAA");
		Customer customer = (Customer) session.get(Customer.class, 1) ;
		customer.getOrders().iterator().next().setOrderName("GG");
	}
	
	@Test
	public void testMany2OneBothDelete(){
		// 在不设定级联关系的情况下,且1这一端的对象在n这端有引用,不能直接删除1这段的对象.
		Customer customer = (Customer) session.get(Customer.class, 1) ;
		session.delete(customer);
	}
	
	@Test
	public void testCascade(){
		Customer customer = (Customer) session.get(Customer.class, 1) ;
		customer.getOrders().clear();
	}
	
	
}

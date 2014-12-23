package com.bin.hibernate.session;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

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

import com.bin.hibernate.n21.Customer;
import com.bin.hibernate.n21.Order;

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
	 * session的二级缓存.只要Session实例没有结束生命周期,且没有清理缓存,
	 * 则存放在它缓存中的对象也不会结束生命周期.
	 * 比如:News类的对象,没有被引用,但是它被session中的集合引用了.
	 * Session缓存可以减少Hibernate应用程序访问数据库的频率.
	 */
	@Test
	public void testSessionCache(){
		News news = (News) session.get(News.class, 1) ;
		System.out.println(news);
		
		// 查询这条时,不从数据库中查询,只是直接从一级缓存(session缓存)中查询.
		News news2 = (News) session.get(News.class, 1) ;
		System.out.println(news2);
		
		
	}
	
	/**
	 * flush:是数据表中的记录和Session缓存中的对象的状态保持一致.为了保持一致,则可能会发送对应的sql语句.
	 * 1.调用Transaction的commit()的方法中:先调用session的flush()方法,再提交事务.
	 * 2.flush()方法可能会发送sql语句,但不会提交事务.只有提交了事务,数据库中的数据才被改掉.
	 * 3.注意:在未提交事务或显示的调用session.flush()方法之前,也有可能会进行flush()操作.
	 * 	1).执行HQL或QBC查询,会先flush()操作,以得到最新的数据.
	 * 	2).若记录的ID是由底层的数据库自增的方式生成的,则调用save()方法后,就会立即发送Insert语句,
	 * 		因为save方法后,必须保证对象的ID是存在的.
	 * 	3).
	 */
	@Test
	public void testSessionFlush(){
		News news = (News) session.get(News.class, 1) ;
		news.setAuthor("SUN");// 自动生成了update语句.也就是说这个方法在session没有结束前使用,会更新到数据库.
		
//		session.flush();
//		System.out.println("flush");
		
		News news2 = (News) session.createCriteria(News.class).uniqueResult() ;
		System.out.println(news2);
	}
	
	@Test
	public void testSessionFlush2(){
		News news = new News("Java","SUM",new Date()) ;
		session.save(news) ;// id是数据库生成的,所以在没有调用flush方法,就会立即发送sql,这样才能得到ID值.
	}
	
	/**
	 * refresh方法:强制发送select语句,以使session缓存中数据和数据库中的一致.
	 */
	@Test
	public void testRefresh(){
		News news = (News) session.get(News.class, 1) ;
		System.out.println(news);
		
		session.refresh(news);
		System.out.println(news);
	}
	
	/**
	 * 清理缓存.缓存中的对象被删除了.所以下面的执行结果是,发送了2条sql语句.
	 */
	@Test
	public void testClear(){
		News news = (News) session.get(News.class, 1) ;
		session.clear();// 被清出了,再需要发送sql查询.
		News news2 = (News) session.get(News.class, 1) ;
		System.out.println(news);
		System.out.println(news2);
	}
	
	/**
	 * 持久化对象的状态:
	 * 	> 临时对象(对应临时状态),OID通常为null,不在session缓存中,数据库中没有对应的记录;打算入职,未办理正式入职手续.
	 *  > 持久化对象(托管,持久状态):OID不为null,在session缓存中.入职了.
	 *  > 删除对象:曾经被session管理过.开除.
	 *  > 游离状态:OID不为null,不再处于Session缓存中.是由持久化对象转变过来的.数据库中有可能还存在与其对应的记录.请假.
	 */
	
	/**
	 * 1. save()方法
	 * 	1). 使一个临时对象变为持久化对象
	 * 	2). 为对象分配ID
	 *  3). 在flush缓存时,会发送一条insert语句
	 *  4). 在save方法之前的id是无效的.
	 *  5). 持久化的对象的ID是不能被修改!修改是会抛异常的.
	 */
	@Test
	public void testSave(){
		News news = new News();
		news.setTitle("CC");
		news.setAuthor("cc");
		news.setDate(new Date());
		
		// news.setId(11);//这是没有问题的.
		System.out.println(news);
		session.save(news) ;
		System.out.println(news);
		//news.setId(11);// 会抛出异常.
	}
	
	/**
	 * persist():也会执行insert操作.
	 * 
	 * 和save()方法有什么区别:
	 * 	1). 在调用persist方法之前,若对象已经有id了,则不会执行insert语句,而是抛出一个异常.
	 * 	2). 
	 */
	@Test
	public void testPersist(){
		News news = new News();
		news.setTitle("DD");
		news.setAuthor("dd"); ;
		news.setDate(new Date());
		
		///news.setId(11);//抛出异常.
		System.out.println(news);
		session.persist(news);
		System.out.println(news);
		//news.setId(11);// 会抛出异常.
	}
	
	/**
	 * get VS load:
	 * 1. 执行get方法:会立即加载对象,而执行load方法,若不使用该对象,则不会立即执行操作,而返回一个代理对象.(相当于,延迟加载)
	 * 			get 是 立即检索,load是延迟检索.
	 * 2. load方法可能会(比如session关闭了.)抛出LazyInitializationException异常:在需要初始化代理对象之前已经关闭了Session.
	 * 3. 若数据表中没有对应的记录.且Session也没有关闭.同时需要使用对象时.如果一直不使用此对象的话,不会抛出异常.
	 * 		get返回null
	 * 		load抛出异常
	 */
	@Test
	public void testGet(){
		News news = (News) session.get(News.class, 1) ;
		session.close() ;
		
		System.out.println(news);
	}
	
	@Test
	public void testLoad(){
		News news = (News) session.load(News.class, 1) ;
		session.close() ;
		System.out.println(news.getClass().getName());
	}
	
	/**
	 * update:
	 * 1. 若更新一个持久化对象,不需要显示的调用update方法.因为,在调用Transaction的commit()方法时会先执行flush() ;若显示调用update但是属性没有发生变化,是不会发送update语句.
	 * 2. 更新一个游离对象,需要显示的调用session的update方法,可以把一个游离对象变为持久化对象.
	 * 
	 * 注意:
	 * 	1.无论要更新的游离对象和数据表中的记录是否一致,都会发送update语句.如何能让update方法不再盲目的出发update语句呢?
	 * 		> 在 .hbm.xml文件的class节点设置select-before-update=true(默认为false,但通常不设置此属性,除非有触发器的情况,否则效率有影响) ;
	 * 	2. 若数据表中没有对应的记录,但还调用了update方法,则会抛出一个异常.
	 *  3. 在update一个游离对象时,如果session缓存中存在一个相同的OID的持久化对象,会抛出异常.因为session缓存中不应该出现两个相同OID的对象.
	 */
	@Test
	public void testUpdate(){
		News news = (News) session.get(News.class, 1) ;
		news.setAuthor("Oracle");// 这个方法也是会发送update的sql语句.
//		transaction.commit();
//		session.close() ;
		
//		news.setId(100);// 这个是数据库中不存在的,update时异常.
//		
//		session = sessionFactory.openSession() ;
//		transaction = session.beginTransaction() ;
		
		news.setAuthor("Oracle");// 另外一个session中了,也就是说不在Session缓存中了(游离对象),需要显示调用update方法
		
//		News news2 = (News) session.get(News.class, 1) ;
		
//		session.update(news);// 对于持久化对象,这句话可以不写,同样更新.
	}
	
	/**
	 * 有id则执行update语句,没有id则执行insert语句.
	 * 注意:
	 * 	1. 若OID不为空,但数据表中还没有和其对应的记录,会抛出一个异常.
	 * 	2. 了解内容:OID值等于id的unsaved-value属性值的对象,也被认为是一个游离对象. unsaved-value="11"
	 */
	@Test
	public void testSaveOrUpdate(){
		News news= new News("FF","ff",new Date()) ;
		news.setId(1);
		
		session.saveOrUpdate(news);
	}
	
	/**
	 * delete:执行删除操作,只要OID和数据表中一条记录对应,就会准备删除操作,
	 * 	如果OID在数据库表中没有对应的记录,则抛出一个异常.
	 * 
	 * 	delete语句是在commit时候才执行,所以期间调用save等方法,是会出问题的.所以,
	 * 	使删除对象后,把其OID设置为null ;需要在cfg.xml中配置一个属性.
	 * 
	 */
	@Test
	public void testDelete(){
		News news = new News() ;///这是删除游离对象.
		news.setId(2);
		session.delete(news);
		
		News news2 = (News) session.get(News.class, 3) ;// 这是删除持久对象.
		
		session.delete(news2);
	}
	
	/**
	 * evict:从session缓存中把指定的持久化对象移除.
	 */
	@Test
	public void testEvict(){
		News news = (News) session.get(News.class, 3) ;
		News news2 = (News) session.get(News.class, 3) ;
		
		news.setTitle("AA");
		news2.setTitle("BB");
		session.evict(news);// 移除了缓存,不会发送更新AA语句.
	}
	
	/**
	 * session.doWork():可以使用原生的connection,所以可以调用存储过程.
	 */
	@Test
	public void testDoWork(){
		session.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				System.out.println(connection);
				// 调用存储过程.
			}
		});
	}
	
	/**
	 * 动态生成SQL语句,值更新需要更新的数据.dynamic-update=true
	 */
	@Test
	public void testDynamicUpdate() {
		News news = (News)session.get(News.class, 1) ;
		news.setAuthor("ABC");
	}
	
	/**
	 * 1. increament:有并发问题,每次都会向数据库select max(id)
	 * 2. identity:由数据库自动生成,oracle不支持.
	 * 3. sequence:由数据库提供的序列,mysql不支持.
	 * 4. hilo:高低算法
	 * 5. native:跨平台,自动选择一种合适的.
	 * 
	 */
	@Test
	public void testIdGrneratorr(){
		News news = new News("AA","aa",new Date()) ;
		session.save(news) ;
		
		System.out.println(news.getDate());
		System.out.println(news.getDate().getClass());
	}
	
	@Test
	public void testBlob() throws IOException, SQLException{
/*		插入blob
 * 		News news = new News("CC","cc",new Date()) ;
		news.setDesc1("DESC");
		
		InputStream stream = new FileInputStream("Y510P.jpg") ;
		Blob image = Hibernate.getLobCreator(session).createBlob(stream,stream.available()) ;
		news.setImage(image);
		
		session.save(news) ;*/
		
		
		/*
		 * 读取Blob数据
		 */
		News news = (News) session.get(News.class, 1) ;
		Blob image = news.getImage() ;
		
		InputStream in = image.getBinaryStream() ;
		System.out.println(in.available());
		
	}
	
	
	@Test
	public void testComponent(){
		Worker worker = new Worker() ;
		Pay pay = new Pay() ;
		
		pay.setMonthlyPay(1000);
		pay.setYearPay(80000);
		pay.setVocationWithPay(5);
		
		worker.setName("ABCD");
		worker.setPay(pay);
		session.save(worker) ;
		
	}
	
	@Test
	public void testMany2OneSave(){
		Customer customer = new Customer() ;
		customer.setCustomerName("AA");
		
		Order order1 = new Order() ;
		order1.setOrderName("order-3");
		order1.setCustomer(customer);
		
		Order order2 = new Order() ;
		order2.setOrderName("order-4");
		order2.setCustomer(customer);
		
		// 执行save操作:先插入Customer,再插入Order,先1的一端.
		// 结果是:三个insert语句.
//		session.save(customer) ;
//		session.save(order1) ;
//		session.save(order2) ;
		
		// 先order,再Customer.先插入n的一端,
		// 结果是:先三个insert语句,然后两个update语句.
		session.save(order1) ;
		session.save(order2) ;
		session.save(customer) ;
	}
	
	
	@Test
	public void testMany2OneGet(){
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
	public void testMany2OneUpdate(){
		Order order = (Order) session.get(Order.class, 1) ;
		order.getCustomer().setCustomerName("AAA");
	}
	
	@Test
	public void testMany2OneDelete(){
		// 在不设定级联关系的情况下,且1这一端的对象在n这端有引用,不能直接删除1这段的对象.
		Customer customer = (Customer) session.get(Customer.class, 1) ;
		session.delete(customer);
		
	}
	// com.bin.hibernate.n21.both.
	@Test
	public void testMany2OneBothSave(){
		com.bin.hibernate.n21.both.Customer customer = new com.bin.hibernate.n21.both.Customer() ;
		customer.setCustomerName("AA");
		
		com.bin.hibernate.n21.both.Order order1 = new com.bin.hibernate.n21.both.Order() ;
		order1.setOrderName("order-3");
		order1.setCustomer(customer);
		
		com.bin.hibernate.n21.both.Order order2 = new com.bin.hibernate.n21.both.Order() ;
		order2.setOrderName("order-4");
		order2.setCustomer(customer);
		
		// 执行save操作:先插入Customer,再插入Order,先1的一端.
		// 结果是:三个insert语句.
		session.save(customer) ;
		session.save(order1) ;
		session.save(order2) ;
		
		// 先order,再Customer.先插入n的一端,
		// 结果是:先三个insert语句,然后两个update语句.
//		session.save(order1) ;
//		session.save(order2) ;
//		session.save(customer) ;
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
		Order order = (Order) session.get(Order.class, 1) ;
		order.getCustomer().setCustomerName("AAA");
	}
	
	@Test
	public void testMany2OneBothDelete(){
		// 在不设定级联关系的情况下,且1这一端的对象在n这端有引用,不能直接删除1这段的对象.
		Customer customer = (Customer) session.get(Customer.class, 1) ;
		session.delete(customer);
		
	}
	
	
}

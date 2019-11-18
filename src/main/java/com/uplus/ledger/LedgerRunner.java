package com.uplus.ledger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.uplus.ledger.mapper.uplus_mapper;

/*
 * 0. 휴일처리 : 휴일시 주문처리 하지 않음.
 * 1. 동시호가처리 
 * 2. LOSSCUT LOGIC : 3초단위 INTERVAL, 휴일과 장종료후는 실행안함.
 * 3. OVER-NIGHT : 오버나잇 처리 및 오버나잇 수수료 적용
 * 4. PNL 처리
 * 5. MATCHING 처리
 * 6. 원장 매칭은 API에서 직접 시세를 받아서 처리해야될지 고민 : 속도문제 나올듯함.
 */

@EnableRabbit
@Component
@EnableScheduling
public class LedgerRunner {
	
	@Autowired
	public Environment environment;

	@Autowired
	public RabbitTemplate rabbitTemplate;
	
	@Autowired 
	uplus_mapper	mapper;
	
	@Autowired
	public RedisTemplate<String, Object> redisTemplate;
		
	//환경설정
	public Gson gson = new Gson();
	
	public ConcurrentHashMap<String, Object>	system_env = new ConcurrentHashMap<String, Object>();
	public Set<String> holidays = null;	
	public OrderMgr	order_mgr = new OrderMgr(this);					//주문관련처리
	public PnlMgr pnl_mgr = new PnlMgr(this); 						//손익계산처리
	public MatchingMgr matching_mgr = new MatchingMgr(this);		//체결처리
	public OvernightMgr overnight_mgr = new OvernightMgr(this);		//오버나잇처리
	public RMSMgr rms_mgr = new RMSMgr(this);						//오버나잇처리
	public UserMgr user_mgr = new UserMgr(this);					//사용자관리
	public EnvMgr env_mgr = new EnvMgr(this);						//환경설정 및 시장상태관리
	public LosscutMgr losscut_mgr = new LosscutMgr(this);			//로스컷처리
	public StopLossMgr stoploss_mgr = new StopLossMgr(this);
	
	public ArrayList<HashMap<String, Object>> buffer_data = new ArrayList<HashMap<String, Object>>();
	
	public  boolean	autoAck = false;
	public 	boolean test_send = false;
	
	public void PrintLogForce ( String log )
	{
		try {
			UplusLedgerApplication.PrintLog(log);
		} catch ( Exception e )
		{
			System.out.println("PrintLogForce Error");
		}
	}
	
	public void PrintDBLog( String log )
	{
		/*
		try {
			
			String filename = String.format("%s/%s_dblog.sql", environment.getProperty("dblog.path").toString(), utils.GetDate2());
			File fp = new File(filename);
			FileWriter writer = new FileWriter(fp, true);
			writer.write( log + "\r\n" );
			writer.flush();
			writer.close();
		} catch ( IOException e )
		{
			PrintLogForce ( e.getMessage() );
		}
		*/
		try {
			PrintLogForce ( log );
		} catch ( Exception e )
		{
			System.out.println("PrintDBLog Error");
		}
	}
	
	public String GetEnv ( String name )
	{
		String value = new String();
		Object object = redisTemplate.opsForHash().get("ENVIRONMENT", name);
		if ( object != null )
		{
			value = object.toString();
			return value;
		}
		return null;
	}
	
    public void Send2MQChart(HashMap<String, Object> data) {
    	try {
	    	if ( data != null )
	    	{
		    	String send_data = gson.toJson(data);
		        rabbitTemplate.convertAndSend(environment.getProperty("feed.chart"), send_data);
	    	}
    	} catch ( Exception e )
    	{
    		PrintLogForce("Send2MQ E-" + e.getMessage());
    	}
    }
    
    public void Send2MQLedger(HashMap<String, Object> data) {
    	try {
	    	if ( data != null )
	    	{
		    	String send_data = gson.toJson(data);
		        rabbitTemplate.convertAndSend(environment.getProperty("feed.queue"), send_data);
	    	}
    	} catch ( Exception e )
    	{
    		PrintLogForce("Send2MQ E-" + e.getMessage());
    	}
    }
	
	public void SendClientMessage ( HashMap<String, Object> data )
	{
		String json = gson.toJson(data);
		rabbitTemplate.convertAndSend(environment.getProperty("client.feed"), "", json );
	}
	
	public void SendClientMessage ( User_Info data )
	{
		String json = data.toJson();
		rabbitTemplate.convertAndSend(environment.getProperty("client.feed"), "", json );
	}
	
	public void Send_Response ( String exchange, HashMap<String, Object> data )
	{
		String json = gson.toJson(data);
		rabbitTemplate.convertAndSend(exchange, "", json );
		if ( !exchange.equals("tiger.pnl") )
		{
			//PrintLogForce("exchange=" + exchange);
		}
	}
	
	public void Send_Environment ( String exchange, String tr )
	{
		HashMap<String, Object> object = new HashMap<String, Object>();
		object.put("tr", tr);
		String json = gson.toJson(object);
		rabbitTemplate.convertAndSend(exchange, "", json );
	}
	
	public String GetSymbolValue ( String symbol, String value_name )
	{
		String value = new String();
		Object mem_obj = redisTemplate.opsForHash().get("SYMBOL", symbol);
		if ( mem_obj != null )
		{
			HashMap<String, Object> object = gson.fromJson(mem_obj.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
			if ( object!= null )
			{
				if ( object.get(value_name) != null )
				{
					value = object.get(value_name).toString();
				}
			}
		}
		return value;
	}
	
	public String GetEvent ( String symbol )
	{
		String value = new String();
		Object mem_obj = redisTemplate.opsForHash().get("SYMBOL", symbol);
		if ( mem_obj != null )
		{
			HashMap<String, Object> object = gson.fromJson(mem_obj.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
			if ( object!= null )
			{
				if ( object.get("info1") != null && !object.get("info1").toString().equals("") )
				{
					value = object.get("info1").toString();
				}
				if ( object.get("info2") != null && !object.get("info2").toString().equals("") )
				{
					value = object.get("info2").toString();
				}
				if ( object.get("info3") != null && !object.get("info3").toString().equals("") )
				{
					value = object.get("info3").toString();
				}
				if ( object.get("info4") != null && !object.get("info4").toString().equals("") )
				{
					value = object.get("info4").toString();
				}
			}
		}
		return value;
	}
	
	/*
    @GetMapping("/RunOverNight")   
	public Object GetInterest(@RequestParam HashMap<String, Object> data) {
    	
    	overnight_mgr.TestOverNight();
    	
    	return data;
    }
    */
	
	@Bean
	public void	Initialization()
	{  
		//API 사용시 이 종목들은 REAL SET을 걸어야된다.
		Object object = redisTemplate.opsForHash().keys("HOLIDAY");
		if ( object != null )
		{
			holidays = gson.fromJson(object.toString(), new TypeToken<Set<String>>(){}.getType());
		} else {
			PrintLogForce("휴일설정을 확인하세요.");
		}
		
		if ( environment.containsProperty("spring.rabbitmq.listener.direct.acknowledge-mode") )
		{
			String value = environment.getProperty("spring.rabbitmq.listener.direct.acknowledge-mode");
			if ( value.equals("auto") )
			{
				autoAck = true;
			}
		}
		
		if ( environment.containsProperty("test.send") )
		{
			String value = environment.getProperty("test.send");
			if ( value.equals("true") )
			{
				test_send = true;
			}
		}
		
		//test.send
		
		PrintDBLog (  "env_mgr.Initialization-Start" );
		env_mgr.Initialization();
		PrintDBLog (  "env_mgr.Initialization-OK" );
		
		PrintDBLog (  "user_mgr.Initialization-Start" );
		user_mgr.Initialization();
		PrintDBLog (  "user_mgr.Initialization-OK" );
		
		PrintDBLog (  "user_mgr.Initialization-Start" );
		order_mgr.Initialization();
		PrintDBLog (  "user_mgr.Initialization-OK" );
		
		PrintDBLog (  "stoploss_mgr.Initialization-Start" );
		stoploss_mgr.Initialization();
		PrintDBLog (  "stoploss_mgr.Initialization-OK" );

		PrintDBLog (  "rms_mgr.Initialization-Start" );
		rms_mgr.Initialization();
		PrintDBLog (  "rms_mgr.Initialization-OK" );
				
		
		//rms_mgr
		
		PrintDBLog (  "금일날짜-" + utils.GetDate2() );
		
		PrintDBLog ( "VERSION-2019/11/11");
	}
    
    public void RunMatching(HashMap<String, Object> item) throws Exception {
    	HashMap<String, Object> item_data = matching_mgr.MatchingData ( item );		//매칭처리를 한다.
    	if ( item_data == null )
    	{
    		if ( item.get("symbol") != null )
    		{
    			String symbol = item.get("symbol").toString(); 			
    			Object mem_data = redisTemplate.opsForHash().get("SYMBOL", symbol);
    			if ( mem_data != null )
    			{
    				item_data = gson.fromJson(mem_data.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
    			}
    		}    		
    	}
    	if ( item_data != null )
    	{
    		stoploss_mgr.RunStopLoss( item_data );
    		pnl_mgr.UpdatePNL ( item_data );
    	}
    }
    
	@Scheduled(initialDelay = 1000, fixedRateString = "100")
    public void RunningSender() {
		synchronized (this) {
	    	try {
				for ( int i=buffer_data.size()-1; i>=0; i-- )
				{
					HashMap<String, Object> object = buffer_data.get(i);
		    		if ( object != null )
		    		{
		    			
		    			if ( test_send )
		    			{
		    				PrintLogForce( object.toString() );
		    			}
		    			
		    			if ( object.get("tr") != null )
		    			{
		    				if ( env_mgr.현재장상태.equals(MarketStatus.동시호가) || env_mgr.현재장상태.equals(MarketStatus.장운영) )
		    				{
	
		    				} else if ( env_mgr.현재장상태.equals(MarketStatus.동시호가) )
		    				{
		    					String msg = env_mgr.CheckMarket();
		    					PrintLogForce( msg );
		    				}
		    				order_mgr.GetLedger().RunMatching( object );
		    			} else {
		    				if ( env_mgr.현재장상태.equals(MarketStatus.동시호가) || env_mgr.현재장상태.equals(MarketStatus.장운영) )
		    				{
	
		    				} else if ( env_mgr.현재장상태.equals(MarketStatus.동시호가) )
		    				{
		    					String msg = env_mgr.CheckMarket();
		    					PrintLogForce( msg );
		    				}
		    				order_mgr.GetLedger().RunMatching( object );
		    			}
		    			buffer_data.remove(i);
		    		}
				}
				if ( buffer_data.size() == 0 )
				{
					//PrintLogForce( "리스트없음" );
				}
	    	} catch ( Exception e )
	    	{
	    		
	    	}
		}
	}
    
    /*
	@Scheduled(initialDelay = 1000, fixedRateString = "500")
    public void TestSender() {
		synchronized (this) {
			HashMap<String, Object> item_data  = new HashMap<String, Object>();
			item_data.put("tr", utils.TR_MATCHING);
			item_data.put("symbol", "000660");
			String send_data = gson.toJson(item_data);
			rabbitTemplate.convertAndSend("ledger.queue", send_data);
		}
	}
	*/
    
	//시세를 받는다. -> API에서 직접 받게되면 없어짐.
    @RabbitListener(queues = "#{T(org.springframework.util.CollectionUtils).arrayToList('${ledger.queue}'.split(','))}")
    public void ReceiveData(String payload, @Header(AmqpHeaders.CHANNEL) Channel channel, 
    						@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag)
            throws IOException 
    {
    	try {
    		HashMap<String, Object> object = gson.fromJson(payload, new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
    		buffer_data.add(0, object);
    	} catch ( Exception e )
    	{
    		if ( !autoAck )
    			channel.basicAck(deliveryTag, false); 
    		System.out.println("uplus_feed_api-Listener:" + payload);
    	} finally {
    		if ( !autoAck )
    			channel.basicAck(deliveryTag, false);    		
    	}
    }
    
    @RabbitListener(queues = "#{T(org.springframework.util.CollectionUtils).arrayToList('${ledger.queue.private}'.split(','))}")
    public void ReceivePrivate(String payload, @Header(AmqpHeaders.CHANNEL) Channel channel, 
    						@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag)
            throws IOException 
    {
    	try {
    		HashMap<String, Object> object = gson.fromJson(payload, new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
    		if ( object != null )
    		{
    			//장시작전과 장종료후를 체크해서 REPLY 메시지를 보낸다.
    			if ( object.get("tr") != null )
    			{
    				boolean bManagerOrderCheck = false;
    				String message = new String();
    				if ( object.get("tr").equals(utils.MTS_ORDER_NEW_WTS ) || object.get("tr").equals(utils.MTS_ORDER_CORRECTION_WTS ) ||
    					 object.get("tr").equals(utils.MTS_ORDER_CANCEL_WTS ) || object.get("tr").equals(utils.MTS_ORDER_CALCELALL_WTS ) ||
    					 object.get("tr").equals(utils.MTS_ORDER_LIQUID_WTS)  )
    				{    	    			
    	    			if ( object.get("password") != null )
    	    			{
    	    				String password = object.get("password").toString();
    	    				if ( password.equals(utils.ORDER_KEY) )
    	    				{
    	    					bManagerOrderCheck = true;
    	    				}
    	    			}
    	    			if (!bManagerOrderCheck)
    	    			{
    	    				message = env_mgr.CheckMarket();
    	    			} else {
    	    				message = "관리자주문입니다.";
    	    			}
    				}
    				
    				object.put("message", message);
    				if ( object.get("tr").equals(utils.MTS_REGISTER_CLIENT) ) {
    					user_mgr.RegisterClient ( object );
    				} else if ( object.get("tr").equals(utils.MTS_UPDATE_USER_INFO) )
    				{
    					user_mgr.UpdateClientInfo( object );
    				} else if ( object.get("tr").equals(utils.MTS_UPDATE_BALANCE) )
    				{
    					user_mgr.UpdateClientInfo( object );
    				} else if ( object.get("tr").equals(utils.MTS_SET_ENVIRONMENT) )
    				{
    					env_mgr.ReloadEnvironment(true);
    					PrintLogForce("시스템 환경설정 데이터 변경이 적용되었습니다.");
    				} else if ( object.get("tr").equals(utils.MTS_STOPLOSS) )
    				{
    					stoploss_mgr.InstallStopLoss( object );
    				} else if ( object.get("tr").equals(utils.MTS_CLEAR_USER_DATA) )
    				{
    					user_mgr.ClearUserData(object);
    				} else if ( object.get("tr").equals(utils.MTS_ORDER_NEW_WTS ) )
    				{
    					order_mgr.NewOrder ( bManagerOrderCheck, object );
    				} else if ( object.get("tr").equals(utils.MTS_ORDER_CORRECTION_WTS ) )
    				{
    					order_mgr.CorrectOrder ( bManagerOrderCheck, object );
    				} else if ( object.get("tr").equals(utils.MTS_ORDER_CANCEL_WTS ) )
    				{
    					order_mgr.CancelOrder ( bManagerOrderCheck, object );
    				} else if ( object.get("tr").equals(utils.MTS_ORDER_CALCELALL_WTS ) )
    				{
    					order_mgr.CancelAllOrder ( bManagerOrderCheck, object );
    				} else if ( object.get("tr").equals(utils.MTS_ORDER_LIQUID_WTS ) )
    				{
    					order_mgr.SettleAllOrder ( bManagerOrderCheck, object );
    				} else if ( object.get("tr").equals(utils.MTS_LEDGER_UPDATE_POSITION ) ) 
    				{
    					order_mgr.UpdateForceManagerPosition( object );
    				} else if ( object.get("tr").equals(utils.MTS_LEDGER_INSERT_POSITION ) ) 
    				{
    					order_mgr.InsertForceManagerPosition( object );
    				} else if ( object.get("tr").equals(utils.MTS_CLEAR_LEDGER_DATA ) ) 
    				{
    					env_mgr.Initialization();
    					String new_date = utils.GetDate();
    					PrintLogForce("강제 정산 실행-" + new_date);
    					stoploss_mgr.UnInstallAll();
    					redisTemplate.opsForValue().set("SYSTEM_NOTICE", new_date);
    				} else if ( object.get("tr").equals(utils.MTS_FORCE_OVERNIGHT ) ) 
    				{
    					overnight_mgr.TestOverNight();
    					PrintLogForce("강제오버나잇실행");
    				} else if ( object.get("tr").equals(utils.MTS_MODIFY_OVERNIGHT ) )
    				{
    					order_mgr.ModifyOverNight( object );
    					PrintLogForce("오버나잇일자 정정 - " + object.toString() );
    				} else if ( object.get("tr").equals(utils.MTS_RELOAD_EXCEPT_SYMBOL ) )
    				{
    					rms_mgr.LoadExceptionSymbol( false );
    					PrintLogForce("RMS설정 - " + object.toString() );
    				} else if ( object.get("tr").equals(utils.MTS_UPDATE_USER_MAX_ITEM ) )
    				{
    					user_mgr.UpdateClient_MaxItem( object );
    				}
    			}
    		}
    	} catch ( Exception e )
    	{
    		if ( !autoAck )
    			channel.basicAck(deliveryTag, false); 
    		System.out.println("uplus_feed_api-Listener:" + payload);
    	} finally {
    		if ( !autoAck )
    			channel.basicAck(deliveryTag, false);    		
    	}
    }
}

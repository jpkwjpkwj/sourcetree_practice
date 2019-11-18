package com.uplus.ledger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

@Component
@EnableScheduling
public class UserMgr {
	
	private LedgerRunner runner = null;
	public ConcurrentHashMap <String, User_Info> user_list = new ConcurrentHashMap <String, User_Info>();
	public ConcurrentHashMap <String, User_Info> active_user_list = new ConcurrentHashMap <String, User_Info>();
	public UserMgr ( LedgerRunner runner )
	{
		this.runner = runner;
	}
	
	public void Initialization()
	{		
		user_list.clear();
		active_user_list.clear();
		ArrayList<HashMap<String, Object>> user_list_db = runner.mapper.GetUserList();
		for ( int i=0; i<user_list_db.size(); i++ )
		{
			HashMap<String, Object> user_info = user_list_db.get(i);
			if ( user_info.get("id") != null )
			{
				String id = user_info.get("id").toString();
				if ( !id.equals("") )
				{
					User_Info new_user = new User_Info( runner, user_info );
					user_list.put(new_user.id, new_user);
					runner.redisTemplate.opsForHash().put("USER_INFO", id, new_user.toJson());
					
					runner.redisTemplate.delete("CONTRACT-" + new_user.id);
					runner.redisTemplate.delete("OUTSTANDING-" + new_user.id);
				}
			}
		}
		runner.PrintLogForce("사용자리스트를 로드했습니다.");
	}
	
	public User_Info GetUserInfoDB ( String id  )
	{
		HashMap<String, Object> user_info = runner.mapper.GetUserInfo(id);
		User_Info new_user = new User_Info( runner, user_info );
		runner.redisTemplate.opsForHash().put("USER_INFO", id, new_user.toJson());
		user_list.remove(id);
		user_list.put(id, new_user);
		return new_user;
	}
	
	public User_Info GetUserInfo ( String id  )
	{
		User_Info new_user = user_list.get(id);
		try {			
			if ( new_user == null )
			{
				HashMap<String, Object> user_info = runner.mapper.GetUserInfo(id);
				new_user = new User_Info( runner, user_info );
				user_list.put(new_user.id, new_user);
				runner.redisTemplate.opsForHash().put("USER_INFO", id, new_user.toJson());
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("GetUserInfo:" + e.getMessage());
		}
		return new_user;
	}
	
    public void UpdateClientInfo ( HashMap<String, Object> data )
    {
    	if ( data.get("id") != null )
    	{
    		String id = data.get("id").toString();
    		User_Info update_user = user_list.get(id);
    		if ( update_user != null )
    		{
    			update_user.SetData(data);
    			update_user.CalLosscutRemain();
    			if ( update_user.로스컷여유금 > 0 )
    			{
    				update_user.runlosscut = Long.valueOf(0);
    			}
    			update_user.SaveMemory();
    			HashMap<String, Object> new_user = update_user.toMap();
    			new_user.put("tr", utils.MTS_REGISTER_CLIENT);
				runner.SendClientMessage( new_user );
    		} else {
    			//신규 사용자인 경우
    			update_user = runner.user_mgr.GetUserInfo(id);
    			if ( update_user != null )
    			{
    				runner.PrintLogForce( id + " 사용자가 신규 등록되었습니다." );
    			}
    		}
    	}
    }
    
    public void UpdateClient_MaxItem ( HashMap<String, Object> data )
    {
    	if ( data.get("max_item") != null )
    	{
    		Long max_item = new Long(data.get("max_item").toString());
			Set<String> keyset = user_list.keySet();
			Iterator<String> itr = keyset.iterator();
			while ( itr.hasNext() )
			{
				String key = itr.next();
				User_Info user_info = user_list.get(key);
				if ( user_info != null )
				{
					user_info.종목당최대금액 = max_item;
					user_info.SaveMemory();
				}
			}
    	}
    }
    
    public void ClearUserData(HashMap<String, Object> object)
    {
    	if ( object.get("id") != null )
    	{
    		String id = object.get("id").toString();
	    	User_Info new_user = user_list.get(id);
	    	if ( new_user != null )
	    	{
	    		new_user.bankBalance = Long.valueOf(0);
	    		new_user.todayFee = Long.valueOf(0);
	    		new_user.loanBalance = Long.valueOf(0);
	    		new_user.todayProfitRealized = Long.valueOf(0);
	    		
	    		new_user.last_bankBalance = Long.valueOf(0);
	    		new_user.last_Fee = Long.valueOf(0);
	    		new_user.last_loanBalance = Long.valueOf(0);
	    		new_user.last_ProfitRealized = Long.valueOf(0);
	    		
	    		new_user.로스컷금액 = Long.valueOf(0);
	    		new_user.총평가손익 = Long.valueOf(0);
	    		new_user.로스컷여유금 = Long.valueOf(0);
	    		
	    		new_user.loanprocess = Long.valueOf(0);

	    		new_user.baseBalance = Long.valueOf(runner.GetEnv("ENV_DEPOSIT_PER_STOCK"));

	    		new_user.positions.clear();
	    		new_user.outstandings.clear();
	    		new_user.orders.clear();
	    		
	    		runner.order_mgr.RemoveOutStandingID(new_user);
	    		runner.order_mgr.RemovePositionID(new_user);
	    		
	    		new_user.SaveMemory();
	    			
	    		runner.mapper.ClearOutStanding(id);
	    		runner.mapper.ClearPosition(id);
	    		runner.mapper.ClearUserData(id);
	    		runner.mapper.ClearOrder(id);
	    		
	    		new_user.SendOutStanding();
	    		new_user.SendPosition(true);
	    		
	    		
	    		boolean bDelete = runner.redisTemplate.delete("OUTSTANDING-" + new_user.id);
	    		if ( bDelete )
	    		{
	    			runner.PrintDBLog("미체결삭제:사용자CLEAR-" + new_user.id);
	    		}
	    		bDelete = runner.redisTemplate.delete("CONTRACT-" + new_user.id);
	    		if ( bDelete )
	    		{
	    			runner.PrintDBLog("포지션삭제:사용자CLEAR-" + new_user.id);
	    		}

	    	}
    	}
    }

    /*
	@Scheduled(initialDelay = 1000, fixedDelay = 3000)
    public void CheckMarketStatus() {
        //CLIENT MESSAGE가 도착 안된다면 업데이트 처리를 한다.
		CheckUpdateClient();
	}
	*/
    
	//MQ쪽에 데이터가 밀리거나 문제가 생기면 늦게 도착하는 경우가 생기므로 직접 3초마다 읽어서 사용자 데이터를 UPDATE한다.
    public void CheckUpdateClient()
    {
    	long nsize = runner.redisTemplate.opsForSet().size("UPDATE_USER");
    	while ( nsize > 0 )
    	{
    		Object object = runner.redisTemplate.opsForSet().pop("UPDATE_USER");
    		if ( object != null )
    		{
    			String id = object.toString();
    			Object user = runner.redisTemplate.opsForHash().get ("USER_INFO", id);
    			if ( user != null )
    			{
    				HashMap<String, Object> new_user = runner.gson.fromJson(user.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
    				if ( new_user != null )
    				{
    					User_Info update_user = runner.user_mgr.user_list.get(id);
    					if ( update_user != null )
    					{
    						update_user.SetData( new_user );
    						runner.SendClientMessage(new_user);
    						new_user.put("tr", utils.MTS_REGISTER_CLIENT);
    						runner.PrintLogForce( "UPDATE FORCE USER - " + new_user.toString() );
    					}
    				}
    			}
    		}
    	}
    }
	
    public void RegisterClient ( HashMap<String, Object> data )
    {
    	//LOGIN한 CLIENT의 정보를 원장처리 서버에 등록한다.
    	if ( data.get("id") != null )
    	{
    		String id = data.get("id").toString();
    		User_Info userinfo = GetUserInfo(id);
    		if ( userinfo != null )
    		{
    			userinfo.is_login = "1";
				userinfo.CalLosscutRemain();
				//userinfo.bankBalance = userinfo.bankBalance + userinfo.총평가손익;
				//userinfo.totalBalance = userinfo.totalBalance + userinfo.총평가손익;
				//userinfo.로스컷여유금 = userinfo.로스컷여유금 + userinfo.총평가손익;	
				HashMap<String, Object> user_map = userinfo.toMap();
				ArrayList<HashMap<String, Object>> env = runner.mapper.GetEnvironment();
				for ( int i=0; i<env.size(); i++ )
				{
					HashMap<String, Object> env_item = env.get(i);
					user_map.put(env_item.get("name").toString(), env_item.get("value").toString());
				}
				user_map.put("tr", utils.MTS_REGISTER_CLIENT);
				String send_data = runner.gson.toJson(user_map);
				runner.rabbitTemplate.convertAndSend(runner.environment.getProperty("client.feed"), "", send_data );
				
				//runner.PrintLogForce(id + " 사용자가 원장에 등록됨.");
    		}
    	}
    }
}

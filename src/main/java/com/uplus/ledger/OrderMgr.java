package com.uplus.ledger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.reflect.TypeToken;

public class OrderMgr {
	private LedgerRunner runner = null;
	
	public ConcurrentHashMap <String, ConcurrentHashMap <String, Position>> contracts = new ConcurrentHashMap <String, ConcurrentHashMap <String, Position>>();		//PNL처리를 위해서
	public ConcurrentHashMap <String, ArrayList<OutStanding>> outstandings = new ConcurrentHashMap <String, ArrayList<OutStanding>>();		//체결 처리를 위해서 symbol 별로 미체결 내역을 유지한다.
	public ConcurrentHashMap <String, User_Info> message_user = new ConcurrentHashMap <String, User_Info>();
	
	public void Initialization()
	{
		contracts.clear();
		outstandings.clear();
		message_user.clear();
		
		runner.redisTemplate.delete("실시간원장");
		GetOutStandings();
		GetContracts();
	}
	
	public LedgerRunner GetLedger()
	{
		return runner;
	}
	
	public void PrintOutStandings(String symbol, ArrayList<OutStanding> outstading)
	{
		runner.PrintLogForce(String.format("등록미체결-ST[%s]", symbol));
		for ( int i=0; i<outstading.size(); i++ )
		{
			OutStanding out = outstading.get(i);
			String data = runner.gson.toJson(out.toPrint());
			runner.PrintLogForce(data);
		}
		runner.PrintLogForce(String.format("등록미체결-END[%s]", symbol));
	}
	
	public void GetOutStandings()
	{
		try {
			ArrayList<HashMap<String, Object>> user_outstadings = runner.mapper.GetOutStandings();
			if ( user_outstadings != null )
			{
				for ( int i=0; i<user_outstadings.size(); i++ )
				{
					HashMap<String, Object> item = user_outstadings.get(i);
					if ( item.get("code") != null )
					{					
						String symbol = item.get("code").toString();
						ArrayList<OutStanding> out_items = outstandings.get(symbol);
						if ( out_items == null )
						{
							out_items = new ArrayList<OutStanding>();
						}
						OutStanding out = new OutStanding ( runner, item );
						out_items.add( out );
						outstandings.put(symbol, out_items);
						if ( outstandings.get(symbol) == null )
						{
							SaveLedgerFeed(out.id, symbol);
						}
						User_Info user_info = runner.user_mgr.user_list.get(out.id);
						if ( user_info == null )
						{
							user_info = runner.user_mgr.GetUserInfoDB(out.id);
						}
						if ( user_info != null )
						{
							user_info.AddOutStandingFromDB(out);
							runner.redisTemplate.opsForHash().put("OUTSTANDING-" + out.id, out.ordNum.toString(), out.toString());
						}
						user_info.CheckActiveUsers();
					}
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)GetOutStandings-Exception-" + e.getMessage() );
		}
	}
	
	public void GetContracts()
	{
		try {
			//평가손익 계산을 하기 위해서 Contract 메모리에 올린다. 포지션은 사용자 별로 중복되지 않음.
			HashMap<String, User_Info> user_list = new HashMap<String, User_Info>();
			ArrayList<HashMap<String, Object>> user_contracts = runner.mapper.GetContracts();
			try {
				for ( int i=0; i<user_contracts.size(); i++ )
				{
					HashMap<String, Object> item = user_contracts.get(i);
					if ( item != null && item.get("code") != null )
					{
						String symbol = item.get("code").toString();
						if ( item.get("id") != null )
						{
							
							String id = item.get("id").toString();
							Position pos = new Position ( runner, item );
							//pos.SetProfit();
							
							ConcurrentHashMap <String, Position> hash_contract = contracts.get(symbol);
							if ( hash_contract == null )
							{
								hash_contract = new ConcurrentHashMap <String, Position>();
								contracts.put(symbol, hash_contract);
								SaveLedgerFeed(id, symbol);
							}
							
							if ( hash_contract != null )
							{
								hash_contract.put(id, pos);
							}
							
							User_Info user_info = runner.user_mgr.user_list.get(id);
							if ( user_info == null )
							{
						    	user_info = runner.user_mgr.GetUserInfoDB(id);
							}
					    	if ( user_info != null )
					    	{
						    	if ( user_list.get(id) == null )
						    	{
						    		user_list.put(id, user_info);
						    	}
						    	user_info.AddPosition(pos);
						    	user_info.CheckActiveUsers();
						    	
						    	runner.redisTemplate.opsForHash().put("CONTRACT-" + pos.id, pos.code, pos.toString());
					    	} else {
					    		runner.PrintLogForce("GetContract:");
					    	}
						}
					}
				}
			} catch ( Exception e )
			{
				runner.PrintLogForce("(EXCEPTION)GetContract-1-" + e.getMessage());
			}
			Set<String> keyset = user_list.keySet();
			Iterator<String> itr = keyset.iterator();
			while ( itr.hasNext() )
			{
				String key = itr.next();
				User_Info user_info = user_list.get(key);
				if ( user_info != null )
				{
		    		user_info.CalLosscutRemain();
		    		user_info.UpdateProfit();
		    		user_info.SaveMemory();
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)GetContract-2-Exception-" + e.getMessage());
		}			
	}	
	
	public Long GetLastOrderNO()
	{
		Long new_ord_no = new Long("0");
		String date = utils.GetDate();
		String no = runner.mapper.GetLastOrderNO( date );
		if ( no == null )
		{
			runner.redisTemplate.opsForValue().set("ORD_NO", "1");
			Long tmp_new_ord_no = new Long ( "1" );
			new_ord_no = tmp_new_ord_no;
		} else {
			Object new_no = runner.redisTemplate.opsForValue().get("ORD_NO");
			if ( new_no != null )
			{
				Long tmp_new_ord_no = new Long ( new_no.toString() );
				tmp_new_ord_no ++;
				runner.redisTemplate.opsForValue().set("ORD_NO", tmp_new_ord_no.toString());
				new_ord_no = tmp_new_ord_no;
			} else {
				Long tmp_new_ord_no = new Long ( no );
				tmp_new_ord_no ++;
				runner.redisTemplate.opsForValue().set("ORD_NO", tmp_new_ord_no.toString());
				new_ord_no = tmp_new_ord_no;
			}
		}
		return new_ord_no;
	}
	
	public OrderMgr ( LedgerRunner runner )
	{
		this.runner = runner;
	}
	
	public boolean InsertPosition ( User_Info user_info, Order order )
	{
		boolean bResult = false;
		ConcurrentHashMap <String, Position> positions = contracts.get(order.code);
		if ( positions != null )
		{
			Position position = positions.get(order.id);
			if ( position != null )
			{
				long nResult = position.InsertPositionDB(order);
				if ( nResult > 0 )
				{
					if ( user_info.positions.get(order.code) == null )
					{
						user_info.positions.put(order.code, position);
					}
					bResult = true;
				}
			} else {
				position = new Position(runner);
				positions.put(order.id, position);
				long nResult = position.InsertPositionDB(order);
				if ( nResult > 0 )
				{
					
					if ( user_info.positions.get(order.code) == null )
					{
						user_info.positions.put(order.code, position);
					}
					bResult = true;
				}
			}
		} else {
			positions = new ConcurrentHashMap <String, Position>();
			Position position = new Position( runner );
			position.SetPosition( order );
			positions.put(order.id, position);
		
			long nResult = position.InsertPositionDB(order);
			if ( nResult > 0 )
			{
				try {
					if ( contracts.get(order.code) == null )
					{
						contracts.put(order.code, positions);
					}
					if ( user_info.positions.get(order.code) == null )
					{
						user_info.positions.put(order.code, position);
					}
				} catch ( Exception e )
				{
					runner.PrintLogForce("InsertPosition:" + e.getMessage());
				}
				bResult = true;
			}
		}
		return bResult;
	}
	
	public boolean UpdatePosition ( User_Info user_info, Order order )
	{
		boolean bResult = false;
		ConcurrentHashMap <String, Position> positions = contracts.get(order.code);
		if ( positions != null )
		{
			Position position = positions.get(order.id);
			if ( position != null )
			{
				long nResult = position.UpdatePositionDB( order );
				if ( nResult > 0 )
				{
					bResult = true;
				}
				if ( position.volume.equals(Long.valueOf(0)))
				{
					//order.profit = position.profit;
					positions.remove(order.id);
					user_info.RemovePosition(order.code);
				}
			}
			if ( positions.size() == 0 )
			{
				positions.remove(order.code);
				contracts.remove(order.code);
			}
		}
		return bResult;
	}
	
	public void InsertOutStandingMemory (User_Info user_info, Order order, OutStanding out)
	{
		ArrayList<OutStanding> datalist = outstandings.get(order.code);
		if ( datalist == null )
		{
			datalist = new ArrayList<OutStanding>();
			datalist.add(out);
		} else {
			datalist.add(out);
		}
		outstandings.put(order.code, datalist);
		user_info.AddOutStanding(out);
		user_info.AddOrder(order);		
		PrintOutStandings(order.code, datalist);
	}
	
	
	public OutStanding UpdateOutStandingCorrect (User_Info user_info, Order new_order )
	{
		OutStanding result = null;
		ArrayList<OutStanding> datalist = outstandings.get(new_order.code);
		if ( datalist != null )
		{
			OutStanding old_out = user_info.outstandings.get(new_order.ordNumOrg);
			if ( old_out != null )
			{
				if ( old_out.volOutstd.longValue() == new_order.volOrder.longValue() )
				{
					if ( old_out.PositionTypeID.equals(new_order.PositionTypeID) )
					{
						new_order.volOrder = old_out.volOutstd;
						new_order.PositionTypeID = old_out.PositionTypeID;
						for ( int i=datalist.size()-1; i>=0; i-- )
						{
							OutStanding check = datalist.get(i);
							if ( check.ordNum.equals( new_order.ordNumOrg ) )
							{
								OutStanding new_out = check.UpdateOutStandingDB_Correct( new_order );
								if ( new_out != null )
								{
									user_info.outstandings.put(new_out.ordNum, new_out);
									user_info.outstandings.remove(new_order.ordNumOrg);
									datalist.remove(i);
									datalist.add(new_out);
									result = new_out;
									break;
								}
							}
						}
						PrintOutStandings(new_order.code, datalist);
					} else {
						HashMap<String, Object> message = new HashMap<String, Object>();
						message.put("tr", "9999");
						message.put("id", new_order.id);
						message.put("message", "[정정] 매수매도 구분을 확인하세요.\r\n" + "주문번호:" + new_order.ordNumOrg.toString() );
						runner.SendClientMessage(message);
						runner.PrintLogForce( "[정정] 매수매도 구분을 확인하세요:" + new_order.ordNum.toString());
					}
				} else {
					HashMap<String, Object> message = new HashMap<String, Object>();
					message.put("tr", "9999");
					message.put("id", new_order.id);
					message.put("message", "정정수량을 확인하세요.\r\n" + "원주문번호:" + new_order.ordNumOrg.toString() );
					runner.SendClientMessage(message);
					runner.PrintLogForce( "정정수량을확인하세요:" + new_order.ordNum.toString());	
				}
			}
		}
		return result;
	}
	
	public long RemoveOutStandingDB( User_Info user_info, Order order, Long ordno )
	{
		long nResult = 0;
		try {
			nResult = runner.mapper.RemoveOutStanding(ordno);
			if ( nResult > 0 )
			{
				nResult = runner.redisTemplate.opsForHash().delete("OUTSTANDING-" + user_info.id, ordno.toString());
				
			} else {
				nResult = order.InsertRejectOrder( user_info );
				if ( nResult > 0 )
				{					
					runner.PrintLogForce( "이미 취소되었거나 체결된 주문입니다." + order.toPrint());
				}
				HashMap<String, Object> message = new HashMap<String, Object>();
				message.put("tr", "9999");
				message.put("id", order.id);
				message.put("message", "이미 취소되었거나 체결된 주문입니다.\r\n" + "주문번호:" + order.ordNum.toString() );
				runner.SendClientMessage(message);
				runner.PrintLogForce( "이미 취소되었거나 체결된 주문입니다:" + order.ordNum.toString());				
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)RemoveOutStandingDB");
		}
		return nResult;
	}
	
	public void RemoveOutStanding (User_Info user_info, Order order, Long ordno, boolean bSendClient)
	{
		ArrayList<OutStanding> datalist = outstandings.get(order.code);
		if ( datalist != null )
		{		
			for ( int i=datalist.size()-1; i>=0; i-- )
			{
				OutStanding check = datalist.get(i);
				if ( check.ordNum.equals(ordno) )
				{
					datalist.remove(i);
					break;
				}
			}			
			if ( datalist.size() == 0 )
			{
				outstandings.remove(order.code);
			}
		}		
		RemoveOutStandingDB (user_info, order, ordno);
		user_info.RemoveOutStanding(ordno);	
		if ( bSendClient )
			user_info.SendOutStanding();
		PrintOutStandings(order.code, datalist);
	}
	
	public void RemoveOutStandingAll_OverNight ( )
	{
		try {
			Object[] user_array = runner.user_mgr.active_user_list.keySet().toArray();
			for ( int i=user_array.length-1; i>=0; i--)
			{
				String id = user_array[i].toString();
				if ( runner.user_mgr.active_user_list.containsKey(id) )
				{
					User_Info user_info = runner.user_mgr.active_user_list.get(id);
					if ( user_info != null && runner.user_mgr.active_user_list.containsValue(user_info) && user_info.outstandings.size() > 0 )
					{
						//id, symbol = ALL
						HashMap<String, Object> item = new HashMap<String, Object>(); 
						item.put("id", id);
						item.put("symbol", "ALL");						
						CancelAllOrder ( false, item );						
						user_info.SendOutStanding();
					}
				}
			}			
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)RemoveOutStandingAll_OverNight-ERROR");
		}
	}
	
	public boolean RemoveOutStandingID ( User_Info user_info )
	{
		Object[] array = outstandings.keySet().toArray();
		for ( int i=array.length-1; i>=0; i--)
		{
			String key = array[i].toString();
			ArrayList<OutStanding> datalist = outstandings.get(key);
			if ( datalist != null )
			{
				for ( int j=datalist.size()-1; j>=0; j-- )
				{
					OutStanding outstanding = datalist.get(j);
					if ( outstanding.id.equals(user_info.id) )
					{
						datalist.remove(j);
					}
				}
			}
		}
		return true;
	}
	
	public boolean RemoveOutStandingID_Losscut ( User_Info user_info )
	{
		Object[] array = outstandings.keySet().toArray();
		for ( int i=array.length-1; i>=0; i--)
		{
			String key = array[i].toString();
			ArrayList<OutStanding> datalist = outstandings.get(key);
			if ( datalist != null )
			{
				for ( int j=datalist.size()-1; j>=0; j-- )
				{
					OutStanding outstanding = datalist.get(j);
					if ( outstanding.id.equals(user_info.id) )
					{						
						if ( user_info.RemoveOutStanding(outstanding.ordNum) != null )
						{
							if ( outstanding.RemoveOutStandingDB(outstanding.ordNum) > 0 )
							{
								Order ord = user_info.RemoveOrder(outstanding.ordNum);
								if ( ord != null )
								{
									runner.PrintLogForce(String.format("로스컷 미체결 주문 삭제[%s]", ord.ordNum.toString()));
								}
							}
						}
						datalist.remove(j);
					}
				}
			}			
		}
		return true;
	}
	
	public boolean RemovePositionID ( User_Info user_info )
	{
		Object[] array = contracts.keySet().toArray();
		for ( int i=array.length-1; i>=0; i--)
		{
			String key = array[i].toString();
			ConcurrentHashMap <String, Position> position_list = contracts.get(key);
			if ( position_list != null )
			{
				Position position_item = position_list.get(user_info.id);
				if ( position_item != null )
				{
					position_list.remove( user_info.id );
					if ( position_list.size() < 1)
					{
						contracts.remove( key );
					}
				}
			}
			
		}
		return true;
	}
	
	//로스컷시 또는 오버나잇 전체 매도시사용한다.
	public boolean RemovePositionID ( User_Info user_info, Long order_type )
	{
		boolean bSuccess = true;
		Object[] array = user_info.positions.keySet().toArray();
		for ( int i=array.length-1; i>=0; i--)
		{
			String symbol_key = array[i].toString();
			Position position = user_info.positions.get(symbol_key);
			if ( position != null )
			{
				SettleOrder ( position, utils.ORDER_ACTOR_LOSSCUT, utils.LOSSCUT_ORDER );
			}
		}
		return bSuccess;
	}
	
	public boolean FilledOrder ( String symbol, OutStanding out, Long curr, Long price, Long volume )
	{
		boolean bSuccess = false;
		try {
	    	User_Info user_info = runner.user_mgr.GetUserInfo( out.id );
	    	if ( user_info != null )
	    	{    		
	    		Order order = user_info.GetOrder( out.ordNum );
	    		if ( order == null )
	    		{
	    			//NULL 인 경우에 DB를 한번 뒤져봐됨. 프로세스 OFF-ON시 ORDER 내역에 없는 경우가 있음.
	    			String date = utils.GetDate();
	    			HashMap<String, Object> new_order = runner.mapper.GetOrder(date, out.ordNum );
	    			if ( new_order != null )
	    			{
	    				order = new Order( runner.order_mgr, new_order, false );
	    				user_info.orders.put(order.ordNum, order);
	    				runner.PrintLogForce(String.format("1.FilledOrder 주문번호[%s]", out.ordNum.toString()));
	    			}
	    		}
	    		
	    		if ( order != null )
	    		{
	    			order.priceContract = price;
	    			order.volContract = volume;
	    			order.Amount = Long.valueOf(order.priceContract) * order.volContract; 
	    			order.Calculate_profit( curr, order.PositionTypeID );
	    			long nResult = order.InsertFilledOrder( user_info );
	    			if ( nResult > 0 )
	    			{
	    				if ( order.volOrder > order.volContract )
	    				{
							if ( order.PositionTypeID.equals(utils.POSITION_BUY))
							{
								InsertPosition( user_info, order );
							} else if ( order.PositionTypeID.equals(utils.POSITION_SELL))
							{
								UpdatePosition( user_info, order );
							}
							order.volOrder = order.volOrder - order.volContract; 

							//주문내역을 UPDATE한다.아직 전체 체결이 되지 않음.(부분체결)
							if ( out.volOutstd != 0 )
							{
								nResult = out.UpdateOutstandingDB();
								if ( nResult > 0 )
								{
									//runner.PrintLogForce("OutStading Update" );									
								}
								user_info.SendOutStanding();
							} else if ( out.volOutstd <= 0 ) 
							{							
								RemoveOutStanding(user_info, order, order.ordNum, true);
							}
							user_info.SendPosition(true);
							bSuccess = true;
	    				} else {	    					
							if ( order.PositionTypeID.equals(utils.POSITION_BUY))
							{
								InsertPosition( user_info, order );
							} else if ( order.PositionTypeID.equals(utils.POSITION_SELL))
							{
								UpdatePosition( user_info, order );
							}	
							RemoveOutStanding ( user_info, order, order.ordNum , true);
							user_info.SendPosition(true);
							bSuccess = true;			
	    				}
	    			}
	    			//SaveLedgerFeed(order.id, order.code);	
	    			order.InserActionLog("신규주문체결", user_info);	
	    		}
	    	}		
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)FilledOrder E:" + e.getMessage() );
		}
		return bSuccess;
	}
	
	public boolean CheckPositionSize( User_Info user_info, Order order )
	{
		//매도가능수량을 2차로 체크해봐야된다.
		boolean bCheck = true;
		if ( order.PositionTypeID.equals(utils.POSITION_SELL) )
		{
			Position position = user_info.positions.get( order.code );
			if ( order.volOrder > position.volume )
			{
				bCheck = false;
			}
		}
		return bCheck;
	}
	
    public boolean OverNightFeeOrder ( User_Info user_info,  Position position )
    {
    	boolean bResult = false;
    	try {
	    	if ( !runner.env_mgr.IsHoliday() )
			{
				Order order = new Order( this, position );
				Object mem_data = runner.redisTemplate.opsForHash().get("SYMBOL", order.code);
				if ( mem_data != null )
				{
					HashMap<String, Object> object = runner.gson.fromJson(mem_data.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
					if ( object != null && object.get("price") != null )
					{
						Depth depth = new Depth(runner, object);	
						Long price = new Long(utils.toNumber(object.get("price").toString()));
						Long filledPrice = new Long(price);
						Long bid1price = depth.GetBidPrice(0);
						//매수1호가보다 아래쪽에서 주문이 들어왔을때.
						if ( bid1price > 0 )
						{
							if ( price <= bid1price )
							{
								filledPrice = price;
							} else 
							{
								filledPrice = bid1price;
							}
						}
						order.priceOrder = filledPrice;
					}
				}
				order.PositionTypeID = utils.POSITION_BUY;
				if ( order.InsertOverNightFeeOrder( user_info ) > 0 )
				{
					bResult = true;
				}
				
			} else {
				runner.PrintLogForce("휴일입니다.");
			}
    	} catch ( Exception e )
    	{
    		runner.PrintLogForce("(EXCEPTION)OverNightFeeOrder ERROR");
    	}
		return bResult;
    }
    
    public boolean SettleOrder( Position position,  Long actor, Long ord_type )
    {
    	//청산가를 가져온다.
    	boolean bResult = false;
    	try {
	    	Order order = new Order( this, position );
	    	order.ORDER_ACTOR_TYPE = actor;
	    	User_Info user_info = runner.user_mgr.GetUserInfo( order.id );
	    	if ( user_info != null )
	    	{
		    	Object mem_data = runner.redisTemplate.opsForHash().get("SYMBOL", order.code);
		    	if ( mem_data != null )
		    	{
		    		HashMap<String, Object> object = runner.gson.fromJson(mem_data.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
		    		if ( object != null && object.get("price") != null )
		    		{
		    			Depth depth = new Depth(runner, object);	
		    			Long price = new Long(utils.toNumber(object.get("price").toString()));
		    			Long filledPrice = new Long(price);
		    			Long bid1price = depth.GetBidPrice(0);
		    			//매수1호가보다 아래쪽에서 주문이 들어왔을때.
		    			if ( bid1price > 0 )
		    			{
		    				if ( price <= bid1price )
		    				{
		    					filledPrice = price;
		    				} else 
		    				{
		    					filledPrice = price;
		    				}
		    			}
		    			order.priceOrder = filledPrice;
		    			Long nResult = Long.valueOf(-1);
		    			if ( ord_type.equals(utils.NEW_ORDER) )
		    			{
		    				nResult = order.InsertNewOrder(user_info);
		    			} else if ( ord_type.equals(utils.LOSSCUT_ORDER) )
		    			{
		    				nResult = order.InsertLosscutOrder(user_info);
		    				order.InserActionLog("로스컷/접수", user_info);
		    			} else if ( ord_type.equals(utils.OVERNIGHT_ORDER) )
		    			{
		    				nResult = order.InsertOverNightOrder(user_info);
		    				order.InserActionLog("오버나잇/접수", user_info);
		    			} else if ( ord_type.equals(utils.STOP_PLUS) )
		    			{
		    				nResult = order.InsertStopLossOrder(user_info, utils.STOP_PLUS);
		    				order.InserActionLog("STOP익절/접수", user_info);
		    			} else if ( ord_type.equals(utils.STOP_MINUS) )
		    			{
		    				nResult = order.InsertStopLossOrder(user_info, utils.STOP_MINUS);
		    				order.InserActionLog("STOP손절/접수", user_info);
		    			}
		    			
		    			if ( nResult > 0 )
		    			{
	    	    			order.priceContract = filledPrice;
	    	    			order.volContract = order.volOrder;
	    	    			order.Amount = Long.valueOf(order.priceContract) * order.volContract; 
	    	    			order.Calculate_profit( filledPrice, order.PositionTypeID );
	    	    			bResult = UpdatePosition ( user_info, order );
	    	    			if ( nResult > 0)
	    	    			{
	    	    				//원장및 사용자에 등록된 POSITION 객체를 CLEAR 한다. 
	    	    				nResult = order.InsertFilledOrder( user_info );
	    	    				if (! bResult )
	    	    				{
	    	    					runner.PrintLogForce( "오버나잇포지션업데이트실패:" + position.toJSON() );
	    	    				} else {
	    	    					//SaveLedgerFeed(order.id, order.code);
	    	    	    			if ( ord_type.equals(utils.NEW_ORDER) )
	    	    	    			{
	    	    	    				order.InserActionLog("전체매도", user_info);
	    	    	    			} else if ( ord_type.equals(utils.LOSSCUT_ORDER) )
	    	    	    			{
	    	    	    				order.InserActionLog("로스컷/체결", user_info);
	    	    	    			} else if ( ord_type.equals(utils.OVERNIGHT_ORDER) )
	    	    	    			{
	    	    	    				order.InserActionLog("오버나잇/체결", user_info);
	    	    	    			} else if ( ord_type.equals(utils.STOP_PLUS) )
	    	    	    			{
	    	    	    				order.InserActionLog("STOP익절/체결", user_info);
	    	    	    			} else if ( ord_type.equals(utils.STOP_MINUS) )
	    	    	    			{
	    	    	    				order.InserActionLog("STOP손절/체결", user_info);
	    	    	    			}
	    	    				}
	    	    				SaveLedgerFeed(order.id, order.code);
	    	    			} else {
	    	    				order.InsertRejectOrder(user_info);
	    	    			}
		    			}
	    			}
	    		}
	    	}
	    	
    	} catch ( Exception e )
    	{
    		runner.PrintLogForce("(EXCEPTION)SettleOrder ERROR");
    	}
    	return bResult;
    }
    
    public void SettleAllOrder ( boolean bManager, HashMap<String, Object> data )
    {
    	//종목코드가 없으면 전체 청산 종목코드가 있다면 해당 종목만 청산.
    	try {
	    	if ( data.get("id") != null )
	    	{
	    		String id = data.get("id").toString();
	    		User_Info user_info = runner.user_mgr.GetUserInfo( id );
	    		if ( bManager || (!bManager && runner.env_mgr.CanOrder()) )
	        	{
		    		String symbol = new String();
		        	if ( data.get("symbol") != null )
		        	{
		        		symbol = data.get("symbol").toString();
		        	}
		        	String ip = new String();
		        	if ( data.get("ip") != null )
		        	{
		        		ip = data.get("ip").toString();
		        	}
		        	String tr = new String();
		        	if ( data.get("tr") != null )
		        	{
		        		tr = data.get("tr").toString();
		        	}
		        	String orderkey = new String();
		        	if ( data.get("orderkey") != null )
		        	{
		        		orderkey = data.get("orderkey").toString();
		        	}
		        	String actor = new String();
		        	if ( data.get("actor") != null )
		        	{
		        		actor = data.get("actor").toString();
		        	}
		        	String password = new String();
		        	if ( data.get("password") != null )
		        	{
		        		password = data.get("password").toString();
		        	}
		        	
		        	boolean bSendMsg = false;
		        	
		    		if ( user_info != null )
		    		{
		    			if ( symbol.toLowerCase().equals("all") || symbol.equals("") )
		    			{
		    				//전체청산
		    				Object[] array_pos = user_info.positions.keySet().toArray();
		    				for ( int j=array_pos.length-1; j>=0; j-- )
		    				{
		    					String key = array_pos[j].toString();
		    					Position position = user_info.positions.get( key );
		        				if (position != null)
		        				{
		        					//현종목청산
		        					Long volume = user_info.TotalOutStandingVolume( key );
			    					Long diff_vol = position.volume - volume;
			    					if ( diff_vol > 0 )
			    					{
			        					runner.PrintLogForce("전체청산:" + position.code);
			        					if (runner.env_mgr.동시호가() )
			        					{
			        						HashMap<String, Object> new_order = new HashMap<String, Object>();
			        						new_order.put("tr", tr);
			        						new_order.put("id", id);
			        						new_order.put("symbol", position.code);
			        						new_order.put("hogatype", utils.시장가);
			        						new_order.put("price", "0");
			        						new_order.put("ip", ip);
			        						new_order.put("position", "S");
			        						new_order.put("orderkey", orderkey);
			        						new_order.put("password", password);
			        						new_order.put("actor", actor);
			        						new_order.put("volume", position.volume.toString());
			        						NewOrder ( bManager, new_order );
			        						bSendMsg = true;
			        					} else {
			        						SettleOrder ( position, utils.GetOrderActor( actor ), utils.NEW_ORDER );
			        						bSendMsg = true;
			        					}
			    					}
		        				}
		    				}
		    				user_info.SendPosition(true);
		    			} else {
		    				//해당종목만 청산.
		    				Long volume = user_info.TotalOutStandingVolume(symbol);
		    				Position position = user_info.positions.get(symbol);
		    				if (position != null)
		    				{
		    					//현종목청산
		    					Long diff_vol = position.volume - volume;
		    					if ( diff_vol > 0 )
		    					{
			    					runner.PrintLogForce("현종목청산:" + position.code);
		        					if (runner.env_mgr.동시호가() )
		        					{
		        						HashMap<String, Object> new_order = new HashMap<String, Object>();
		        						new_order.put("tr", tr);
		        						new_order.put("id", id);
		        						new_order.put("symbol", position.code);
		        						new_order.put("hogatype", utils.시장가);
		        						new_order.put("price", "0");
		        						new_order.put("ip", ip);
		        						new_order.put("position", "S");
		        						new_order.put("orderkey", orderkey);
		        						new_order.put("password", password);
		        						new_order.put("volume", position.volume.toString());
		        						new_order.put("actor", actor);
		        						NewOrder ( bManager, new_order );
		        						bSendMsg = true;
		        					} else {
		        						SettleOrder ( position, utils.GetOrderActor( actor ), utils.NEW_ORDER );
		        						bSendMsg = true;
		        					}
		    					}
		    				}
			    			if ( bSendMsg )
			    				user_info.SendPosition(true);
			    			else {
			    				user_info.SendMessageToUser( "매도가능수량이 초과되었습니다.", "" );
			    			}
		    			}
		    		}
	        	} else {
	        		user_info.SendMessageToUser( runner.env_mgr.CheckMarket(), "" );
	        	}
	    	}
    	} catch ( Exception e )
    	{
    		runner.PrintLogForce("(EXCEPTION)SettleAllOrder ERROR");
    	}
    }
    
    
    public void CancelOrder ( boolean bManager, HashMap<String, Object> data )
    {
    	//단일 취소 주문시
    	String id = new String();
    	try {
    		data.put("OrderTypeID", Long.valueOf(utils.REMOVE_ORDER).toString());
	    	Order order = new Order( this, data, true );
	    	id = order.id;
	    	User_Info user_info = runner.user_mgr.GetUserInfo( order.id );
	    	if ( user_info != null )
	    	{
	    		if ( bManager || (!bManager && runner.env_mgr.CanOrder()) )
		    	{	    			
    				long nResult = order.InsertRemoveOrder( user_info );
    				if ( nResult > 0 )
    				{
	    				RemoveOutStanding( user_info, order, order.ordNumOrg , true );
	    				nResult = order.InserActionLog("취소주문접수", user_info);
	    				if ( nResult > 0 )
	    					runner.PrintLogForce( "취소주문처리성공:" + order.toPrint());
	    				SaveLedgerFeed(order.id, order.code);
    				}
		    	} else {
		    		user_info.SendMessageToUser( runner.env_mgr.CheckMarket(), "" );
		    	}
	    	}
	    	//SaveLedgerFeed(order.id, order.code);
    	} catch ( Exception e )
    	{
			HashMap<String, Object> message = new HashMap<String, Object>();
			message.put("tr", "9999");
			message.put("id", id);
			message.put("message", "이미 취소되었거나 체결된 주문입니다.");
			runner.SendClientMessage(message);
    		runner.PrintLogForce( "(EXCEPTION)(OrderMgr.CancelOrder)-" + e.getMessage());
    	}
    }
    

    public void CancelOrder ( User_Info user_info, HashMap<String, Object> data, OutStanding outstanding, boolean bClientSend )
    {
    	try {
	    	Order order = new Order(this, outstanding);
	    	order.HogaTypeID = utils.지정가;
	    	if ( data.get("actor") != null )
	    	{
	    		order.ORDER_ACTOR_TYPE = utils.GetOrderActor( data.get("actor").toString() );
	    	}	    		    
			long nResult = order.InsertRemoveOrder( user_info );
			if ( nResult > 0 )
			{
				RemoveOutStanding( user_info, order, order.ordNumOrg, bClientSend ) ;
				nResult = order.InserActionLog("취소주문접수", user_info);
				if ( nResult > 0 )
					runner.PrintLogForce( "취소주문처리성공:" + order.toPrint());
				SaveLedgerFeed(order.id, order.code);
				
			}				    
			//SaveLedgerFeed(order.id, order.code);
    	} catch ( Exception e )
    	{
    		runner.PrintLogForce("(EXCEPTION)CancelOrder ERROR");
    	}
    }
  
    
    public void CancelAllOrder ( boolean bManager, HashMap<String, Object> data )
    {
    	try {
	    	if ( data.get("id") != null )
	    	{
	    		String id = data.get("id").toString();
	    		String symbol = new String();
	        	if ( data.get("symbol") != null )
	        	{
	        		symbol = data.get("symbol").toString();
	        	}
	    		User_Info user_info = runner.user_mgr.GetUserInfo( id );
	    		if ( user_info != null )
	    		{
	    			if ( symbol.toUpperCase().equals("ALL") )
	    			{
	    				Object[] array = user_info.outstandings.keySet().toArray();
	    				for ( int i=array.length-1; i>=0; i--)
	    				{
	    					Object key = array[i];
	    					if ( user_info.outstandings.containsKey(key))
	    					{
		    					Long ord_no = new Long(key.toString());
		    					OutStanding outstanding = user_info.outstandings.get(ord_no);
		        				if (outstanding != null && user_info.outstandings.containsValue(outstanding) )
		        				{
		        					//전종목청산
	        						CancelOrder(user_info, data, outstanding, false );
	        						runner.PrintLogForce("전체취소:" + outstanding.code);
		        				}
	    					}
	    				}
	    				user_info.SendOutStanding();
	    			} else {
	    				Object[] array = user_info.outstandings.keySet().toArray();
	    				for ( int i=array.length-1; i>=0; i--)
	    				{
	    					Object key = array[i];
	    					if ( user_info.outstandings.containsKey(key))
	    					{
		    					Long ord_no = new Long(key.toString());
		    					OutStanding outstanding = user_info.outstandings.get(ord_no);
		        				if (outstanding != null)
		        				{
		        					//현종목청산
		        					if ( outstanding.code.equals(symbol) && user_info.outstandings.containsValue(outstanding) )
		        					{
		        						CancelOrder(user_info, data, outstanding, false);
		        						runner.PrintLogForce("전체취소:" + outstanding.code);
		        					}
		        				}
	    					}
	    				}
	    				user_info.SendOutStanding();
	    			}
	    		}
	    	}
    	} catch ( Exception e )
    	{
    		runner.PrintLogForce("(EXCEPTION)CancelOrder ERROR");
    	}
    }
	
    public boolean NewOrder ( boolean bManager, HashMap<String, Object> data )
    {
    	boolean bResult = false;
    	try {
	    	Order order = new Order( this, data, true );
	    	User_Info user_info = runner.user_mgr.GetUserInfo( order.id );
	    	if ( user_info != null )
	    	{
	    		if ( bManager || (!bManager && runner.env_mgr.CanOrder()) )
		    	{
			    	try {
		    			user_info.CalLosscutRemain();
		    			if ( user_info.로스컷여유금 <= Long.valueOf(0))
		    			{
		    				order.message = "로스컷 사용자입니다. 관리자에 문의하세요.(시스템거부)";
		    				user_info.SendMessageToUser( order.message, "" );
		    	    		if ( order.InsertRejectOrder(user_info) > 0 )
		    	    		{
		    	    			order.InserActionLog("주문거부-" + order.message, user_info);
		    	    		}
		    			}  else {
				    		boolean bCheckPositionSize = CheckPositionSize ( user_info, order);		
				    		if ( bCheckPositionSize )
				    		{
					    		long nResult = -1;
					    		if ( bManager || runner.env_mgr.CanOrder() )
					    		{
					    			nResult = order.InsertNewOrder( user_info );
					    			OutStanding out = new OutStanding(runner, order);
					    			nResult = out.InsertOutStandingDB( order );
	
				    				InsertOutStandingMemory ( user_info, order, out );
			    					user_info.SendOutStanding();
			    					nResult = order.InserActionLog("신규주문접수", user_info);
			    					if ( nResult > 0 )
			    					{
			    						runner.PrintLogForce("신규주문처리성공:" + order.toPrint());
			    					}
				    				boolean bFilled = runner.matching_mgr.CheckMatchingDirect(null, out, true, false, false);
				    				if ( bFilled )
				    				{
				    					runner.PrintLogForce("주문체결성공:" + order.toPrint());
				    				}
				    				bResult = true;	
				    				
				    				
				    				//미체결시 UPDATE (시세조회유지를 위해서 -> API처리 프로세스에서 확인용
				    				SaveLedgerFeed(order.id, order.code);

					    		} else {
					    			user_info.SendMessageToUser( order.message, "" );
					    		}
				    		} else {
				    			user_info.SendMessageToUser("매도가능수량을 확인하세요.", "");
				    		}
		    			}
			    	} catch ( Exception e )
			    	{
			    		runner.PrintLogForce( "(EXCEPTION)(OrderMgr.NewOrder)-" + e.getMessage());
			    		user_info.SendMessageToUser( "주문처리 실패하였습니다. 잠시후 시도하세요.", "");
			    	}
		    	} else {
		    		user_info.SendMessageToUser( runner.env_mgr.CheckMarket(), "" );
		    		if ( order.InsertRejectOrder(user_info) > 0 )
		    		{
		    			order.InserActionLog("주문거부-" + runner.env_mgr.CheckMarket(), user_info);
		    		}
		    	}	
	    	}
	    	
    	} catch ( Exception e )
    	{
    		runner.PrintLogForce("(EXCEPTION)NewOrder ERROR");
    	}
    	return bResult;
    }
    
    public void CorrectOrder ( boolean bManager, HashMap<String, Object> data )
    {
    	String id = new String();
    	try {
	    	Order order = new Order( this, data, true );
	    	id = order.id;
	    	User_Info user_info = runner.user_mgr.GetUserInfo( order.id );
	    	if ( user_info != null )
	    	{
	    		OutStanding old_out = user_info.outstandings.get(order.ordNumOrg);
	    		if ( old_out != null )
	    		{
					if ( order.priceOrder.equals(old_out.price) )
					{
						runner.PrintLogForce( "정정주문실패 가격같음:" + order.toString());
						HashMap<String, Object> message = new HashMap<String, Object>();
						message.put("tr", "9999");
						message.put("id", id);
						message.put("message", "정정 가격이 같습니다.");
						runner.SendClientMessage(message);
					} else {
			    		OutStanding out = UpdateOutStandingCorrect(user_info, order);
						if ( out != null )
						{	
							Long nResult = order.InsertReplaceOrder(user_info);
							if ( nResult > 0 )
							{
		    					user_info.RemoveOrder(order.ordNumOrg);
		    					user_info.AddOrder(order);
			    				if ( !runner.matching_mgr.CheckMatchingDirect(null, out, true, false, false) )
			    				{
			    					user_info.SendOutStanding();
			    				}
			    				nResult = order.InserActionLog("정정주문접수", user_info);
			    				if ( nResult > 0 )
			    					runner.PrintLogForce( "정정주문처리성공:" + order.toString());
							}
						} else {
							
						}
					}
	    		}
	    	}
    	} catch ( Exception e )
    	{
			HashMap<String, Object> message = new HashMap<String, Object>();
			message.put("tr", "9999");
			message.put("id", id);
			message.put("message", "이미 정정되었거나 체결된 주문입니다.");
			runner.SendClientMessage(message);
    		runner.PrintLogForce( "(EXCEPTION)(OrderMgr.CorrectOrder)-" + e.getMessage());
    	}
    }
            
    public void UpdateForceManagerPosition ( HashMap<String, Object> data )
    {
    	try {
	    	Position position = new Position(runner, data);
	    	ConcurrentHashMap <String, Position> symbol_contract = contracts.get(position.code);
	    	if ( symbol_contract != null )
	    	{
	    		Position old_position = symbol_contract.get(position.id);
	    		if ( old_position != null )
	    		{
	    			old_position.volume = position.volume;
	    			old_position.price = position.price;
	    			old_position.ov = position.ov;
	    			old_position.time =position.time;
	    			old_position.ovtime = position.ovtime;
	    			try {
		    			Long nResult = runner.mapper.UpdateContract(old_position.volume.toString(), old_position.price.toString(), 
		    										old_position.ov.toString(), old_position.ovtime, old_position.time, old_position.id, old_position.code);
		    			if ( nResult > 0 )
		    			{
		    				runner.redisTemplate.opsForHash().put("CONTRACT-" + position.id, position.code, position.toJSON());
			    			User_Info user_info = runner.user_mgr.GetUserInfo( position.id );
			    			if ( user_info != null )
			    			{
			    				user_info.SendPosition( false );
			    			}
		    			}
	    			} catch ( Exception e )
	    			{
	    				runner.PrintLogForce( "(EXCEPTION)UpdateForceManagerPosition E : " + e.getMessage());
	    			}
	    		}
	    	}
    	} catch ( Exception e )
    	{
    		runner.PrintLogForce("(EXCEPTION)UpdateForceManagerPosition ERROR");
    	}
    }
    
    public void InsertForceManagerPosition ( HashMap<String, Object> data )
    {
    	try {
	    	boolean bInsertPosition = false;
	    	Position position = new Position(runner, data);
	    	ConcurrentHashMap <String, Position> symbol_contract = contracts.get(position.code);
	    	if ( symbol_contract != null )
	    	{
	    		Position old_position = symbol_contract.get(position.id);
	    		if ( old_position == null )
	    		{
	    			bInsertPosition = true;
	    			symbol_contract.put(position.id, position);
	    		}
	    	} else {
	    		bInsertPosition = true;
	    		symbol_contract = new ConcurrentHashMap <String, Position>();
	    		symbol_contract.put(position.id, position);
	    		contracts.put(position.code, symbol_contract);    		
	    	}
	    	if (bInsertPosition)
	    	{
				try {
	    			Long nResult = runner.mapper.NewPosition(position.ordNum, position.time, position.code, position.id, position.PositionTypeID, position.volume, 
	    					position.price, position.MerchandiseTypeID, Long.valueOf("0"), Long.valueOf("0"), "ALPHA", position.ov, position.profit, position.ovtime, position.currprice);
	    			if ( nResult > 0 )
	    			{
	    				runner.redisTemplate.opsForHash().put("CONTRACT-" + position.id, position.code, position.toJSON());
		    			User_Info user_info = runner.user_mgr.GetUserInfo( position.id );
		    			if ( user_info != null )
		    			{
		    				user_info.positions.put(position.code, position);
		    				user_info.SendPosition( false );
		    			}
	    			}
				} catch ( Exception e )
				{
					runner.PrintLogForce( "(EXCEPTION)UpdateForceManagerPosition E : " + e.getMessage());
				}
	    	}
    	} catch ( Exception e )
    	{
    		runner.PrintLogForce("(EXCEPTION)UpdateForceManagerPosition ERROR");
    	}
    }
    
    public void SaveLedgerFeed( String id, String symbol )
    {
    	try {
	    	boolean bFounded = false;
	    	if ( contracts.get(symbol) != null || outstandings.get(symbol) != null )
	    	{
	    		bFounded = true;
	    	}
	    	if ( !bFounded )
	    	{
	    		runner.redisTemplate.opsForHash().delete("실시간원장", symbol);
	    	} else {
	    		runner.redisTemplate.opsForHash().put("실시간원장", symbol, symbol);
	    	}
			HashMap<String, Object>	data = new HashMap<String, Object>();
			data.put("tr", utils.MTS_UPDATE_REAL_DATA);
			data.put("id", id);
			data.put("symbol", symbol);
			runner.Send2MQLedger(data);
    	} catch ( Exception e )
    	{
    		runner.PrintLogForce("(EXCEPTION)UpdateForceManagerPosition ERROR");
    	}
    }
    
    public void ModifyOverNight ( HashMap<String, Object> data )
    {
    	if ( data.get("symbol") != null )
    	{
    		String symbol = data.get("symbol").toString();
        	if ( data.get("id") != null )
        	{
        		String id = data.get("id").toString();
        		ConcurrentHashMap <String, Position> id_position = contracts.get( symbol );
        		if ( id_position != null )
        		{
        			Position position = id_position.get( id );
        			if ( position != null )
        			{
        	        	if ( data.get("old_ov") != null )
        	        	{
        	        		String old_ov_s = data.get("old_ov").toString();
        	        		if ( old_ov_s.equals("0") )
        	        		{
                	        	if ( data.get("new_ov") != null )
                	        	{                	        		
                	        		position.ov = Long.valueOf( data.get("new_ov").toString() );
                	        		runner.redisTemplate.opsForHash().put("CONTRACT-" + position.id, position.code, position.toJSON());
                	        		runner.mapper.UpdateOVDays(id, symbol, position.ov);                	        		
                	        	}
        	        		} else {
            	        		Long old_ov = Long.valueOf( data.get("old_ov").toString() );
                	        	if ( data.get("new_ov") != null )
                	        	{
                	        		Long new_ov = Long.valueOf( data.get("new_ov").toString() );
                	        		if ( new_ov.longValue() == 0 )
                	        		{
                	        			position.ov = Long.valueOf(0);
                	        		} else {
	                	        		if ( old_ov.longValue() == 0 )
	                	        		{
	                	        			position.ov = Long.valueOf(0);
	                	        		} else {
	                	        			position.ov = new_ov;
	                	        		}
                	        		}
                	        		runner.redisTemplate.opsForHash().put("CONTRACT-" + position.id, position.code, position.toJSON());
                	        		runner.mapper.UpdateOVDays(id, symbol, position.ov);
                	        	}        	        			
        	        		}
        	        	}
        			}
        		}
    		}
    	}
    }

}


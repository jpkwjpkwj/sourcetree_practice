package com.uplus.ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

@Component
@EnableScheduling
public class StopLossMgr {
	public LedgerRunner runner = null;
	//Symbol/Id
	public HashMap<String, ArrayList<StopLoss>>	stoploss_list = new HashMap<String, ArrayList<StopLoss>>();
	
	public void Initialization()
	{
		stoploss_list.clear();
		
		Set<String> set_stop = runner.redisTemplate.keys("STOPLOSS-*");
		Iterator<String> itr_stop = set_stop.iterator();
		while ( itr_stop.hasNext() )
		{
			String key = itr_stop.next();
			runner.redisTemplate.delete(key);
		}
		
		ArrayList<HashMap<String, Object>>	datalist = runner.mapper.GetStopLoss();
		for ( int i=0; i<datalist.size(); i++ )
		{
			HashMap<String, Object> item = datalist.get(i);
			StopLoss stop_item = new StopLoss(item);
			ArrayList<StopLoss> data_list = runner.stoploss_mgr.stoploss_list.get(stop_item.code);
			if ( data_list == null )
			{
				data_list = new ArrayList<StopLoss>();
				data_list.add(stop_item);
				runner.stoploss_mgr.stoploss_list.put(stop_item.code, data_list);

			} else {
				data_list.add(stop_item);
			}
			User_Info user_info = runner.user_mgr.GetUserInfo(stop_item.id);
			if ( user_info != null )
			{
				user_info.InstallStopLoss(stop_item.code, stop_item);
			}
			runner.redisTemplate.opsForHash().put("STOPLOSS-" + stop_item.id, stop_item.code, stop_item.toString());
		}
	}
	
	public StopLossMgr( LedgerRunner runner )
	{
		this.runner = runner;
	}
	
	public void InstallStopLoss ( HashMap<String, Object > data )
	{
		if ( data.get("install") != null )
		{
			String install = data.get("install").toString();
			if ( data.get("id") != null )
			{
				String id = data.get("id").toString();
				if ( data.get("symbol") != null )
				{
					String symbol = data.get("symbol").toString();
					if ( data.get("losstick") != null )
					{
						String losstick = data.get("losstick").toString();
						if ( data.get("earntick") != null )
						{
							String earntick = data.get("earntick").toString();
							if ( install.equals("1") )
							{
								Install ( id, symbol, Long.valueOf(losstick), Long.valueOf(earntick) );
							} else if ( install.equals("0") )
							{
								UnInstall ( id, symbol );
							}
						}
					}
				}
			}
		}
	}
	
	public void Install ( String id, String symbol, Long losstick, Long earntick )
	{
		if ( runner.env_mgr.CanOrder() )
		{
			ArrayList<StopLoss>	datalist = runner.stoploss_mgr.stoploss_list.get(symbol);
			if ( datalist == null )
			{
				datalist = new ArrayList<StopLoss>();
			} else {
				for ( int i=0; i<datalist.size(); i++ )
				{
					StopLoss old_item = datalist.get(i);
					if ( old_item.id.equals(id) )
					{
						datalist.remove(i);
						break;
					}
				}
			}
			StopLoss stop_item = new StopLoss();
			stop_item.code = symbol;
			stop_item.losstick = losstick;
			stop_item.earntick = earntick;
			stop_item.id = id;
			datalist.add(stop_item);
			if ( runner.stoploss_mgr.stoploss_list.get(symbol) == null )
			{
				runner.stoploss_mgr.stoploss_list.put(symbol, datalist);
			}
			runner.PrintLogForce("STOPLOSS설정:" + stop_item.toString());
			User_Info user_info = runner.user_mgr.GetUserInfo(id);
			if ( user_info != null )
			{
				user_info.InstallStopLoss(symbol, stop_item);
			}
		} else {
			User_Info user_info = runner.user_mgr.GetUserInfo(id);
			if ( user_info != null )
			{
				user_info.SendMessageToUser( runner.env_mgr.CheckMarket(), "" );
			}
		}
	}
	
	public void UnInstall ( String id, String symbol )
	{
		try {
			ArrayList<StopLoss>	datalist = runner.stoploss_mgr.stoploss_list.get(symbol);
			if ( datalist != null )
			{
				for ( int i=datalist.size()-1; i>=0; i++ )
				{
					StopLoss stop_item = datalist.get(i);
					if ( stop_item.id.equals(id) && stop_item.code.equals(symbol) )
					{
						runner.PrintLogForce("STOPLOSS해제:" + stop_item.toString());
						User_Info user_info = runner.user_mgr.GetUserInfo(id);
						if ( user_info != null )
						{
							user_info.UnInstallStopLoss(symbol, stop_item);
							runner.mapper.UnInstallStopLoss(id, symbol);
						}
						datalist.remove(i);
						break;
					}
				}
			}	
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(EXCEPTION)UnInstall E :" + id + "/" + symbol );
		}
	}
	
	boolean UnInstallAll()
	{
		runner.mapper.UnInstallStopLossAll();
		runner.stoploss_mgr.stoploss_list.clear();
		Set<String> set_stop = runner.redisTemplate.keys("STOPLOSS-*");
		Iterator<String> itr_stop = set_stop.iterator();
		while ( itr_stop.hasNext() )
		{
			String key = itr_stop.next();
			runner.redisTemplate.delete(key);
		}
		return true;
	}
	
	@Scheduled(initialDelay = 3000, fixedRateString = "5000")
	public void StopLoss_Interval()
	{
		synchronized (this) {
			RunStopLoss_Interval();
		}
	}
	
	public void RunStopLoss_Interval()
	{
		try {
			if ( runner.env_mgr.현재장상태.equals(MarketStatus.장운영) )
			{
				Object[] array = runner.stoploss_mgr.stoploss_list.keySet().toArray();
				for ( int i=array.length-1; i>=0; i--)
				{
					String symbol = array[i].toString();
					if ( runner.stoploss_mgr.stoploss_list.containsKey(symbol) )
					{
						Object symbol_obj = runner.redisTemplate.opsForHash().get("SYMBOL", symbol);
						HashMap<String, Object> object = runner.gson.fromJson(symbol_obj.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
						if ( object != null )
						{
							RunStopLoss ( object );
						}
					}
				}
			} else {
				//runner.PrintLogForce("STOPLOSS CHECK - every 5MIN - 장종료");
			}		
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)RunStopLoss_Interval");
		}
	}

	public void RunStopLoss(HashMap<String, Object> object)
	{
		try {
			if ( object.get("symbol") != null )
			{
				String symbol = object.get("symbol").toString();
				if ( object.get("market") != null )
				{
					String market = object.get("market").toString();
					if ( object.get("price") != null )
					{
						Long price = new Long(utils.toNumber(object.get("price").toString()));
						ArrayList<StopLoss> data_list = runner.stoploss_mgr.stoploss_list.get(symbol);
						if ( data_list != null )
						{
							ConcurrentHashMap <String, Position> ord_contract = runner.order_mgr.contracts.get(symbol);
							if ( ord_contract != null )
							{
								ArrayList<User_Info> user_list = new ArrayList<User_Info>();
								for ( int i=0; i<data_list.size(); i++ )
								{
									StopLoss stop_item = data_list.get(i);
									if ( stop_item.losstick.longValue() <= 0 && stop_item.earntick.longValue() <= 0 )
									{
										runner.PrintLogForce(String.format("STOPLOSS 설정이 0, 0 [%s][%s]", stop_item.id, stop_item.code ) );
									} else {
										Position pos = ord_contract.get(stop_item.id);
										if ( pos != null && ord_contract.containsValue(pos) )
										{

											Long tick = utils.GetPriceTick(market, price);
											if ( tick.longValue() > 0 && pos.volume.longValue() > 0 )
											{
												tick = tick * pos.volume;
												pos.SetProfit();
												BigDecimal dTick = new BigDecimal(pos.profit);
												
												if ( dTick.longValue() != 0 )
												{
													dTick = dTick.divide(BigDecimal.valueOf(tick), 2, RoundingMode.CEILING);
													
													if ( dTick.longValue() > 0 )
													{
														if ( stop_item.earntick.longValue() != 0 && stop_item.earntick.longValue() < dTick.longValue() )
														{
															//이익TICK
															runner.order_mgr.SettleOrder(pos, utils.ORDER_ACTOR_STOP_PLUS, utils.STOP_PLUS);
															User_Info user_info = runner.user_mgr.GetUserInfo(pos.id);
															if ( user_info != null )
															{
																user_list.add(user_info);
																UnInstall ( stop_item.id, stop_item.code );
																user_info.SendMessageStopLoss(stop_item.code,"이익설정", stop_item.earntick.toString());
															}
															
														}
													} else if ( dTick.longValue() < 0 ) 
													{
														if ( stop_item.losstick.longValue() != 0 && stop_item.losstick.longValue() < dTick.abs().longValue() )
														{
															//손실TICK
															runner.order_mgr.SettleOrder(pos, utils.ORDER_ACTOR_STOP_MINUS, utils.STOP_MINUS);
															User_Info user_info = runner.user_mgr.GetUserInfo(pos.id);
															if ( user_info != null )
															{
																user_list.add(user_info);
																UnInstall ( stop_item.id, stop_item.code );
																user_info.SendMessageStopLoss(stop_item.code,"손실설정", stop_item.losstick.toString());
															}
														}	
													}
												}
											}
							
										}
									}
								}
								
								for ( int i=0; i<user_list.size(); i++ )
								{
									user_list.get(i).SendPosition(true);
								}
							}
						}
					}
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)RunStopLoss E:" + e.getMessage() );
		}
	}
}

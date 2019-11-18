package com.uplus.ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;

@Component
@EnableScheduling
public class MatchingMgr {
	private LedgerRunner runner = null;
	public boolean bAfterOpenMarket = false;
	
	public MatchingMgr ( LedgerRunner runner )
	{
		this.runner = runner;
	}
	
	public HashMap<String, Object> MatchingData ( HashMap<String, Object> item )
	{
		HashMap<String, Object> item_data = null;
		if ( item.get("symbol") != null )
		{
			String symbol = item.get("symbol").toString(); 			
			Object mem_data = runner.redisTemplate.opsForHash().get("SYMBOL", symbol);
			if ( mem_data != null )
			{
				item_data = runner.gson.fromJson(mem_data.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
				if ( item_data != null )
				{
					ArrayList<OutStanding> outstandings = runner.order_mgr.outstandings.get(symbol);
					if ( outstandings != null )
					{
						CheckMatching ( item_data, outstandings ); 
					}
				}
			}
		}
		return item_data;
	}
	
	public void CheckOpenMatching()
	{
		//동시호가구분인 주문들만 확인해서 체결시켜준다.
		boolean bRunMatching = false;
		Date now = utils.GetNow();
		String new_date = utils.GetDate();
		Object old_date = runner.redisTemplate.opsForValue().get("OPENNING_MARKET");
		if ( old_date == null )
		{
			bRunMatching = true;
			
		} else {
			if ( !new_date.equals(old_date.toString()) )
			{
				bRunMatching = true;
			}
		}
		if ( bRunMatching )
		{
			runner.PrintLogForce(now.toString() + " 장시작후 동시호가시 주문들 체결시작합니다.");		
			runner.redisTemplate.opsForValue().set("OPENNING_MARKET", new_date);
			synchronized (this) {
				CheckMatchingInterval(true);
			}
			runner.matching_mgr.bAfterOpenMarket = true;
		}
	}
	
	@Scheduled(initialDelay = 1000, fixedRateString = "2500")
    public void CheckMarketStatus() {
	//	if ( runner.matching_mgr.bAfterOpenMarket )
		{
			synchronized (this) {
				CheckMatchingInterval(false);
			}
		}
	}
	
	public void CheckMatchingInterval( boolean bMarketStart )
	{
		//체결 되지 않은 미체결 내역들을 주기적으로 체크한다. 
		//시세가 들어왔지만 Ledger가 받지 못할수도 있고 프로세스가 재기동되어 이전 시세를 놓치는 경우도 있으므로 강제로 메모리의 시세와 비교하여 체결시킨다.
		try {
			if ( runner.env_mgr.현재장상태.equals(MarketStatus.장운영) )
			{
				//runner.PrintLogForce("INTERVAL CHECK-START");
				Set<String> keyset = runner.user_mgr.active_user_list.keySet();
				Iterator<String> user_itr = keyset.iterator(); 
				while ( user_itr.hasNext() )
				{
					String key = user_itr.next();
					if ( runner.user_mgr.active_user_list.containsKey(key) )
					{
						User_Info user = runner.user_mgr.active_user_list.get(key);
						if ( runner.user_mgr.active_user_list.containsValue(user) )
						{
							Set<Long> keyset_out = user.outstandings.keySet();
							Iterator<Long> out_itr = keyset_out.iterator(); 
							while ( out_itr.hasNext() )
							{
								Long key_out = out_itr.next();
								if ( user.outstandings.containsKey(key_out))
								{
									OutStanding outstanding = user.outstandings.get(key_out);
									if ( user.outstandings.containsValue(outstanding))
									{
										CheckMatchingDirect ( null, outstanding, false, true, bMarketStart );
									}
								}
							}
						}
					}
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)CheckMatchingInterval");
		}
	}
	
	public String GetLogTag ( Long PositionTypeID, Long HogaTypeID, boolean bInterval )
	{
		String result = new String();
		if ( PositionTypeID.equals(utils.POSITION_BUY) )
		{
			if ( HogaTypeID.equals(utils.지정가) )
			{
				if ( bInterval )
					result = "[매수체결(지정가)-INTERVAL]";
				else 
					result = "[매수체결(지정가)]";
			} else if ( HogaTypeID.equals(utils.시장가) )
			{
				if ( bInterval )
					result = "[매수체결(시장가)-INTERVAL]";
				else 
					result = "[매수체결(시장가)]";	
			} else if ( HogaTypeID.equals(utils.동시호가지정가) )
			{
				if ( bInterval )
					result = "[매수체결(동시호가지정가)-INTERVAL]";
				else 
					result = "[매수체결(동시호가지정가)]";	
			} else if ( HogaTypeID.equals(utils.동시호가시장가) )
			{
				if ( bInterval )
					result = "[매수체결(동시호가시장가)-INTERVAL]";
				else 
					result = "[매수체결(동시호가시장가)]";	
			}
		} else if ( PositionTypeID.equals(utils.POSITION_SELL) )
		{
			if ( HogaTypeID.equals(utils.지정가) )
			{
				if ( bInterval )
					result = "[매도체결(지정가)-INTERVAL]";
				else 
					result = "[매도체결(지정가)]";
			} else if ( HogaTypeID.equals(utils.시장가) )
			{
				if ( bInterval )
					result = "[매도체결(시장가)-INTERVAL]";
				else 
					result = "[매도체결(시장가)]";	
			} else if ( HogaTypeID.equals(utils.동시호가지정가) )
			{
				if ( bInterval )
					result = "[매도체결(동시호가지정가)-INTERVAL]";
				else 
					result = "[매도체결(동시호가지정가)]";	
			} else if ( HogaTypeID.equals(utils.동시호가시장가) )
			{
				if ( bInterval )
					result = "[매도체결(동시호가시장가)-INTERVAL]";
				else 
					result = "[매도체결(동시호가시장가)]";	
			}	
		}
		return result;
	}
	
	public boolean CheckMatchingDirect ( HashMap<String, Object> object, OutStanding outstand, boolean bDirect, boolean bInterval, boolean bMarketStart )
	{
		boolean bFilled = false;
		
		try {		
			if ( runner != null && runner.env_mgr.현재장상태.equals(MarketStatus.동시호가) || runner.env_mgr.현재장상태.equals(MarketStatus.장운영) )
			{
				boolean 동시호가 = false;
				if ( runner.env_mgr.현재장상태.equals(MarketStatus.동시호가 ) )
				{
					동시호가 = true;
					//동시호가때는 체결을 대기 시킨다.
				}
				
				if ( !동시호가 )
				{	
					if ( object == null )
					{
						String symbol = outstand.code; 			
						Object mem_data = runner.redisTemplate.opsForHash().get("SYMBOL", symbol);
						if ( mem_data != null )
						{
							object = runner.gson.fromJson(mem_data.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
						}
					}
					
					if ( object != null )
					{
						if ( object.get("price") != null )
						{
							String tag = GetLogTag(outstand.PositionTypeID, outstand.HogaTypeID, bInterval);							
							Depth depth = new Depth(runner, object);								
							if ( depth.check.equals("현재가")  )
							{										
								//시가가 없으면 주문체결을 하지 않는다.
								if ( depth.open.longValue() != 0 )
								{
									if ( !bDirect )
									{
										outstand.UpdateDepth(depth);
									}							
									Long 체결수량 = new Long(0);
									if ( object.get("cvolume") != null )
									{
										BigDecimal tmp = utils.toNumberDecimal( object.get("cvolume").toString() );
										체결수량 = tmp.longValue();
									}
									
									Long priceContract = new Long(0);
									Long volContract = new Long(0);
									Long price = new Long(0);
									if ( object.get("price") != null )
									{
										BigDecimal tmp_price = utils.toNumberDecimal( object.get("price").toString() );
										price = tmp_price.longValue();
									}
									Long open = new Long(0);
									if ( object.get("open") != null )
									{
										BigDecimal tmp_open = utils.toNumberDecimal( object.get("open").toString() );
										open = tmp_open.longValue();
									}
									
									String 체결시간 = new String("");
									if ( object.get("chetime") != null )
									{
										체결시간 = object.get("chetime").toString();
									}
									
									boolean bRunFilled = true;
									
									Long filledPrice = new Long(price);
									Long ask1price = depth.GetAsk1Price();
									Long ask1count = depth.GetAsk1Count();
									Long bid1price = depth.GetBid1Price();
									Long bid1count = depth.GetBid1Count();
									
									if ( bRunFilled )
									{
										
										User_Info user_info = runner.user_mgr.GetUserInfo( outstand.id );										
										if ( user_info != null )
										{
										
											if ( outstand.PositionTypeID.equals(utils.POSITION_BUY))
											{
												//매도1호가보다 위쪽에서 주문이 들어왔을때.
												if ( ask1price > 0 )
												{
													if ( price > ask1price )
													{
														filledPrice = price;
													} else 
													{
														filledPrice = ask1price;
													}
												}
												
												if ( price > 0 )
												{
													if ( outstand.HogaTypeID.equals(utils.지정가) || outstand.HogaTypeID.equals(utils.동시호가지정가) )
													{
														if ( outstand.price > price || ( outstand.price.equals(price) && outstand.price.equals(filledPrice) ) )
														{
															priceContract = outstand.price;
															if ( ask1price > 0 && outstand.price > price )
															{
																priceContract = price;																		
																if ( outstand.price < ask1price )
																{
																	if ( !bDirect )
																	{
																		if ( outstand.HogaTypeID.equals(utils.지정가 ) )
																		{
																			priceContract = outstand.price;
																		}
																	}
																}
															} else {
																if ( !bDirect )
																{
																	if ( outstand.HogaTypeID.equals(utils.지정가 ) )
																	{
																		priceContract = outstand.price;
																	}
																}																
															}
															
															if ( bDirect && outstand.HogaTypeID.equals(utils.지정가 ) )
															{
																if ( outstand.price > ask1price )
																{
																	priceContract = ask1price;
																}
															}
															
															//장시작후 바로 체결되는 경우 -> 첫번째 체결 시도
															if ( outstand.HogaTypeID.equals(utils.동시호가지정가) && bMarketStart )
															{
																if ( outstand.price >= depth.open )
																{
																	priceContract = depth.open;
																} else {
																	priceContract = Long.valueOf(-1);
																}
															}
															
															if ( priceContract.longValue() >= 0 )
															{
																if ( outstand.volOutstd > 0 && ask1count > 0 )
																{
																	if ( outstand.volOutstd > ask1count )
																	{											
																		if ( user_info.체결방식.equals(Long.valueOf(0)))
																		{
																			volContract = outstand.volOutstd;
																			outstand.volOutstd = Long.valueOf(0);														
																		} else if ( user_info.체결방식.equals(Long.valueOf(1))) 
																		{
																			volContract = ask1count;
																			outstand.volOutstd = outstand.volOutstd - ask1count;
																		}
																	} else {
																		volContract = outstand.volOutstd;
																		outstand.volOutstd = Long.valueOf(0);			
																	}
																	boolean bSuccess = runner.order_mgr.FilledOrder( outstand.code, outstand, price, priceContract,  volContract );
																	if ( bSuccess ) 
																	{
																		bFilled = true;
																		runner.PrintLogForce( String.format("매수1.[%s][주문번호:%s][%s][주문가:%s][체결가:%s][체결수량:%d][미체결수량:%d][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																				outstand.price.toString(), priceContract, volContract, outstand.volOutstd, bid1price, ask1price, price, 체결시간 ));
																	} else {
																		runner.PrintLogForce( String.format("매수1.[%s-체결실패][주문번호:%s][%s][주문가:%s][체결가:%s][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																				outstand.price.toString(), priceContract, bid1price, ask1price, price, 체결시간 ));
																	}
																}
															}
														} else if ( bid1price.equals(price) && outstand.price.equals( bid1price ) )
														{
															//1호가 주문이면서 현재가라면 진입잔량을 확인하면서 주문처리를 한다.
															if ( outstand.CanbeFilled(체결수량, depth) )
															{
																priceContract = outstand.price;
																if ( outstand.volOutstd > 0 && ask1count > 0 )
																{
																	if ( outstand.volOutstd > ask1count )
																	{
																		if ( user_info.체결방식.equals(Long.valueOf(0)))
																		{
																			volContract = outstand.volOutstd;
																			outstand.volOutstd = Long.valueOf(0);	
																		} else if ( user_info.체결방식.equals(Long.valueOf(1))) 
																		{
																			volContract = ask1count;
																			outstand.volOutstd = outstand.volOutstd - ask1count;
																		}
																	} else {
																		volContract = outstand.volOutstd;
																		outstand.volOutstd = Long.valueOf(0);						
																	}
																	//runner.PrintLogForce("1호가주문");
																	boolean bSuccess = runner.order_mgr.FilledOrder( outstand.code, outstand, price, priceContract,  volContract );
																	if ( bSuccess ) 
																	{
																		bFilled = true;
																		runner.PrintLogForce( String.format("매수2.[%s][주문번호:%s][%s][주문가:%s][체결가:%s][체결수량:%d][미체결수량:%d][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																				outstand.price.toString(), priceContract, volContract, outstand.volOutstd, bid1price, ask1price, price, 체결시간 ));
																	} else {
																		runner.PrintLogForce( String.format("매수2.[%s-체결실패][주문번호:%s][%s][주문가:%s][체결가:%s][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																				outstand.price.toString(), priceContract, bid1price, ask1price, price, 체결시간 ));
																	}
																}
															}
														}
													} else if ( outstand.HogaTypeID.equals(utils.시장가) )
													{
														//시장가인경우 바로체결시킨다.
														priceContract = price;
														//if ( user_info.매수손익적용.longValue() == 1 )
														{
															priceContract = ask1price;
														}
														
														if ( outstand.volOutstd > 0 &&  ask1count > 0 )
														{
															volContract = outstand.volOutstd;
															outstand.volOutstd = Long.valueOf(0);	
															boolean bSuccess = runner.order_mgr.FilledOrder( outstand.code, outstand, price, priceContract,  volContract );
															if ( bSuccess ) 
															{
																runner.PrintLogForce( String.format("매수3.[%s][주문번호:%s][%s][주문가:%s][체결가:%s][체결수량:%d][미체결수량:%d][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																		outstand.price.toString(), priceContract, volContract, outstand.volOutstd, bid1price, ask1price, price, 체결시간 ));
															} else {
																runner.PrintLogForce( String.format("매수3.[%s-체결실패][주문번호:%s][%s][주문가:%s][체결가:%s][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																		outstand.price.toString(), priceContract, bid1price, ask1price, price, 체결시간 ));
															}
														}
													} else if ( outstand.HogaTypeID.equals(utils.동시호가시장가) )
													{
														//동시호가 시장가는 시가로 체결
														if ( open > 0 )
														{
															priceContract = open;
															volContract = outstand.volOutstd;
															outstand.volOutstd = Long.valueOf(0);
															boolean bSuccess = runner.order_mgr.FilledOrder( outstand.code, outstand, price, priceContract,  volContract );
															if ( bSuccess ) 
															{
																runner.PrintLogForce( String.format("매수5.[%s][주문번호:%s][%s][주문가:%s][체결가:%s][체결수량:%d][미체결수량:%d][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																		outstand.price.toString(), priceContract, volContract, outstand.volOutstd , bid1price, ask1price, price, 체결시간));
															} else {
																runner.PrintLogForce( String.format("매수5.[%s-체결실패][주문번호:%s][%s][주문가:%s][체결가:%s][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, outstand.price.toString(), 
																		priceContract, bid1price, ask1price, price, 체결시간 ));
															}
														}
													}
												}
											} else if ( outstand.PositionTypeID.equals(utils.POSITION_SELL))
											{
			
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
												if ( price > 0 )
												{
													if ( outstand.HogaTypeID.equals(utils.지정가) || outstand.HogaTypeID.equals(utils.동시호가지정가) )
													{
														if ( outstand.price < price || ( outstand.price.equals(price) && outstand.price.equals(filledPrice) ) )
														{
															priceContract = outstand.price;
															if ( bid1price > 0 && outstand.price < price )
															{
																priceContract = price;	
																if ( outstand.price > bid1price )
																{
																	if ( !bDirect )
																	{
																		if ( outstand.HogaTypeID.equals(utils.지정가 ) )
																		{
																			priceContract = outstand.price;
																		}
																	}
																}
															}
															
															if ( bDirect && outstand.HogaTypeID.equals(utils.지정가 ) )
															{
																if ( outstand.price < bid1price )
																{
																	priceContract = bid1price;
																}
															}
															
															//장시작후 바로 체결되는 경우 -> 첫번째 체결 시도
															if ( outstand.HogaTypeID.equals(utils.동시호가지정가) && bMarketStart )
															{
																if ( outstand.price <= depth.open )
																{
																	priceContract = depth.open;
																} else {
																	priceContract = Long.valueOf(-1);
																}
															}
															
															if ( priceContract.longValue() >= 0 )
															{
																if ( outstand.volOutstd > 0 && bid1count > 0 )
																{
																	if ( outstand.volOutstd > bid1count )
																	{
																		if ( user_info.체결방식.equals(Long.valueOf(0)))
																		{
																			volContract = outstand.volOutstd;
																			outstand.volOutstd = Long.valueOf(0);	
																		} else if ( user_info.체결방식.equals(Long.valueOf(1)))
																		{
																			volContract = bid1count;
																			outstand.volOutstd = outstand.volOutstd - bid1count;
																		}
																	} else {
																		volContract = outstand.volOutstd;
																		outstand.volOutstd = Long.valueOf(0);						
																	}
																	boolean bSuccess = runner.order_mgr.FilledOrder( outstand.code, outstand, price, priceContract, volContract );
																	if ( bSuccess ) 
																	{
																		bFilled = true;
																		runner.PrintLogForce( String.format("매도1.[%s][주문번호:%s][%s][주문가:%s][체결가:%s][체결수량:%d][미체결수량:%d][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																				outstand.price.toString(), priceContract, volContract, outstand.volOutstd , bid1price, ask1price, price, 체결시간));
																	} else {
																		runner.PrintLogForce( String.format("매도1.[%s-체결실패][주문번호:%s][%s][주문가:%s][체결가:%s][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																				outstand.price.toString(), priceContract, bid1price, ask1price, price, 체결시간 ));
																	}
																}
															}
														} else if ( ask1price.equals(price) && outstand.price.equals( ask1price ) )
														{
															if ( outstand.CanbeFilled(체결수량, depth) )
															{
																priceContract = outstand.price;
																if ( outstand.volOutstd > 0 && bid1count > 0 )
																{
																	if ( outstand.volOutstd > bid1count )
																	{
																		if ( user_info.체결방식.equals(Long.valueOf(0)))
																		{
																			//전량체결
																			volContract = outstand.volOutstd;
																			outstand.volOutstd = Long.valueOf(0);
																		} else if ( user_info.체결방식.equals(Long.valueOf(1)))
																		{
																			//부분체결
																			volContract = bid1count;
																			outstand.volOutstd = outstand.volOutstd - bid1count;
																		}
																	} else {
																		volContract = outstand.volOutstd;
																		outstand.volOutstd = Long.valueOf(0);						
																	}
																	boolean bSuccess = runner.order_mgr.FilledOrder( outstand.code, outstand, price, priceContract,  volContract );
																	if ( bSuccess ) 
																	{
																		bFilled = true;
																		runner.PrintLogForce( String.format("매도2.[%s][주문번호:%s][%s][주문가:%s][체결가:%s][체결수량:%d][미체결수량:%d][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, outstand.price.toString(), 
																				priceContract, volContract, outstand.volOutstd, bid1price, ask1price, price, 체결시간 ));
																	} else {
																		runner.PrintLogForce( String.format("매도2.[%s-체결실패][주문번호:%s][%s][주문가:%s][체결가:%s][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																				outstand.price.toString(), priceContract, bid1price, ask1price, price, 체결시간 ));
																	}
																}
															}
														}
													} else if ( outstand.HogaTypeID.equals(utils.시장가) )
													{
														priceContract = price;
														
														//if ( user_info.매수손익적용.longValue() == 1 )
														{
															priceContract = bid1price;
														}
														
														if ( outstand.volOutstd > 0 && bid1count > 0 )
														{
															volContract = outstand.volOutstd;
															outstand.volOutstd = Long.valueOf(0);	
															boolean bSuccess = runner.order_mgr.FilledOrder( outstand.code, outstand, price, priceContract, volContract );
															if ( bSuccess ) 
															{
																bFilled = true;
																runner.PrintLogForce( String.format("매도3.[%s][주문번호:%s][%s][주문가:%s][체결가:%s][체결수량:%d][미체결수량:%d][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, outstand.price.toString(), 
																		priceContract, volContract, outstand.volOutstd, bid1price, ask1price, price, 체결시간 ));
															} else {
																runner.PrintLogForce( String.format("매도3.[%s-체결실패][주문번호:%s][%s][주문가:%s][체결가:%s][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, outstand.price.toString(), 
																		priceContract, bid1price, ask1price, price, 체결시간 ));
															}
														}
													} else if ( outstand.HogaTypeID.equals(utils.동시호가시장가) )
													{
														if ( open > Long.valueOf(0) )
														{
															priceContract = open;
															volContract = outstand.volOutstd;
															outstand.volOutstd = Long.valueOf(0);
															boolean bSuccess = runner.order_mgr.FilledOrder( outstand.code, outstand, open, open,  volContract );
															if ( bSuccess ) 
															{
																bFilled = true;
																runner.PrintLogForce( String.format("매도5.[%s][주문번호:%s][%s][주문가:%s][체결가:%s][체결수량:%d][미체결수량:%d][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, outstand.price.toString(), 
																		priceContract, volContract, outstand.volOutstd, bid1price, ask1price, price, 체결시간 ));
															} else {
																runner.PrintLogForce( String.format("매도5.[%s-체결실패][주문번호:%s][%s][주문가:%s][체결가:%s][매수1:%d][매도1:%d][현재가:%d][체결시간:%s]", tag, outstand.ordNum.toString(), outstand.code, 
																		outstand.price.toString(), priceContract, bid1price, ask1price, price, 체결시간 ));
															}
														}	
													}
												}
											}
										}
									}
								} else {
									runner.PrintLogForce(String.format("시가 구성 안됨-현재가재요청-1[%s]", outstand.code));
									//조회를 보낸다.
									HashMap<String, Object> item = new  HashMap<String, Object>();
									item.put("tr", "TRANS");
									item.put("symbol", outstand.code );
									item.put("reset-symbol", "");
									runner.Send2MQChart(item);
								}
							} else {
								runner.PrintLogForce(String.format("현재가 조회 안됨-현재가재요청-1[%s]", outstand.code));
								//조회를 보낸다.
								HashMap<String, Object> item = new  HashMap<String, Object>();
								item.put("tr", "TRANS");
								item.put("symbol", outstand.code );
								item.put("reset-symbol", "");
								runner.Send2MQChart(item);
							}
						}
					} else {
						runner.PrintLogForce("주문실행안됨-" + outstand.toPrint() );
					}
				}
			} 
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)Matching E:" + outstand.toString());
			runner.PrintLogForce("(EXCEPTION)Matching E:" + e.getMessage() + "/" + e.getLocalizedMessage());
		}
		return bFilled;
	}
	
	public void CheckMatching ( HashMap<String, Object> object, ArrayList<OutStanding> out )
	{
		//1.동시호가시 지정가로 체결 OPEN가격으로 
		//2.VI및 단기과열 종목 매수&매도 불가.
		//3.상한가 매도시 매도1호가보다 큰경우 체결
		//4.상한가 매수시 매수1호가보다 큰경우 미체결로 대기
		//5.주문시 해당 호가의 수량을 저장한다.		
		//6.현재 장상태가 동시호가라면 주문 체결을 SKIP한다.
		//장운영시에만 시세 비교 체결을 실행한다.
		//synchronized (this) {
		if ( runner.env_mgr.현재장상태.equals(MarketStatus.장운영 ) )
		{
			if ( object != null )
			{		
				Depth depth = new Depth ( runner, object );
				if ( depth.check.equals("현재가")  )
				{	
					if ( object.get("symbol") != null )
					{

						for ( int i=0; i<out.size(); i++ )
						{
							OutStanding outstand = out.get(i);
							CheckMatchingDirect(object, outstand, false, false, false );
						}
						
						String symbol = object.get("symbol").toString();
						Object check_symbol = runner.redisTemplate.opsForValue().get("CHECK_SYMBOL");
						if ( check_symbol != null )
						{
		    				if ( symbol.equals(check_symbol.toString()) )
		    				{
		    					runner.PrintLogForce( String.format("주문체결시도[%s][%s][체결량:%d]", symbol.toString(), depth.price.toString(), depth.cvolume ) );
		    				}
						}
					}
				} else {
					//호가가 없는 경우다. 조회를 요청한다.
					runner.PrintLogForce(String.format("현재가 재요청-2[%s]", depth.symbol));
					//조회를 보낸다.
					HashMap<String, Object> item = new  HashMap<String, Object>();
					item.put("tr", "TRANS");
					item.put("symbol", depth.symbol );
					item.put("reset-symbol", "");
					runner.Send2MQChart(item);						
				}
			}
		}
		//}
	}
}

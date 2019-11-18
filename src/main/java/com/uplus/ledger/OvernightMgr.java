package com.uplus.ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class OvernightMgr {
	private LedgerRunner runner = null;
	public OvernightMgr ( LedgerRunner runner )
	{
		this.runner = runner;
	}
	
	public void SendToOverNightMessageBefore5 ( Date now )
	{
		try {
			boolean bRunOV = false;
			String new_date = utils.GetDate();
			Object save_date = runner.redisTemplate.opsForValue().get("OV_MESSAGE");
			if ( save_date == null )
			{
				bRunOV = true;
			} else {
				if ( !new_date.equals(save_date.toString()) )
				{
					bRunOV = true;
				}
			}
			if ( bRunOV )
			{
				runner.redisTemplate.opsForValue().set("OV_MESSAGE", new_date);	
				Set<String> sets = runner.user_mgr.user_list.keySet();
				Iterator<String> keys = sets.iterator();
				while ( keys.hasNext() )
				{
					String id = keys.next();
					if ( runner.user_mgr.user_list.containsKey(id) )
					{
						User_Info user_info = runner.user_mgr.user_list.get(id);
						if ( user_info != null && runner.user_mgr.user_list.containsValue(user_info) )
						{
							if ( user_info.outstandings.size() != 0 || user_info.positions.size() != 0 )
							{
								user_info.SendMessageToUserOverNight5Before(now);
							}
						}
					}
				}
			}
			
			//runner.PrintLogForce("오버나잇 5분전 메세지 전송");
			
		} catch ( Exception e )
		{
			runner.PrintLogForce("오버나잇 실행 에러");
		}
	}
	
	public void RunOverNight( Date now, boolean bForce )
	{
		try {
			boolean bRunOV = false;
			String new_date = utils.GetDate();
			Object save_date = runner.redisTemplate.opsForValue().get("OV_DATE");
			if ( save_date == null )
			{
				bRunOV = true;
			} else {
				if ( !new_date.equals(save_date.toString()) )
				{
					bRunOV = true;
				}
			}
			
			if ( bForce )
			{
				bRunOV = true;
			}
			
			if ( bRunOV )
			{
				//오버나잇은 하루에 한번씩 실행한다.
				runner.env_mgr.현재장상태 = MarketStatus.오버나잇;
				runner.PrintLogForce ( runner.env_mgr.CheckMarketFormat(now) );
				runner.redisTemplate.opsForValue().set("OV_DATE", new_date);	
				
				runner.PrintLogForce("=========================오버나잇 미체결 시작=========================");
				runner.order_mgr.RemoveOutStandingAll_OverNight();
				runner.PrintLogForce("=========================오버나잇 미체결 끝=========================");
				
				//1.미체결은 모두 ,ㅜ.ㅠ OK ? 오버나잇시에 계산하나?
				//4.오버나잇 수수료를 떼려면 주문내역에 신규 오버나잇 주문을 낸다.
				
				//포지션을 가지고 있는 user들만 filter
			
				Object[] array = runner.user_mgr.active_user_list.keySet().toArray();
				for ( int i=array.length-1; i>=0; i--)
				{
					String id = array[i].toString();
					User_Info user_info = runner.user_mgr.active_user_list.get(id);
					if ( runner.user_mgr.active_user_list.containsValue(user_info) )
					{
						if ( user_info != null && !user_info.runOV )
						{
							String msg = utils.GetMessage ( now, "오버나잇 미체결 전체 취소." + user_info.id );
							runner.PrintLogForce ( msg );
							user_info.SendOutStanding();
							
							BackupClientInfo ( user_info );
							
							if ( user_info.enableOvernight.equals(Long.valueOf(0)) )
							{
								//무조건 반대매매
								SettlePositionUser ( user_info );
							} else {
								//오버나잇 실행
								//금액 CHECK한다.
								runner.stoploss_mgr.UnInstallAll();
								if ( user_info.bankBalance > 0 && user_info.positions.size() > 0 )
								{
									RunOverNightUser( user_info );
								}
							}
							user_info.SendMessageToUser("오버나잇 실행되었습니다.자세한내역은 거래내역을 확인하세요.", "overnight");
							user_info.runOV = true;
						}
					}
				}
				runner.order_mgr.message_user.clear();			
				runner.PrintLogForce("오버나잇 실행완료");
			}
		} catch ( Exception e )
		{
			runner.redisTemplate.delete("OV_DATE");
			runner.PrintLogForce("(EXCEPTION)오버나잇 실행 에러");
		}
	}
	
	class AscendingPosition implements Comparator<Position> 
	{ 
		@Override public int compare(Position a, Position b) 
		{ 
			return b.amount.compareTo(a.amount); 	
		} 
	}
	
	public void BakupOverNightPosition( Position pos )
	{
		try {
			String insert_string = String.format("insert into tOrderContract(ordNum, time, code, id, PositionTypeID, volume, price, MerchandiseTypeID, MarketTypeID, league, ov, profit, ovtime, currprice, isReal)"
				+ " values(%d, \'%s\', \'%s\', \'%s\', %d, %d, %d, %d, %d, \'%s\', %d, %d, \'%s\', %d, %d) ", pos.ordNum, pos.time, pos.code, pos.id, pos.PositionTypeID, pos.volume, pos.price, 
				pos.MerchandiseTypeID, pos.MarketTypeID, "ALPHA", pos.ov, pos.profit, pos.ovtime, pos.currprice, 0 );
			runner.PrintDBLog(insert_string);
		} catch ( Exception e )
		{
			runner.PrintDBLog("(EXCEPTION)BackupClientInfo-ERROR");
		}
	}
	
	public void BackupClientInfo( User_Info user_info )
	{
		try {
			String update_string = String.format("update tClient set bankBalance = %d, todayProfitRealized = %d, todayFee = %d where id = \'%s\'", 
					user_info.bankBalance, user_info.todayProfitRealized, user_info.todayFee, user_info.id);
			runner.PrintDBLog(update_string);
		} catch ( Exception e )
		{
			runner.PrintDBLog("(EXCEPTION)BackupClientInfo-ERROR");
		}
	}
	
	public void SettlePositionUser(User_Info user_info)
	{
		try {
			Object[] array = user_info.positions.keySet().toArray();
			for ( int i=array.length-1; i>=0; i--)
			{
				String symbol_key = array[i].toString();
				Position pos = user_info.positions.get(symbol_key);
				if ( pos != null )
				{
					BakupOverNightPosition ( pos );
					runner.order_mgr.SettleOrder(pos, utils.ACTOR_SYSTEM, utils.OVERNIGHT_ORDER );
					runner.PrintLogForce("무조건 반대매매:" + pos.id + ":" + pos.toJSON() );
				}
			}
			user_info.SendPosition(false);
		} catch ( Exception e )
		{
			runner.PrintDBLog("(EXCEPTION)SettlePositionUser-ERROR");
		}
	}
	
	public void RunOverNightUser(User_Info user_info)
	{
		try {
			BigDecimal 총손익 = new BigDecimal("0");
			BigDecimal 총매수금액 = new BigDecimal("0");
			Set<String> contract_key = user_info.positions.keySet();
			if ( contract_key != null )
			{
				ArrayList<Position> sorted_positions = new ArrayList<Position>();				
				Iterator<String> keys = contract_key.iterator();
				Long tmp_Value = new Long(0);
				while ( keys.hasNext() )
				{
					Object key = keys.next();
					Position pos = user_info.positions.get(key);
					if ( pos != null )
					{
						sorted_positions.add(pos);						
						//총매수금액 = 총매수금액.add(BigDecimal.valueOf(pos.amount));						
						
						tmp_Value = pos.price * pos.volume;
						총매수금액 = 총매수금액.add(BigDecimal.valueOf(tmp_Value));
						
						총손익 = 총손익.add(BigDecimal.valueOf(pos.profit));
						BakupOverNightPosition ( pos );
					}
				}

				BigDecimal 담보금 = new BigDecimal(user_info.bankBalance);
				BigDecimal 오버나잇비율 = new BigDecimal(user_info.securityRate);
				BigDecimal 오버나잇가능금액 = new BigDecimal(user_info.securityRate);		//사용자의 오버나잇 가능금액 마이너스면 모든 수량을 오버나잇할수 있다. 담보금의 *N까지 가능함.
				BigDecimal 오버나잇차익금액 = new BigDecimal("0");
	
				//오버나잇 필요 담보금 = 총매수금 * 오버나잇비율
				//담보금 = 담보금.add(BigDecimal.valueOf(user_info.todayProfitRealized));  -> 당일 손익은 계산하지 않는다. 실시간 손익은 넣는다.
				담보금 = 담보금.add(총손익);
				오버나잇비율 = BigDecimal.valueOf(100).divide(오버나잇비율, 2, RoundingMode.CEILING);
				오버나잇가능금액 = 담보금.multiply(오버나잇비율);
				오버나잇차익금액 = 오버나잇가능금액.subtract(총매수금액); 	
				//매수 금액이 많은 순으로 정렬한다.
				Collections.sort(sorted_positions, new AscendingPosition());
				
				if ( 오버나잇차익금액.longValue() > 0 )
				{
					runner.PrintLogForce(String.format("[담보금충분사용자][id:%s][담보금:%d][총손익:%d][계산담보금:%d][오버나잇비율:%d][총매수금액:%d][오버나잇가능금액:%d][오버나잇차익금액%d]", 
							user_info.id, user_info.bankBalance, 총손익.longValue(), 담보금.longValue(), user_info.securityRate, 
							총매수금액.longValue(), 오버나잇가능금액.longValue(), 오버나잇차익금액.longValue()));
				} else {
					runner.PrintLogForce(String.format("[담보금불충분사용자][id:%s][담보금:%d][총손익:%d][계산담보금:%d][오버나잇비율:%d][총매수금액:%d][오버나잇가능금액:%d][오버나잇차익금액%d]", 
							user_info.id, user_info.bankBalance, 총손익.longValue(), 담보금.longValue(), user_info.securityRate, 
							총매수금액.longValue(), 오버나잇가능금액.longValue(), 오버나잇차익금액.abs().longValue()));	
				}
				//반대매매금액이 크면 담보금이 충분함.
				if ( 오버나잇차익금액.longValue() > 0 )
				{
					BigDecimal 매수비율 = new BigDecimal(0);
					for ( int i=0; i<sorted_positions.size(); i++ )
					{
						Position pos = sorted_positions.get(i);
						
						pos.매수비율 = BigDecimal.valueOf(pos.amount).divide( 총매수금액, 4, RoundingMode.HALF_DOWN ).multiply(BigDecimal.valueOf(0.01));
						
						매수비율 = 매수비율.add(pos.매수비율);
						
						runner.PrintLogForce( String.format("매수비율[%s][%s]", user_info.id, pos.매수비율.toString()));

						if ( pos.remaining_ov.equals(Long.valueOf(-1)) )	//무제한 사용자
						{
							//수수료만 징수한다. TEST OK
							if ( runner.order_mgr.OverNightFeeOrder(user_info, pos) )
							{
								runner.PrintLogForce("오버나잇무제한사용자(수수료만징수):" + pos.id + ":" + pos.toJSON());
							} else {
								runner.PrintLogForce("오버나잇무제한사용자(수수료만징수-실패):" + pos.id + ":" + pos.toJSON());
							}
						} else if ( pos.remaining_ov.equals(Long.valueOf(0)) )	//오버나잇 일자가 남지 않은 사용자
						{
							//반대매매실행 : TEST OK
							if ( runner.order_mgr.SettleOrder(pos, utils.ACTOR_SYSTEM, utils.OVERNIGHT_ORDER ) )
							{
								runner.PrintLogForce("오버나잇 반대매매:" + pos.id + ":" + pos.toJSON() );
							} else {
								runner.PrintLogForce("오버나잇 반대매매-실패:" + pos.id + ":" + pos.toJSON() );
							}
						} else if ( pos.remaining_ov > 0 )	//오버나잇 일자가 남은 사용자
						{
							//수수료만 징수(일자남음) : TEST OK
							if ( runner.order_mgr.OverNightFeeOrder(user_info, pos) )
							{
								runner.PrintLogForce("오버나잇일자남은 사용자(수수료만징수):" + pos.id + ":" + pos.toJSON());
							} else {
								runner.PrintLogForce("오버나잇일자남은 사용자(수수료만징수-실패):" + pos.id + ":" + pos.toJSON());
							}
						}
					}
				} else {
					
					BigDecimal 매도금액합 = new BigDecimal(0);
					BigDecimal 매수비율합 = new BigDecimal(0);
					BigDecimal 매도수량 = new BigDecimal(0);
					//금액이 모자른 사용자는 모자른 비율만큼 각 종목의 비율대로 반대매매한다.
					
					오버나잇차익금액 = 오버나잇차익금액.abs();
					
					for ( int i=0; i<sorted_positions.size(); i++ )
					{
						Position pos = sorted_positions.get(i);
						if ( pos.ov >= 0 )
						{
							pos.매수비율 = BigDecimal.valueOf(pos.amount).divide( 총매수금액, 4, RoundingMode.HALF_DOWN );
							pos.매수비율금액 = 오버나잇차익금액.multiply( pos.매수비율 );
							매수비율합 = 매수비율합.add(pos.매수비율);
							매도수량 = pos.매수비율금액.divide( BigDecimal.valueOf(pos.price), 4, RoundingMode.HALF_DOWN );
							
							if ( 매도수량.equals(BigDecimal.valueOf(0)))
							{
								매도수량 = BigDecimal.valueOf(1);
							}
							
							매도금액합 = 매도금액합.add(pos.매수비율금액);
							if ( pos.ov.equals(Long.valueOf(0)) )
							{
								//비율대로 매도를 한다.
								Position new_pos = new Position(pos);
								new_pos.volume = 매도수량.longValue();
								new_pos.SetAmount();
								runner.PrintLogForce(String.format("담보금불충분사용자(수수료징수-OV무제한) id(%s) code(%s) 보유수량(%s) 신규매도수량(%s)", 
															new_pos.id, new_pos.code, pos.volume.toString(), new_pos.volume.toString()));
								
								if ( pos.volume > new_pos.volume )
								{
									if ( runner.order_mgr.SettleOrder(new_pos, utils.ACTOR_SYSTEM, utils.OVERNIGHT_ORDER ) )
									{
										if ( runner.order_mgr.OverNightFeeOrder(user_info, pos) )
										{
											runner.PrintLogForce(String.format("담보금불충분사용자(수수료징수-OV무제한) id(%s) code(%s) volume(%s)", pos.id, pos.code, pos.volume.toString()));
										}
									}
								} else {
									if ( runner.order_mgr.OverNightFeeOrder(user_info, pos) )
									{
										runner.PrintLogForce(String.format("담보금불충분사용자(수수료징수-OV무제한-수량계산오류) id(%s) code(%s) volume(%s)", pos.id, pos.code, pos.volume.toString()));
									}
								}
							} else {
								if ( pos.remaining_ov > 1 )
								{
									Position new_pos = new Position(pos);
									new_pos.volume = 매도수량.longValue();
									new_pos.SetAmount();
									runner.PrintLogForce(String.format("담보금불충분사용자(수수료징수-매수일(%s)) id(%s) code(%s) 보유수량(%s) 신규매도수량(%s)", 
																		new_pos.ovtime, new_pos.id, new_pos.code, pos.volume.toString(), new_pos.volume.toString()));
									if ( pos.volume > new_pos.volume )
									{
										if ( runner.order_mgr.SettleOrder(new_pos, utils.ACTOR_SYSTEM, utils.OVERNIGHT_ORDER ) )
										{
											if ( runner.order_mgr.OverNightFeeOrder(user_info, pos) )
											{
												runner.PrintLogForce(String.format("담보금불충분사용자(수수료징수-매수일(%s)) id(%s) code(%s) 보유수량(%s)", pos.ovtime, pos.id, pos.code, pos.volume.toString()));
											}
										}
									} else {
										if ( runner.order_mgr.OverNightFeeOrder(user_info, pos) )
										{
											runner.PrintLogForce(String.format("담보금불충분사용자(수수료징수-매수일(%s)-수량계산오류) id(%s) code(%s) 보유수량(%s)", pos.ovtime, pos.id, pos.code, pos.volume.toString()));
										}
									}
								} else if ( pos.remaining_ov <= 1 )
								{
									if ( runner.order_mgr.SettleOrder(pos, utils.ACTOR_SYSTEM, utils.OVERNIGHT_ORDER ) )
									{
										runner.PrintLogForce(String.format("담보금불충분사용자(전체매도)-매수일(%s)) id(%s) code(%s) 보유수량(%s) 오버나잇잔일(%s)", 
															pos.ovtime, pos.id, pos.code, pos.volume.toString(), pos.remaining_ov.toString()));
									}
									
								} else {
									
								}
							}
						}
					}
					
					오버나잇비율 = 오버나잇차익금액.divide(총매수금액, 4, RoundingMode.HALF_DOWN );
					
					runner.PrintLogForce( "오버나잇비율:" + 오버나잇비율.toString());
					runner.PrintLogForce( String.format("매도금액합:%s, 오버나잇차익금액:%s", 매도금액합.toString(), 오버나잇차익금액.toString() ) );
					runner.PrintLogForce( "매수비율합:" + 매수비율합.toString());
				}
			}
			user_info.SendPosition(false);
		} catch ( Exception e )
		{
			runner.PrintDBLog("(EXCEPTION)RunOverNightUser-ERROR");
		}
	}
	
	public void TestOverNight()
	{
		Date now = utils.GetNow();
		runner.overnight_mgr.RunOverNight(now, true);
	}
}

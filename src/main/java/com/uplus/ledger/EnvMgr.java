package com.uplus.ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class EnvMgr {
	
	public LedgerRunner runner = null;
	
	public EnvMgr(LedgerRunner runner)
	{
		this.runner = runner;
	}
	
	public MarketStatus 이전장상태 = MarketStatus.장준비;
	public MarketStatus 현재장상태 = MarketStatus.장준비;
	
	public Date 장시작시간 = new Date(0);
	public Date 장종료시간 = new Date(0);
	public Date 동시호가시간 = new Date(0);
	public Date 오버나잇시간 = new Date(0);
	public Date 오버나잇실행금지시간 = new Date(0);
	public Date 오버나잇5분전 = new Date(0);
	
	public Date 시스템정산 = new Date(0);
	
	public Long 강제휴일상태 = new Long(0);
	
	
	
	public String 업체전화1 = new String();
	public String 업체은행1 = new String();
	public String 업체은행계좌1 = new String();
	public String 업체은행계좌주1 = new String();
	public String 업체전화2 = new String();
	public String 업체은행2 = new String();
	public String 업체은행계좌2 = new String();
	public String 업체은행계좌주2 = new String();
	
	public Long ETF거래가능 = new Long(0);
	
	public Long 최소담보금 = new Long(500000);
	public Long 오버나잇가능 = new Long(1);
	
	//수수료
	public BigDecimal 주식거래수수료 = new BigDecimal("0.015");
	public BigDecimal 주식오버나잇수수료 = new BigDecimal("0.01");
	public BigDecimal 주식마감수수료 = new BigDecimal("0.01");
	public BigDecimal 주식취급수수료 = new BigDecimal("0.22");
	public BigDecimal 주식로스컷수수료 = new BigDecimal("0.2");
	public BigDecimal 주식거래세 = new BigDecimal("0.25");
	
	public BigDecimal ETF거래수수료 = new BigDecimal("0.015");
	public BigDecimal ETF오버나잇수수료 = new BigDecimal("0.01");
	public BigDecimal ETF마감수수료 = new BigDecimal("0.01");
	public BigDecimal ETF취급수수료 = new BigDecimal("0.22");
	public BigDecimal ETF로스컷수수료 = new BigDecimal("0.2");
	public BigDecimal ETF거래세 = new BigDecimal("0.25");
	
	public boolean 이전장시작전상태 = false;
	
	public String 현재시장상태 = new String();
	
	public String 시간체크 = new String();
	public String 휴일정보 = new String();
	
	public boolean CanOrder()
	{
		boolean bResult = false;
		if ( runner.env_mgr.현재장상태.equals(MarketStatus.동시호가) || runner.env_mgr.현재장상태.equals(MarketStatus.장운영) )
		{
			bResult = true;
		}
		return bResult;
	}
	
	public boolean 동시호가()
	{
		boolean bResult = false;
		if ( runner.env_mgr.현재장상태.equals(MarketStatus.동시호가) )
		{
			bResult = true;
		}
		return bResult;
	}
	
	public boolean CanOrderSettle()
	{
		boolean bResult = false;
		if ( runner.env_mgr.현재장상태.equals(MarketStatus.장운영) )
		{
			bResult = true;
		}
		return bResult;
	}
	
	public String CheckMarket()
	{
		SetupTime();
		String msg = new String();		
		//장시작전, 동시호가, 장중, 장종료
		switch ( runner.env_mgr.현재장상태 )
		{
			case 휴일 :
				msg = String.format("[%s] %s", runner.env_mgr.휴일정보, "금일은 휴일 또는 휴장입니다." );
				break;
			case 장준비 :
				msg = "장준비상태입니다.";
				break;
			case 장시작전 :
				msg = "장시작전입니다.";
				break;
			case 동시호가 :
				msg = "동시호가상태입니다.";
				break;
			case 장운영 :
				if ( 시간체크.equals("false") )
				{
					msg = "장운영중입니다.(개발자용)";
				} else {
					msg = "장운영중입니다.";
				}
				break;
			case 오버나잇 :
				msg = "오버나잇을 실행합니다.";
				break;
			case 장종료 :
				msg = "장종료되었습니다.";
				break;
			default :
				msg = "";
				break;
		}	
		return msg;
	}
	
	public String CheckMarketFormat( Date tm )
	{
		String msg = new String();	
		msg = CheckMarket();
		runner.redisTemplate.opsForValue().set("MARKET_STATUS", msg);
		String msg_result = utils.GetMessage ( tm, msg );
		return msg_result;
	}
	
	@Scheduled(cron="0 0 8 * * ?")
    public void CheckInitDB() {
		runner.PrintLogForce("===== 장시작전 정산처리 CRON START =====" );
		String new_date = utils.GetDate();
		runner.Initialization();
		//종목코드 INIT.
		runner.redisTemplate.opsForValue().set("SYSTEM_NOTICE", new_date);
		runner.PrintLogForce("===== 장시작전 정산처리 CRON END =====" );
	}
	
	@Scheduled(initialDelay = 1000, fixedRateString = "1000")
    public void CheckMarketStatus() {
        // 실행될 로직	
		//String time = utils.GetTime();
		//runner.PrintLogForce("CHECK ENV:" + time);
		if ( runner != null && runner.env_mgr != null )
		{
			if ( runner.env_mgr.장종료시간 != null && runner.env_mgr.장종료시간 != null && runner.env_mgr.동시호가시간 != null )
			{
				SetupTime();
				Date now = utils.GetNow();
			
				if ( runner.env_mgr.시간체크.equals("true") )
				{
					if ( !runner.env_mgr.현재장상태.equals(MarketStatus.휴일) )
					{
						if ( now.compareTo(runner.env_mgr.오버나잇5분전) > 0 )
						{
							runner.overnight_mgr.SendToOverNightMessageBefore5(runner.env_mgr.오버나잇시간);
						}
						
						if ( now.compareTo(runner.env_mgr.오버나잇시간) > 0 )
						{
							if ( now.compareTo(runner.env_mgr.오버나잇실행금지시간) < 0 )
							{
								synchronized (this) {
									runner.overnight_mgr.RunOverNight(now, false);
								}
							}
						} 
						//오버나잇 실행후 장종료 FLAG를 SETUP한다.
						if ( now.compareTo(runner.env_mgr.장종료시간) > 0 )
						{
							runner.matching_mgr.bAfterOpenMarket = false;
							runner.env_mgr.현재장상태 = MarketStatus.장종료;
							if ( !runner.env_mgr.현재장상태.equals(runner.env_mgr.이전장상태) )
							{
								runner.PrintLogForce ( CheckMarketFormat(now) );
							}
							runner.env_mgr.이전장상태 = runner.env_mgr.현재장상태;
						} else {
							if ( now.compareTo(runner.env_mgr.장시작시간) < 0 )
							{
								if ( now.compareTo(runner.env_mgr.동시호가시간) > 0 )
								{
									runner.matching_mgr.bAfterOpenMarket = false;
									runner.env_mgr.현재장상태 = MarketStatus.동시호가;
									if ( !runner.env_mgr.현재장상태.equals(runner.env_mgr.이전장상태) )
									{
										runner.PrintLogForce ( CheckMarketFormat(now) );
									}
									runner.env_mgr.이전장상태 = runner.env_mgr.현재장상태;
								} else {
									runner.env_mgr.현재장상태 = MarketStatus.장시작전;
									if ( !runner.env_mgr.현재장상태.equals(runner.env_mgr.이전장상태) )
									{
										runner.PrintLogForce ( CheckMarketFormat(now) );
									}
									runner.env_mgr.이전장상태 = runner.env_mgr.현재장상태;	
								}
								//시스템에 정산관련 메세지를 던진다. 1분간만 CHECK 한다.
								if ( now.compareTo(runner.env_mgr.시스템정산) > 0 )
								{
									boolean bSystemNotice = false;
									String new_date = utils.GetDate();
									Object save_date = runner.redisTemplate.opsForValue().get("SYSTEM_NOTICE");
									if ( save_date == null )
									{
										bSystemNotice = true;
									} else {
										if ( !new_date.equals(save_date.toString()) )
										{
											bSystemNotice = true;
										}
									}
									if ( bSystemNotice )
									{
										//전체 유저에 정산처리 메세지를 보낸다.
										//runner.user_mgr.user_list
										HashMap<String, Object>	data = new HashMap<String, Object>();
										data.put("tr", utils.MTS_SYSTEM_EXIT);
										data.put("message", "정산처리를 위하여 시스템을 종료합니다. 재접속후 이용하세요.");
										runner.Send_Response("tiger.pnl", data);
										
										runner.PrintLogForce("===== 장시작전 정산처리 SETUP START =====" );
										//정산처리
										Initialization();
										//종목코드 INIT.
										runner.stoploss_mgr.UnInstallAll();
										runner.redisTemplate.opsForValue().set("SYSTEM_NOTICE", new_date);
										runner.PrintLogForce("===== 장시작전 정산처리 SERUP END =====" );
									}
								}
							} else {
								runner.env_mgr.현재장상태 = MarketStatus.장운영;
								if ( !runner.env_mgr.현재장상태.equals(runner.env_mgr.이전장상태) )
								{
									runner.PrintLogForce ( CheckMarketFormat(now) );
								}
								runner.env_mgr.이전장상태 = runner.env_mgr.현재장상태;
								runner.matching_mgr.CheckOpenMatching();
								runner.losscut_mgr.RunLossCut();			//장운영중 로스컷 처리를 한다.
							}
						}	
					} else {
						if ( now.compareTo(runner.env_mgr.시스템정산) > 0 )
						{
							boolean bSystemNotice = false;
							String new_date = utils.GetDate();
							Object save_date = runner.redisTemplate.opsForValue().get("SYSTEM_NOTICE");
							if ( save_date == null )
							{
								bSystemNotice = true;
							} else {
								if ( !new_date.equals(save_date.toString()) )
								{
									bSystemNotice = true;
								}
							}
							if ( bSystemNotice )
							{
								//전체 유저에 정산처리 메세지를 보낸다.
								//runner.user_mgr.user_list
								HashMap<String, Object>	data = new HashMap<String, Object>();
								data.put("tr", utils.MTS_SYSTEM_EXIT);
								data.put("message", "정산처리를 위하여 시스템을 종료합니다. 재접속후 이용하세요.");
								runner.Send_Response("tiger.pnl", data);
								
								runner.PrintLogForce( new_date + "- 정산처리");
								//정산처리
								Initialization();
								//종목코드 INIT.
								runner.stoploss_mgr.UnInstallAll();
								runner.redisTemplate.opsForValue().set("SYSTEM_NOTICE", new_date);
							}
						}
						runner.env_mgr.현재장상태 = MarketStatus.휴일;
						if ( !runner.env_mgr.현재장상태.equals(runner.env_mgr.이전장상태) )
						{
							runner.PrintLogForce ( CheckMarketFormat(now) );
						}
						runner.env_mgr.이전장상태 = runner.env_mgr.현재장상태;
					}
				} else {
					runner.env_mgr.현재장상태 = MarketStatus.장운영;
					if ( !runner.env_mgr.현재장상태.equals(runner.env_mgr.이전장상태) )
					{
						runner.PrintLogForce ( CheckMarketFormat(now) );
					}
					runner.env_mgr.이전장상태 = runner.env_mgr.현재장상태;
				}
			}
		}
    }
	
	public void SetupTime()
	{
		if ( runner.system_env.size() > 0 )
		{
			String 장시작 = runner.system_env.get("ENV_MARKET_TIME_STOCK_START").toString();		
			String 장종료 = runner.system_env.get("ENV_MARKET_TIME_STOCK_END").toString();		
			String 정산시간 = "080000";
			
			if ( runner.system_env.get("ENV_SYSTEM_NOTICE") != null )
			{
				정산시간 = runner.system_env.get("ENV_SYSTEM_NOTICE").toString();
			}
			
			Integer 동시호가 = new Integer ( runner.system_env.get("ENV_MARKET_TIME_STOCK_BUY_ON_OPENING_BEFORE_MINUTES").toString() );		
			runner.env_mgr.장시작시간 = utils.GetTimeConvert(장시작);
			runner.env_mgr.장종료시간 = utils.GetTimeConvert(장종료);
			runner.env_mgr.오버나잇시간 = runner.env_mgr.장종료시간;
			runner.env_mgr.시스템정산 = utils.GetTimeConvert(정산시간);
			
			Calendar cal_ov = new GregorianCalendar(Locale.KOREA);
			cal_ov.setTime(runner.env_mgr.오버나잇시간);
			cal_ov.add(Calendar.MINUTE, -5 );
			runner.env_mgr.오버나잇5분전 = cal_ov.getTime();
			
			Calendar cal_ov_after = new GregorianCalendar(Locale.KOREA);
			cal_ov_after.setTime(runner.env_mgr.오버나잇시간);
			cal_ov_after.add(Calendar.MINUTE, 1 );
			runner.env_mgr.오버나잇실행금지시간 = cal_ov_after.getTime();	
			
			Calendar cal_ov_system_exit = new GregorianCalendar(Locale.KOREA);
			cal_ov_system_exit.setTime(runner.env_mgr.시스템정산);
			runner.env_mgr.시스템정산 = cal_ov_system_exit.getTime();
			
			//시스템정산
			
			Calendar cal = new GregorianCalendar(Locale.KOREA);
			cal.setTime(runner.env_mgr.장시작시간);
			cal.add(Calendar.MINUTE, ( 동시호가 * -1 ) );
			runner.env_mgr.동시호가시간 = cal.getTime();	
			Long tmp = new Long(runner.system_env.get("ENV_MARKET_HOLIDAY_STOCK").toString());
			runner.env_mgr.강제휴일상태 = tmp;		
			runner.env_mgr.시간체크 = runner.environment.getProperty("market.checktime");
			if ( runner.env_mgr.강제휴일상태.equals(Long.valueOf(1)) )
			{
				runner.env_mgr.현재장상태 = MarketStatus.휴일;
			} else {
				if ( IsHoliday() )
				{
					runner.env_mgr.현재장상태 = MarketStatus.휴일;
				} else {
					if ( runner.env_mgr.현재장상태.equals(MarketStatus.휴일) )
					{
						runner.env_mgr.현재장상태 = MarketStatus.장준비;
					}
				}
			}
		}
	}
	
	public boolean IsHoliday()
	{
		boolean bResult = false;
		if ( runner.environment.getProperty("market.checktime").equals("true") )
		{
			if ( runner.env_mgr.강제휴일상태.equals(Long.valueOf(1)) )
			{
				bResult = true;
			} else {
				Date date = utils.GetNow();	     
			    Calendar cal = Calendar.getInstance() ;
			    cal.setTime(date);		     
			    int dayNum = cal.get(Calendar.DAY_OF_WEEK);
			    if ( dayNum == 1 )
			    {
			    	runner.env_mgr.휴일정보 = "일요일";
			    	bResult = true;
			    } else if ( dayNum == 7 )
			    {
			    	runner.env_mgr.휴일정보 = "토요일";
			    	bResult = true;
			    } else {
					String now = utils.GetDate();
					Object info = runner.redisTemplate.opsForHash().get("HOLIDAY", now);
					if ( info != null )
					{
						runner.env_mgr.현재장상태 = MarketStatus.휴일;
						runner.env_mgr.휴일정보 = info.toString();
						bResult = true;
					} else {
						runner.env_mgr.휴일정보 = "";
					}
			    }
			}
		}
		return bResult;
	}
	
	public void LoadHoliday()
	{
		runner.redisTemplate.delete("HOLIDAY");
		ArrayList<HashMap<String, Object>> holiday = runner.mapper.GetHoliday();
		for ( int i=0; i<holiday.size(); i++ )
		{
			HashMap<String, Object> item = holiday.get(i);
			runner.redisTemplate.opsForHash().put("HOLIDAY", item.get("date").toString(), item.get("info") );
		}
	}
	
	public void ReloadEnvironment( boolean bReload )
	{
		runner.system_env.clear();
		ArrayList<HashMap<String, Object>> env = runner.mapper.GetEnvironment();
		for ( int i=0; i<env.size(); i++ )
		{
			HashMap<String, Object> item = env.get(i);
			runner.system_env.put(item.get("name").toString(), item.get("value").toString());	
			runner.redisTemplate.opsForHash().put("ENVIRONMENT", item.get("name").toString(), item.get("value").toString());
		}
		
		//매수손익계산 = "1";
		
		Set<String> sets = runner.system_env.keySet();
		Iterator<String> keys = sets.iterator();
		while ( keys.hasNext() )
		{
			//ENV_MARKET_TIME_STOCK_START
			String name = keys.next();
			String value = runner.system_env.get(name).toString();
			if ( name.equals("ENV_MARKET_TIME_STOCK_START") )
			{
				Date tmp = utils.GetTimeConvert(value);
				runner.env_mgr.장시작시간 = tmp;
				runner.env_mgr.동시호가시간 = tmp;
			} else if ( name.equals("ENV_MARKET_TIME_STOCK_END") )
			{
				Date tmp = utils.GetTimeConvert(value);
				runner.env_mgr.장종료시간 = tmp;
			} else if ( name.equals("ENV_MARKET_HOLIDAY_STOCK") )
			{
				Long tmp = new Long(value);
				runner.env_mgr.강제휴일상태 = tmp;				
			} else if ( name.equals("ENV_CALL_TEL1") )
			{
				runner.env_mgr.업체전화1 = value;
			} else if ( name.equals("ENV_BANK") )
			{
				runner.env_mgr.업체은행1 = value;
			} else if ( name.equals("ENV_BANK_ACCOUNT") )
			{
				runner.env_mgr.업체은행계좌1 = value;				
			} else if ( name.equals("ENV_BANK_OWNER") )
			{
				runner.env_mgr.업체은행계좌주1 = value;				
			} else if ( name.equals("ENV_BANK2") )
			{
				runner.env_mgr.업체은행2 = value;
			} else if ( name.equals("ENV_CALL_TEL2") )
			{
				runner.env_mgr.업체전화2 = value;				
			} else if ( name.equals("ENV_BANK_ACCOUNT2") )
			{
				runner.env_mgr.업체은행계좌2 = value;				
			} else if ( name.equals("ENV_BANK_OWNER2") )
			{
				runner.env_mgr.업체은행계좌주2 = value;						
			} else if ( name.equals("ENV_DEAL_ETF") )
			{
				Long tmp = new Long(value);
				runner.env_mgr.ETF거래가능 = tmp;				
			} else if ( name.equals("ENV_DEPOSIT_PER_STOCK") )
			{
				Long tmp = new Long(value);
				runner.env_mgr.최소담보금 = tmp;				
			} else if ( name.equals("ENV_ENABLE_OVERNIGHT_STOCK") )
			{
				Long tmp = new Long(value);
				runner.env_mgr.오버나잇가능 = tmp;				
			} 
			//주식수수료
			else if ( name.equals("ENV_STOCK_FEE_TRADE") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.주식거래수수료 = tmp;				
			} else if ( name.equals("ENV_STOCK_FEE_OVER") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.주식오버나잇수수료 = tmp;				
			} else if ( name.equals("ENV_STOCK_FEE_END") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.주식마감수수료 = tmp;				
			} else if ( name.equals("ENV_STOCK_FEE_LOAN") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.주식취급수수료 = tmp;				
			} else if ( name.equals("ENV_STOCK_FEE_LOSSCUT") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.주식로스컷수수료 = tmp;				
			} else if ( name.equals("ENV_STOCK_TAX") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.주식거래세 = tmp;				
			}
			//ETF수수료
			else if ( name.equals("ENV_ETF_FEE_TRADE") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.주식거래수수료 = tmp;				
			} else if ( name.equals("ENV_ETF_FEE_OVER") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.ETF오버나잇수수료 = tmp;				
			} else if ( name.equals("ENV_ETF_FEE_END") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.ETF마감수수료 = tmp;				
			} else if ( name.equals("ENV_ETF_FEE_LOAN") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.ETF취급수수료 = tmp;				
			} else if ( name.equals("ENV_ETF_FEE_LOSSCUT") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.ETF로스컷수수료 = tmp;				
			} else if ( name.equals("ENV_ETF_TAX") )
			{
				BigDecimal tmp = new BigDecimal(value);
				runner.env_mgr.ETF거래세 = tmp;				
			} else if ( name.equals("ENV_OVER_TIME") )
			{
				Date tmp = utils.GetTimeConvert(value);
				runner.env_mgr.오버나잇시간 = tmp;
			} else if ( name.equals("ENV_SYSTEM_NOTICE") )
			{
				Date tmp = utils.GetTimeConvert(value);
				runner.env_mgr.시스템정산 = tmp;
			}
			//상하한가 제한 등
		}
		
		if ( runner.system_env.get("ENV_FILLED") == null )
		{
			runner.redisTemplate.opsForHash().put("ENVIRONMENT", "ENV_FILLED", "0");
			runner.mapper.InsertEnv("ENV_FILLED", "0");
		}
		
		if ( runner.system_env.get("ENV_BUY_PROFIT") == null )
		{
			runner.redisTemplate.opsForHash().put("ENVIRONMENT", "ENV_BUY_PROFIT", "0");
			runner.mapper.InsertEnv("ENV_BUY_PROFIT", "0");
		}
		
		if ( runner.system_env.get("ENV_SYSTEM_NOTICE") == null )
		{
			Date tmp = utils.GetTimeConvert("081000");
			runner.env_mgr.시스템정산 = tmp;
		} else {
			Date tmp = utils.GetTimeConvert(runner.system_env.get("ENV_SYSTEM_NOTICE").toString());
			runner.env_mgr.시스템정산 = tmp;
		}
		
		if ( runner.system_env.get("ENV_MAX_ITEM") == null )
		{
			runner.redisTemplate.opsForHash().put("ENVIRONMENT", "ENV_MAX_ITEM", "0");
			runner.mapper.InsertEnv("ENV_MAX_ITEM", "0");			
		}
		
		SetupTime();
		if ( bReload )
			runner.PrintLogForce ( "------------------환경설정 변경-------------------");
		runner.PrintLogForce ( "장시작시간:" + runner.env_mgr.장시작시간.toString() );
		runner.PrintLogForce ( "동시호가시간:" + runner.env_mgr.동시호가시간.toString() );
		runner.PrintLogForce ( "장종료시간:" + runner.env_mgr.장종료시간.toString() );
		runner.PrintLogForce ( "오버나잇시간:" + runner.env_mgr.오버나잇시간.toString() );
		runner.PrintLogForce ( "오버나잇실행금지시간:" + runner.env_mgr.오버나잇실행금지시간.toString() );
		runner.PrintLogForce ( "시스템정산시간:" + runner.env_mgr.시스템정산.toString() );
		if ( !IsHoliday() )
		{
			runner.PrintLogForce ( runner.env_mgr.장시작시간 + ":휴장일이 아닙니다." );
		} else {
			runner.PrintLogForce ( runner.env_mgr.장시작시간 + ":휴장입니다." );
			runner.env_mgr.현재장상태 = MarketStatus.휴일;
		}	
		
		/*
		if ( runner.env_mgr.ENV_FILLED.equals(Long.valueOf(0)) )
		{
			runner.PrintLogForce ( "체결로직:전량체결" );
		} else {
			runner.PrintLogForce ( "체결로직:부분체결" );
		}
		
		if ( runner.env_mgr.매수손익계산.equals("1") )
		{
			runner.PrintLogForce ( "매수손익계산:사용" );
		} else {
			runner.PrintLogForce ( "매수손익계산:미사용" );
		}
		*/
	}

	public void Initialization()
	{
		Long nResult = runner.mapper.InitDB("ALPHA");
		if ( nResult > 0 )
		{
			runner.PrintLogForce("주문 DB 초기화 OK.");
		}
		LoadHoliday();
		ReloadEnvironment( false );
	}
}

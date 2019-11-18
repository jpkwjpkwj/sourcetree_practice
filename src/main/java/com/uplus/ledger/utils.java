package com.uplus.ledger;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

enum MarketStatus {
    장준비, 휴일, 장시작전, 동시호가, 장운영, 오버나잇, 장종료
}

public class utils {
		public static Long OS_NEW = new Long(0);
	public static Long OS_CORRECTION = new Long(1);
	public static Long OS_CANCEL = new Long(2);
	public static Long OS_CONTRACT = new Long(3);
	public static Long OS_REJECT = new Long(4);
	public static Long OS_OVERNIGHT = new Long(5);
	
	public static String	KOSPI 	= "0";
	public static String	KOSDAQ 	= "1";

	public static long	NEW_ORDER = 0;
	public static long	REPLACE_ORDER = 1;
	public static long	REMOVE_ORDER = 2;
	public static long	FILLED_ORDER = 3;
	public static long	REJECT_ORDER = 4;
	public static long	OVERNIGHT_ORDER = 5;
	public static long	LOSSCUT_ORDER = 6;
	public static long	STOP_PLUS = 7;
	public static long	STOP_MINUS = 8;
	
	public static long	동시호가주문_지정가 = 10;
	public static long	동시호가주문_시장가 = 11;
	
	public static Long	ORDER_ACTOR_HTS = Long.valueOf(0);		//HTS ACTION
	public static Long	ORDER_ACTOR_MIT = Long.valueOf(1);		//HTS ACTION
	public static Long	ORDER_ACTOR_STOP_MINUS = Long.valueOf(2);	//HTS ACTION
	public static Long	ORDER_ACTOR_STOP_PLUS = Long.valueOf(3);	//HTS ACTION
	public static Long	ORDER_ACTOR_LOSSCUT = Long.valueOf(4);	//시스템 ACTION -> 오버나잇, 로스컷, 예약주문
	public static Long	ORDER_ACTOR_EXCHANGE = Long.valueOf(5);	//거래소 ACTION -> 체결
	public static Long	ORDER_ACTOR_MANAGER = Long.valueOf(6);	//관리자 ACTION
	public static Long	ORDER_ACTOR_MTS = Long.valueOf(7);		//HTS ACTION
	public static Long	ORDER_ACTOR_WTS = Long.valueOf(8);		//WTS ACTION

	public static Long	ACTOR_USER = Long.valueOf(1);		//HTS ACTION
	public static Long	ACTOR_MANAGER = Long.valueOf(2);		//HTS ACTION
	public static Long	ACTOR_EXCHANGE = Long.valueOf(3);	//HTS ACTION
	public static Long	ACTOR_SYSTEM = Long.valueOf(4);	//HTS ACTION
	
	public static long	지정가 = 0;
	public static long	시장가 = 1;
	public static long	동시호가지정가 = 10;
	public static long	동시호가시장가 = 11;
	
	public static long	POSITION_BUY = 66;
	public static long	POSITION_SELL = 83;
	
    public static final String QUEUE_FEED = "stock.feed.queue.request";
    public static final String QUEUE_DEAD_FEED = "stock.feed.dead.request";
    public static final String EXCHANGE_FEED = "stock.feed.exchange.request";
    public static final String ROUTING_KEY = "stock.feed.routingkey";
    
    public static final String MTS_SISE					= "2001";
    public static final String MTS_ORDER_NEW_WTS		= "3001";	//신규주문
    public static final String MTS_ORDER_CORRECTION_WTS	= "3002";	//정정주문
    public static final String MTS_ORDER_CANCEL_WTS 	= "3003";	//취소주문
    public static final String MTS_ORDER_LIQUID_WTS		= "3004";	//청산주문
    public static final String MTS_REQ_ENABLE_ORDER_VOLUME	= "3005";	//가능수량
    public static final String MTS_LOAN					= "3006";	//대출처리
    public static final String MTS_OVERNIGHT			= "3007";	//오버나잇
    public static final String MTS_DEPOSIT				= "3008";	//입금처리
    public static final String MTS_WITHDRAW				= "3009";	//출금처리
    public static final String MTS_STOPLOSS				= "3010";
    public static final String MTS_REGISTER_MEMBER		= "3011";
    public static final String MTS_LOGIN				= "3012";
    public static final String MTS_LOGOUT				= "3013";
    public static final String MTS_ORDER_CALCELALL_WTS	= "3014";
    public static final String MTS_REGISTER_CLIENT		= "3015";
    public static final String MTS_REGISTER_OVERNIGHT	= "3016";
    public static final String MTS_REGISTER_HOLIDAY		= "3017";
    public static final String MTS_MODIFY_OVERNIGHT		= "3018";
    public static final String MTS_ORDER_NEW_FORCE_CONTRACT= "3019";
    public static final String MTS_ORDER_REGISTER_SISE		= "3020";
    
    public static final String MTS_SET_ENVIRONMENT		= "3021";
    public static final String MTS_INIT_USER_INFO		= "3022";
    public static final String MTS_UPDATE_USER_INFO		= "3023";
    public static final String MTS_UPDATE_OVERNIGHT		= "3024";
    public static final String MTS_UPDATE_BALANCE		= "3025";
    public static final String MTS_UPDATE_LOAN			= 	"3026";
    
    public static final String MTS_CLEAR_USER_DATA		= 	"3027";
    public static final String MTS_CLEAR_LEDGER_DATA	= 	"3028";
    
    public static final String MTS_FORCE_OVERNIGHT		= 	"3029";
    
    public static final String MTS_UPDATE_USER_MAX_ITEM		= "3030";
    
    public static final String MTS_MANAGER_START			= "5000";
    public static final String MTS_MANAGER_NEW_NOTICE		= "5001";
    public static final String MTS_MANAGER_BANKING			= "5002";
    public static final String MTS_MANAGER_BANKING_CONFIRM	= "5003";
    public static final String MTS_MANAGER_NEW_CLIENT		= "5004";
    public static final String MTS_MANAGER_UPDATE_CLIENT	= "5005";
    public static final String MTS_FORCE_DISCONNECT			= "5006";
    public static final String MTS_RELOAD_EXCEPT_SYMBOL		= "5007";
    public static final String MTS_BROADCAST_EXCEPT_SHORT  	= "5008";
    
    public static final String MTS_EMERGENCY_MESSAGE  		= "5100";
    
    public static final String MTS_SYSTEM_EXIT				= "5999";
    
    public static final String MTS_LEDGER_UPDATE_POSITION	= "6001";
    public static final String MTS_LEDGER_INSERT_POSITION	= "6002";
    
    public static final String MTS_ORDER_STATUS				= "8001";
    
    public static final String MTS_UPDATE_OUTSTANDINGS	 	= "0001";
    public static final String MTS_UPDATE_POSITIONS		 	= "0002";
    public static final String MTS_UPDATE_EVALUATION	 	= "0003";
    public static final String MTS_UPDATE_STOPLOSS		 	= "0004";
     
    public static final String MTS_UPDATE_REAL_DATA		 	= "4001";
    
    public static final String TR_MATCHING		 			= "7001";
    
    public static final String ORDER_KEY = "{367016CD-295B-4B33-8FC5-46FDC9E4B873}";
    
	public static Long toNumber(String number) {
		number = number.replace(",", "");
		if ( number.equals("") )
		{
			number = "0";
		}
		Long num = new Long(number);
		return num;
	}	
	
	public static BigDecimal toNumberDecimal(String number) {
		number = number.replace(",", "");
		if ( number.equals("") )
		{
			number = "0";
		}
		BigDecimal num = new BigDecimal(number);
		return num;
	}	
	
	public static String toNumFormat(String number) {
		  Integer num = new Integer(number);
		  DecimalFormat df = new DecimalFormat("#,###");
		  return df.format(num);
	}	
	
	public static String toDecimalFormat(String number) {
		  Double num = new Double(number);
		  DecimalFormat df = new DecimalFormat("#,###.##");
		  return df.format(num);
	}
	
	static public long GetTimeStamp()
	{
		Calendar c = Calendar.getInstance();
		long timestamp = c.getTimeInMillis();				
		return timestamp;		
	}
	
	static public String GetDate ()
	{
		long time = GetTimeStamp();
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd");
		return formatter.format(time);
	}
	
	static public String GetDate2 ()
	{
		long time = GetTimeStamp();
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(time);
	}
	
	static public String GetTime ()
	{
		long time = GetTimeStamp();
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
		return formatter.format(time);
	}
	
	static public String GetDateTime ()
	{
		long time = GetTimeStamp();
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return formatter.format(time);
	}
	
	static public String GetDateKorean (Date now)
	{
		long time = now.getTime();
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("HH시mm분ss초");
		return formatter.format(time);
	}
	
	static void Stanby ( int Second )
	{
		try {
			Thread.sleep( Second );
		} catch ( InterruptedException e )
		{
			
		}
	}
	
	static public String GetTime ( String day )
	{
			java.sql.Timestamp t = java.sql.Timestamp.valueOf(day);
			String time = new String();
			SimpleDateFormat df = new SimpleDateFormat( "HH:mm:ss.SSS" , Locale.KOREA );
			time = df.format(new Date(t.getTime()));
			return time;
	}
	
	static public Date GetTimeConvert ( String time )
	{		
		long now = GetTimeStamp();
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd");
		time = formatter.format(now) + time;				
		SimpleDateFormat dt = new SimpleDateFormat("yyyyMMddhhmmss", Locale.KOREA); 	
		try {
			Date date = dt.parse(time);
			return date;
		} catch ( Exception e )
		{
			
		}
		return null;
	}
	
	static public Date GetNow()
	{
		Calendar c = Calendar.getInstance();
		long timestamp = c.getTimeInMillis();		
		Date d = new Date(timestamp);
		return d;		
	}
	
	static public String GetMessage ( Date date, String msg )
	{
		String result = new String();
		result = String.format("[%s] %s", date.toString(), msg );
		return result;
	}
	
	static public Long GetOrderActor( String actor )
	{
		Long result = new Long(0);
		if ( actor.equals("HTS") )
		{
			result = ORDER_ACTOR_HTS;
		} else if ( actor.equals("MTS") )
		{
			result = ORDER_ACTOR_MTS;
		} else if ( actor.equals("MANAGER") )
		{
			result = ORDER_ACTOR_MANAGER;
		} else if ( actor.equals("EXCHANGE") )
		{
			result = ORDER_ACTOR_EXCHANGE;
		} else if ( actor.equals("SYSTEM") )
		{
			result = ORDER_ACTOR_LOSSCUT;
		} else if ( actor.equals("WTS") )
		{
			result = ORDER_ACTOR_WTS;
		}
		return result;
	}
	
	static public Long GetActor( String actor )
	{
		Long result = new Long(0);
		if ( actor.toUpperCase().equals("HTS") || actor.toUpperCase().equals("MTS") )
		{
			result = ACTOR_USER;
		} else if ( actor.toUpperCase().equals("MANAGER") )
		{
			result = ACTOR_MANAGER;
		} else if ( actor.toUpperCase().equals("EXCHANGE") )
		{
			result = ACTOR_EXCHANGE;
		} else if ( actor.toUpperCase().equals("SYSTEM") )
		{
			result = ACTOR_SYSTEM;
		}
		return result;
	}
	
	static public String GetHogaType( Long HogaTypea )
	{
		String result = new String();
		if ( HogaTypea.equals(utils.지정가))
		{
			result = "지정가";
		} else if ( HogaTypea.equals(utils.시장가))
		{
			result = "시장가";
		} else if ( HogaTypea.equals(utils.동시호가지정가))
		{
			result = "동시호가지정가";
		} else if ( HogaTypea.equals(utils.동시호가시장가))
		{
			result = "동시호가시장가";
		}
		return result;
	}
	
	static public String GetMsgOrder ( Order order )
	{
		String msg = new String();
		if ( order.PositionTypeID.equals(utils.POSITION_BUY))
		{
			msg = String.format("[매수(%s)][%s][주문가:%d][체결가:%d][주문수량:%d][체결수량:%d]", GetHogaType(order.HogaTypeID), order.code, order.priceOrder, order.priceContract, order.volOrder, order.volContract );
		} else if ( order.PositionTypeID.equals(utils.POSITION_SELL))
		{
			msg = String.format("[매도(%s)][%s][주문가:%d][체결가:%d][주문수량:%d][체결수량:%d]", GetHogaType(order.HogaTypeID), order.code, order.priceOrder, order.priceContract, order.volOrder, order.volContract );
		}
		return msg;
	}
	
	static public Date GetDateConvert ( String time )
	{		
		SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd", Locale.KOREA); 	
		try {
			Date date = dt.parse(time);
			return date;
		} catch ( Exception e )
		{
			
		}
		return null;
	}
	
	static public Long GetOverNightDays( Set<String> holiday, String date, Long days )
	{
		Long nResult = new Long(0);
		if ( days == 0 )
		{
			nResult = Long.valueOf(-1);
		} else {
			
			date = date.replace("-", "");
			Long nCount = new Long(0);
			Date 매수일 = utils.GetDateConvert(date);
			String 오늘 = utils.GetDate();
	
			Calendar cal = new GregorianCalendar(Locale.KOREA);
			cal.setTime(매수일);
	
			String 오버나잇일자 = new String();
	
			java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd");
			String time = formatter.format(매수일);
			
			while ( days > nCount )
			{
				boolean bHoliday = false;	
				cal.add(Calendar.DAY_OF_YEAR, 1);
				매수일 = cal.getTime();
				time = formatter.format(매수일);	
		
				
				int nWeek = cal.get ( Calendar.DAY_OF_WEEK );
				if ( nWeek == 1 || nWeek == 7 )
				{
					//System.out.println("휴일:" + time);
	    			매수일 = cal.getTime();
	    			bHoliday = true;
				} else {
					매수일 = cal.getTime();
	        		Iterator<String> itr = holiday.iterator();
	        		while ( itr.hasNext() )
	        		{
	        			if ( itr.next().equals(time) )
	        			{
	        				bHoliday = true;
	        				//System.out.println("휴일:" + time);
	        				break;
	        			}
	        		}
				}
				time = formatter.format(매수일);
	    		if ( !bHoliday )
	    		{
	    			if ( Long.valueOf(time) > Long.valueOf(오늘) )
	    			{
	    				nResult ++;
	    			}
	    			nCount ++;
	
	    			오버나잇일자 = time;
	    			//System.out.println("Count:" + nCount + "/" + "오버잔일:" + nResult + ":" + "일자:" + time);
	    		}
			}
			//System.out.println("오버나잇일자:" + 오버나잇일자);
			date = 오버나잇일자;
		}
		return nResult;
	}
	
	static public Long GetPriceTick ( String marketId, Long price )
	{
		Long pricetick = new Long(0);
		if ( marketId.equals(utils.KOSPI) )	//코스피
		{
			if ( price < 1000 )
			{
				pricetick = Long.valueOf(1);
			} else if ( price > 1000 && price <= 5000 )
			{
				pricetick = Long.valueOf(5);
			} else if ( price > 5000 && price <= 10000 )
			{
				pricetick = Long.valueOf(10);
			} else if ( price > 10000 && price <= 50000 )
			{
				pricetick = Long.valueOf(50);
			} else if ( price > 50000 && price <= 100000 )
			{
				pricetick = Long.valueOf(100);
			} else if ( price > 100000 && price <= 500000 )
			{
				pricetick = Long.valueOf(500);
			} else if ( price > 500000 )
			{
				pricetick = Long.valueOf(1000);
			}
		} else if ( marketId.equals(utils.KOSDAQ) )	//코스피
		{
			if ( price < 1000 )
			{
				pricetick = Long.valueOf(1);
			} else if ( price > 1000 && price <= 5000 )
			{
				pricetick = Long.valueOf(5);
			} else if ( price > 5000 && price <= 10000 )
			{
				pricetick = Long.valueOf(10);
			} else if ( price > 10000 && price <= 50000 )
			{
				pricetick = Long.valueOf(50);
			} else if ( price > 50000 )
			{
				pricetick = Long.valueOf(100);
			}
		}
		return pricetick;
	}
}

package com.uplus.ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.reflect.TypeToken;
	
public class Position {
	
	public LedgerRunner runner = null;
	
	public Long ordNum = new Long("0");
	public String time = new String();
	public String code = new String();
	public String name = new String();
	public String id = new String();
	public Long PositionTypeID = new Long("0");
	public Long volume = new Long("0");
	public Long price = new Long("0");
	public Long ov = new Long("0");
	public Long profit = new Long("0");
	public Long MerchandiseTypeID = new Long("0");
	public Long MarketTypeID = new Long("0");
	public String ovtime = new String();;
	public Long currprice = new Long("0");
	public Long amount = new Long("0");
	public Long realamount = new Long("0");
	public Long remaining_ov = new Long("0");
	
	public BigDecimal 매수비율 = new BigDecimal("0");
	public BigDecimal 매수비율금액 = new BigDecimal("0");
	
	public Position ( LedgerRunner runner )
	{
		this.runner = runner;
	}
	
	public void SetAmount()
	{
		this.amount = this.price * this.volume;
	}
	
	public Position ( LedgerRunner runner, HashMap<String, Object> object )
	{
		this.runner = runner;
		SetPosition ( object );
	}
	
	public Position ( Position pos )
	{
		try {
			this.runner = pos.runner;
			this.ordNum = pos.ordNum;
			this.time = pos.time;
			this.code = pos.code;
			this.name = pos.name;
			this.id = pos.id;
			this.PositionTypeID = pos.PositionTypeID;
			this.volume = pos.volume;
			this.price = pos.price;
			this.ov = pos.ov;
			this.profit = pos.profit;
			this.MerchandiseTypeID = pos.MerchandiseTypeID;
			this.MarketTypeID = pos.MarketTypeID;
			this.ovtime = pos.ovtime;
			this.currprice = pos.currprice;
			this.amount = pos.amount;
			this.realamount = pos.realamount;
			this.remaining_ov = pos.remaining_ov;	
			this.amount = pos.price * pos.volume;
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)Position-1-ERROR");
		}
	}
	
	public void SetPosition ( HashMap<String, Object> object )
	{
		try { 
			if ( object.get("ordnum") != null )
			{
				BigDecimal tmp_ordNum = new BigDecimal(object.get("ordnum").toString());
				ordNum = tmp_ordNum.longValue();
			}
			
			if ( object.get("id") != null )
			{
				id = object.get("id").toString();
			}
			
			if ( object.get("time") != null )
			{
				time = object.get("time").toString();
			} else {
				time = utils.GetTime();
			}
			
			if ( object.get("symbol") != null )
				code = object.get("symbol").toString();
			
			if ( object.get("code") != null )		
			{
				code = object.get("code").toString();
			}
			if ( object.get("PositionTypeID") != null )
			{
				if ( object.get("PositionTypeID").toString().equals("매수") )
				{
					PositionTypeID = Long.valueOf(utils.POSITION_BUY);
				} else if ( object.get("PositionTypeID").toString().equals("매도") )
				{
					PositionTypeID = Long.valueOf(utils.POSITION_SELL);
				} else {
					BigDecimal tmp_PositionTypeID = new BigDecimal(object.get("PositionTypeID").toString());
					PositionTypeID = tmp_PositionTypeID.longValue();
				}
			}
			if ( object.get("MerchandiseTypeID") != null )
			{
				BigDecimal tmp_MerchandiseTypeID = new BigDecimal(object.get("MerchandiseTypeID").toString());
				MerchandiseTypeID = tmp_MerchandiseTypeID.longValue();
			}
			if ( object.get("MarketTypeID") != null )
			{
				BigDecimal tmp_MarketTypeID = new BigDecimal(object.get("MarketTypeID").toString());
				MarketTypeID = tmp_MarketTypeID.longValue();
			}
			if ( object.get("volume") != null )
			{
				BigDecimal tmp_volume = new BigDecimal(utils.toNumber(object.get("volume").toString()));
				volume = tmp_volume.longValue();
			}
	
			if ( object.get("price") != null )
			{		
				BigDecimal tmp_price = new BigDecimal(utils.toNumber(object.get("price").toString()));
				price = tmp_price.longValue();
			}
			
			if ( object.get("ov") != null )
			{			
				BigDecimal tmp_ov = new BigDecimal(utils.toNumber(object.get("ov").toString()));
				ov = tmp_ov.longValue();
			}
			
			if ( object.get("ovtime") != null )
			{				
				ovtime = object.get("ovtime").toString();		
			}
			
			amount = volume * price;		
			name = runner.GetSymbolValue(code, "name");
			
			remaining_ov = utils.GetOverNightDays(runner.holidays, ovtime, ov);
			
			String tmp_currprice = runner.GetSymbolValue(code, "price");
			if ( tmp_currprice != null )
			{
				currprice = Long.valueOf(utils.toNumber(tmp_currprice));
				if ( currprice.equals(Long.valueOf(0)))
				{
					tmp_currprice = runner.GetSymbolValue(code, "jnilclose");
					currprice = Long.valueOf(utils.toNumber(tmp_currprice));
				}
			}
			
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(EXCEPTION)SetPosition-1:" +  e.getMessage() );
		}
	}
	
	public void SetPosition ( Order order )
	{
		try { 
			ordNum = order.ordNum;
			time = utils.GetDateTime();
			code = order.code;
			name = runner.GetSymbolValue(code, "name");
			id = order.id;
			PositionTypeID = order.PositionTypeID;
			MerchandiseTypeID = order.MerchandiseTypeID;
			MarketTypeID = order.MarketTypeID;
			ovtime = utils.GetDate2();
			
			User_Info user_info = runner.user_mgr.GetUserInfo(order.id);
			if ( user_info != null )
			{
				ov = user_info.OvernightDays;
				remaining_ov = utils.GetOverNightDays(runner.holidays, ovtime, ov);
			}	
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(EXCEPTION)SetPosition-2:" +  e.getMessage() );
		}
	}
	
	public Long SetProfit ( String symbol, HashMap<String, Object> item, boolean bSave )
	{
		try { 
			if ( item != null && item.get("price") != null )
			{
				if ( item.get("name") != null )
				{
					name = item.get("name").toString();
				}
				if ( item.get("price") != null )
				{
					Long tmp_currprice = utils.toNumber(item.get("price").toString());
					currprice = tmp_currprice;
					if ( currprice.equals(Long.valueOf(0)))
					{
						tmp_currprice = utils.toNumber(item.get("jnilclose").toString());
						currprice = tmp_currprice;
					}
				}			
				profit = ( currprice - price ) * volume;
				realamount = volume * currprice;	
				if ( bSave )
				{
					String data = toJSON();
					runner.redisTemplate.opsForHash().put("CONTRACT-" + id, symbol, data);
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(EXCEPTION)SetProfit-2:" +  e.getMessage() );
		}
		return Long.valueOf(-1);
	}
	
	public Long SetProfit ( boolean bNewPosition, boolean bSave )
	{
		try {
			String tmp_currprice = runner.GetSymbolValue(code, "price");
			if ( tmp_currprice != null )
			{
				currprice = Long.valueOf(utils.toNumber(tmp_currprice));
				if ( currprice.equals(Long.valueOf(0)))
				{
					tmp_currprice = runner.GetSymbolValue(code, "jnilclose");
					currprice = Long.valueOf(utils.toNumber(tmp_currprice));
				}
			}
			profit = ( currprice - price ) * volume;
			realamount = volume * currprice;
			if ( bSave )
			{
				String data = toJSON();
				runner.redisTemplate.opsForHash().put("CONTRACT-" + id, code, data);
			}
			try {
				if ( !bNewPosition )
				{
					Long nResult = runner.mapper.GetPosition(id, code);
					if ( nResult > Long.valueOf( 0 ))
					{
						nResult = runner.mapper.UpdateProfit ( id, code, profit, currprice.longValue() );
						if ( nResult > 0 )
						{
							return nResult;
						} else {
							runner.PrintLogForce("SetProfit Update Error");
						}
					}
				}
			} catch ( Exception e )
			{
				runner.PrintLogForce("(EXCEPTION)SetProfit-1-E:" + e.getMessage());
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)SetProfit-2-E:" + e.getMessage());
		}
		return Long.valueOf(-1);
	}
	
	public void SetProfit ( )
	{
		try {
			String tmp_currprice = runner.GetSymbolValue(code, "price");
			if ( tmp_currprice != null )
			{
				currprice = Long.valueOf(utils.toNumber(tmp_currprice));
				if ( currprice.equals(Long.valueOf(0)))
				{
					tmp_currprice = runner.GetSymbolValue(code, "jnilclose");
					currprice = Long.valueOf(utils.toNumber(tmp_currprice));
				}
			}
			profit = ( currprice - price ) * volume;
			realamount = volume * currprice;
			String data = toJSON();
			runner.redisTemplate.opsForHash().put("CONTRACT-" + id, code, data);
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)SetProfit-2-E:" + e.getMessage());
		}
	}
	
	public long InsertPositionDB( Order order )
	{
		long nResult = 0;	
		try {
			Object position_item = runner.redisTemplate.opsForHash().get("CONTRACT-" + order.id, order.code);
			if ( position_item != null )
			{
				//기존의 포지션에 REPLACE
				HashMap<String, Object> object = runner.gson.fromJson(position_item.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
				if ( object != null )
				{
					SetPosition( object );
					Long tmpcurr = new Long(utils.toNumber(runner.GetSymbolValue(code, "price")));
					currprice = tmpcurr;	
					volume = volume + order.volContract;
					amount = amount + ( order.volContract * Long.valueOf(order.priceContract));		
					realamount = volume * currprice;
					if ( volume != 0 && amount != 0 )
					{
						BigDecimal tmp_avg_price = new BigDecimal("0");
						tmp_avg_price = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(volume), RoundingMode.FLOOR);
						price = tmp_avg_price.longValue();
					}			
					
					SetProfit( false, false );
					Long nCount = runner.mapper.GetPosition(id, code);
					if ( nCount > 0 )
					{	
						nResult = runner.mapper.UpdatePosition(id, code, volume, currprice, price, profit);
						if ( nResult > 0 )
						{						
							String data = toJSON();
							runner.redisTemplate.opsForHash().put("CONTRACT-" + id, order.code, data);
						}
					} else {
						nResult = runner.mapper.NewPosition(ordNum, time, code, id, PositionTypeID, volume, price, MerchandiseTypeID, Long.valueOf("0"), Long.valueOf("0"), "ALPHA", ov, profit, ovtime, currprice);
						if ( nResult > 0 )
						{						
							String data = toJSON();
							runner.redisTemplate.opsForHash().put("CONTRACT-" + order.id, order.code, data);
						}					
					}
				}
			} else {
				//신규포지션인상태			
				User_Info user_info = runner.user_mgr.GetUserInfo(order.id);
				if ( user_info != null )
				{			
					time = utils.GetDateTime();
					ordNum = order.ordNum;
					id = order.id;
					code = order.code;
					name = runner.GetSymbolValue(code, "name");
					PositionTypeID = order.PositionTypeID;
					Long tmpcurr = new Long(utils.toNumber(runner.GetSymbolValue(code, "price")));
					currprice = tmpcurr;
					
					ov = user_info.OvernightDays;
					ovtime = utils.GetDate2();
					MerchandiseTypeID = order.MerchandiseTypeID;
					
					volume = volume + order.volContract;
					amount = amount + ( order.volContract * Long.valueOf(order.priceContract));			
					realamount = volume * currprice;
					if ( volume != 0 && amount != 0 )
					{
						BigDecimal tmp_avg_price = new BigDecimal("0");
						tmp_avg_price = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(volume), RoundingMode.FLOOR);
						price = tmp_avg_price.longValue();
					}
					
					String market = runner.GetSymbolValue(order.code, "market");
					MerchandiseTypeID = Long.valueOf(market);
					
					SetProfit( true, false );
					
					Long nCount = runner.mapper.GetPosition(id, code);
					if ( nCount > 0 )
					{	
						nResult = runner.mapper.UpdatePosition(id, code, volume, currprice, price, profit);
						if ( nResult > 0 )
						{						
							String data = toJSON();
							runner.redisTemplate.opsForHash().put("CONTRACT-" + id, order.code, data);
						}
					} else {
						nResult = runner.mapper.NewPosition(ordNum, time, code, id, PositionTypeID, volume, price, MerchandiseTypeID, Long.valueOf("0"), Long.valueOf("0"), "ALPHA", ov, profit, ovtime, currprice);
						if ( nResult > 0 )
						{						
							String data = toJSON();
							runner.redisTemplate.opsForHash().put("CONTRACT-" + order.id, order.code, data);
						}		
					}
					//runner.order_mgr.SaveLedgerFeed(order.id, order.code);
				}
			}
			remaining_ov = utils.GetOverNightDays(runner.holidays, ovtime, ov);
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(EXCEPTION)InsertPositionDB:" +  e.getMessage() );
		}
		return nResult;
	}
	
	public long UpdatePositionDB( Order order )
	{
		long nResult = 0;
		try {
			if ( volume - order.volContract < 0 )
			{
				nResult = 0;
			} else {
				volume = volume - order.volContract;
				if ( volume.equals(Long.valueOf(0)) )
				{
					amount = Long.valueOf(0);
				} else {
					amount = amount - ( Long.valueOf(order.priceContract) * order.volContract);
					SetProfit( false, false );
				}		
				if ( volume == Long.valueOf(0) )
				{
					nResult = runner.mapper.RemovePosition(id, code);
					if ( nResult > 0 )
					{
						runner.redisTemplate.opsForHash().delete("CONTRACT-" + order.id, order.code);
					}	
				} else {
					nResult = runner.mapper.UpdatePosition(id, code, volume, currprice, price, profit);
					if ( nResult > 0 )
					{
						String data = toJSON();
						runner.redisTemplate.opsForHash().put("CONTRACT-" + order.id, order.code, data);
					}	
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(EXCEPTION)UpdatePositionDB:" +  e.getMessage() );
		}
		return nResult;
	}
	
	public long RemovePositionDB( )
	{
		long nResult = 0;
		try {
			nResult = runner.mapper.RemovePosition(id, code);
			if ( nResult > 0 )
			{
				runner.redisTemplate.opsForHash().delete("CONTRACT-" + id, code);
			}	
			//RemovePositionFeed ( code );
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(EXCEPTION)RemovePositionDB" +  e.getMessage() );
		}
		return nResult;
	}
	
	/*
	public void AddPositionFeed( String symbol )
	{
		HashMap<String, Object>	data = new HashMap<String, Object>();
		data.put("tr", utils.MTS_ADD_POSITION);
		data.put("id", id);
		data.put("symbol", symbol);
		runner.Send_Response(runner.environment.getProperty("feed_all.exchange"), data);
		runner.order_mgr.SaveLedgerFeed ( symbol, true );
	}
	
	public void RemovePositionFeed( String symbol )
	{
		HashMap<String, Object>	data = new HashMap<String, Object>();
		data.put("tr", utils.MTS_REMOVE_POSITION);
		data.put("id", id);
		data.put("symbol", symbol);
		runner.Send_Response(runner.environment.getProperty("feed_all.exchange"), data);
		runner.order_mgr.SaveLedgerFeed ( symbol, false );
	}
	*/
	
	@Override
	public String toString() {	
		HashMap<String, Object> object = new HashMap<String, Object>();
		try {
			object.put("ordnum", ordNum);
			object.put("time", time);
			object.put("symbol", code);
			object.put("id", id);
			object.put("MerchandiseTypeID", MerchandiseTypeID.toString());
			object.put("MarketTypeID", MarketTypeID.toString());
			object.put("PositionTypeID", PositionTypeID.toString());
			object.put("volume", utils.toNumFormat(volume.toString()));
			object.put("price", utils.toNumFormat(price.toString()));
			object.put("ov", ov);
			object.put("profit", utils.toNumFormat(profit.toString()));
			object.put("ovtime", ovtime);
			remaining_ov = utils.GetOverNightDays(runner.holidays, ovtime, ov);
			if ( remaining_ov.equals(Long.valueOf(-1)) )
			{
				object.put("remaining_ov", "무제한");
			} else {
				object.put("remaining_ov", remaining_ov.toString() + "일");
			}
			object.put("currprice", utils.toNumFormat(currprice.toString()));	
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(EXCEPTION)RemovePositionDB" +  e.getMessage() );
		}
	    return object.toString();
	}
	
	public String toJSON()
	{
		String data = new String("");
		HashMap<String, Object> object = new HashMap<String, Object>();
		try {
			object.put("ordnum", ordNum.toString());
			object.put("time", time);
			object.put("symbol", code);
			object.put("id", id);
			object.put("MerchandiseTypeID", MerchandiseTypeID.toString());
			object.put("MarketTypeID", MarketTypeID.toString());
			object.put("PositionTypeID", PositionTypeID.toString());
			object.put("volume", volume.toString());
			object.put("price", price.toString());
			object.put("ov", ov.toString());
			object.put("profit", profit.toString());
			object.put("ovtime", ovtime);		
			object.put("currprice", currprice.toString());
			object.put("name", name);
			object.put("amount", amount.toString());
			object.put("realamount", realamount.toString());
			remaining_ov = utils.GetOverNightDays(runner.holidays, ovtime, ov);
			if ( remaining_ov.equals(Long.valueOf(-1)) )
			{
				object.put("remaining_ov", "무제한");
			} else {
				object.put("remaining_ov", remaining_ov.toString() + "일");
			}
			data = runner.gson.toJson(object);
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(EXCEPTION)RemovePositionDB" +  e.getMessage() );
		}
	    return data;
	}
	
	public HashMap<String, Object> toMap()
	{
		HashMap<String, Object> object = new HashMap<String, Object>();		
		object.put("ordnum", ordNum);
		object.put("time", time);
		object.put("symbol", code);
		object.put("name", name);
		object.put("id", id);
		object.put("MerchandiseTypeID", MerchandiseTypeID.toString());
		object.put("MarketTypeID", MarketTypeID.toString());
		object.put("PositionTypeID", PositionTypeID.toString());
		object.put("volume", utils.toNumFormat(volume.toString()));
		object.put("price", utils.toNumFormat(price.toString()));
		object.put("profit", utils.toNumFormat(profit.toString()));
		object.put("eval", utils.toNumFormat(profit.toString()));
		object.put("ovtime", ovtime);		
		object.put("curprice", utils.toNumFormat(currprice.toString()));
		String event = runner.GetEvent(code);
		object.put("amount", amount.toString());
		object.put("realamount", realamount.toString());
		remaining_ov = utils.GetOverNightDays(runner.holidays, ovtime, ov);
		if ( remaining_ov.equals(Long.valueOf(-1)) )
		{
			object.put("remaining_ov", "무제한");
		} else {
			object.put("remaining_ov", remaining_ov.toString() + "일");
		}
		object.put("event", event);		
		return object;
	}
}

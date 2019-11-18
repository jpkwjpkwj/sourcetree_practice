package com.uplus.ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.reflect.TypeToken;

public class OutStanding {
	public LedgerRunner runner = null;
	public Long ordNum = new Long("0");
	public String time = new String();
	public String code = new String();
	public String name = new String();
	public String id = new String();
	public Long PositionTypeID = new Long("0");
	public Long HogaTypeID = new Long("0");
	public Long volOrder = new Long("0");
	public Long volOutstd = new Long("0");
	public Long price = new Long("0");
	public Long nHogaIndexOnOrder = new Long("-1");
	public Long nRemainPreContractToOrder = new Long("-1");
	public Long FirstInCount = new Long("0");
	public Long FilledCount = new Long("0");
	public Long cancelMode = new Long("-1");
	public Long realOrderOrdNum = new Long("0");
	public Long MerchandiseTypeID = new Long("0");
	public Long MarketTypeID = new Long("0");
	public Long RealOrdNum = new Long("0");
	public String league = new String();
	
	public OutStanding ( LedgerRunner runner, HashMap<String, Object> object )
	{
		this.runner = runner;
		BigDecimal tmp_ordNum = new BigDecimal(object.get("ordNum").toString());
		ordNum = tmp_ordNum.longValue();
		
		if ( object.get("time") != null )
		{
			time = object.get("time").toString();
		} else {
			time = utils.GetTime();
		}
		if ( object.get("symbol") != null )
			code = object.get("symbol").toString();
		if ( object.get("code") != null )
			code = object.get("code").toString();
		id = object.get("id").toString();
		if ( object.get("PositionTypeID") != null )
		{
			if (object.get("PositionTypeID").toString().equals("B") || object.get("PositionTypeID").toString().equals("매수"))
			{
				PositionTypeID = Long.valueOf(utils.POSITION_BUY);
			} else if (object.get("PositionTypeID").toString().equals("S") || object.get("PositionTypeID").toString().equals("매도"))
			{
				PositionTypeID = Long.valueOf(utils.POSITION_SELL);
			} else {
				PositionTypeID = Long.valueOf(object.get("PositionTypeID").toString());
			}
		}
		if ( object.get("HogaTypeID") != null )
		{
			BigDecimal tmp_HogaTypeID = new BigDecimal(object.get("HogaTypeID").toString());
			HogaTypeID = tmp_HogaTypeID.longValue();
		}
		if ( object.get("volOrder") != null )
		{
			BigDecimal tmp_volOrder = new BigDecimal(object.get("volOrder").toString());
			volOrder = tmp_volOrder.longValue();
			volOutstd = tmp_volOrder.longValue();
		}
		if ( object.get("volOutstd") != null )
		{
			BigDecimal tmp_volOutstd = new BigDecimal(object.get("volOutstd").toString());
			volOutstd = tmp_volOutstd.longValue();
		}
		if ( object.get("priceOrder") != null )
		{
			BigDecimal tmp_price = new BigDecimal(object.get("priceOrder").toString());
			price = tmp_price.longValue();
		}
		if ( object.get("price") != null )
		{
			BigDecimal tmp_price = new BigDecimal(object.get("price").toString());
			price = tmp_price.longValue();
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
		league = "ALPHA";
		name = runner.GetSymbolValue(code, "name");
	}
	
	public OutStanding ( LedgerRunner runner, Order order )
	{
		this.runner = runner;
		ordNum = order.ordNum;
		time = order.time;
		code = order.code;
		id = order.id;
		PositionTypeID = order.PositionTypeID;
		HogaTypeID = order.HogaTypeID;
		volOrder = order.volOrder;
		volOutstd = order.volOrder;
		Long tmp_price = new Long(order.priceOrder);
		price = tmp_price;
		MerchandiseTypeID = order.MerchandiseTypeID;
		MarketTypeID = order.MarketTypeID;
		league = "ALPHA";
		name = runner.GetSymbolValue(code, "name");
		
		Depth depth = new Depth(runner, code);
		if ( depth != null )
		{			
			Long count = depth.GetReadyFilledCount(this);
			FirstInCount = count;
			nHogaIndexOnOrder = depth.Index;
			FilledCount = count;
			User_Info user = runner.user_mgr.active_user_list.get(id);
			if ( user != null )
			{
				BigDecimal 체결계산수량 = new BigDecimal(user.체결감도);
				체결계산수량 = 체결계산수량.divide( BigDecimal.valueOf(100), 2, RoundingMode.CEILING );
				체결계산수량 = 체결계산수량.multiply( BigDecimal.valueOf(FirstInCount) );							
				FilledCount = 체결계산수량.longValue();
				
				if ( FilledCount.longValue() < 1 )
					FilledCount = Long.valueOf(1);
				
				String 체결 = String.format("미체결 등록 - id:%s, 진입수량:%s, 체결수량:%s, 체결감도:%s", 
						id, FirstInCount.toString(), FilledCount.toString(), user.체결감도.toString() );
				
				runner.PrintLogForce( 체결 );
				
			}
		}
	}
	
	public void UpdateDepth(Depth depth)
	{
		Long new_count = depth.GetReadyFilledCount(this);
		if ( FirstInCount == 0 )
		{
			FirstInCount = new_count;		
		}
	}
	
	public boolean CanbeFilled (Long filledCount, Depth depth )
	{
		boolean bCheck = false;		
		User_Info user = runner.user_mgr.active_user_list.get(id);
		if ( user != null )
		{		
			if ( user.체결감도.equals(Long.valueOf(0)) )
			{	
				FirstInCount = FirstInCount - filledCount;
				if ( FirstInCount <= 0 )
				{
					bCheck = true;
				}
			} else {
				//체결감도에 따른 주문 체결 : 처음 들어간 진입 호가잔량 수량의 N%의 잔량이 들어온 경우 체결				
				Long nCount = Long.valueOf(0);
				if (PositionTypeID.equals(utils.POSITION_BUY))
				{
					nCount = depth.GetBidCount( nHogaIndexOnOrder.intValue() );
				} else {
					nCount = depth.GetAskCount( nHogaIndexOnOrder.intValue() );
				}				
				if ( FilledCount >= nCount )
				{
					String 체결 = String.format("체결 - id:%s, 진입수량:%s, 체결수량:%s, 체결감도:%s", 
							id, FirstInCount.toString(), FilledCount.toString(), user.체결감도.toString() );
					
					runner.PrintLogForce( 체결 );
					bCheck = true;
				}
			}
		}
		return bCheck;
	}
	
	public long UpdateOutstandingDB()
	{
		long nResult = 0;
		nResult = runner.mapper.UpdateOrderOutStandingCount( id, code, ordNum, volOutstd);
		if ( nResult > 0 )
		{
			runner.PrintLogForce("부분체결:" + toPrint() );
			runner.redisTemplate.opsForHash().put("OUTSTANDING-" + id, ordNum.toString(), toString());
		}
		return nResult;
	}
		
	public long InsertOutStandingDB( Order order )
	{
		long nResult = 0;
		nResult = runner.mapper.NewOutStanding ( code, id, ordNum, PositionTypeID, HogaTypeID, volOrder, volOutstd, price,
												nHogaIndexOnOrder, nRemainPreContractToOrder, cancelMode, realOrderOrdNum, 
												MerchandiseTypeID, MarketTypeID, league );
		//if ( nResult > 0 )
		{
			runner.redisTemplate.opsForHash().put("OUTSTANDING-" + order.id, order.ordNum.toString(), toString());
		}
		return nResult;
	}
	
	
	public OutStanding UpdateOutStandingDB_Correct( Order order )
	{
		try {
			long nResult = 0;
			nResult = runner.mapper.UpdateOutStanding ( order.ordNum, order.ordNumOrg, order.priceOrder );
			if ( nResult > 0 )
			{
				Object object = runner.redisTemplate.opsForHash().get("OUTSTANDING-" + id, order.ordNumOrg.toString());
				if ( object != null )
				{
					HashMap<String, Object> data = runner.gson.fromJson(object.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
					if ( data != null )
					{
						OutStanding out = new OutStanding ( runner, data );
						out.price = order.priceOrder;
						out.ordNum = order.ordNum;
						runner.redisTemplate.opsForHash().put("OUTSTANDING-" + id, order.ordNum.toString(), out.toString());
						runner.redisTemplate.opsForHash().delete("OUTSTANDING-" + id, order.ordNumOrg.toString());
						return out;
					}
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)UpdateOutStandingDB_Correct");
		}
		return null;
	}
	
	public long RemoveOutStandingDB( Long ordNumOrg )
	{
		long nResult = 0;
		try {
			nResult = runner.mapper.RemoveOutStanding(ordNumOrg);
			if ( nResult > 0 )
			{
				nResult = runner.redisTemplate.opsForHash().delete("OUTSTANDING-" + id, ordNumOrg.toString());
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)RemoveOutStandingDB");
		}
		return nResult;
	}
	
	public LinkedHashMap<String, Object> toMap()
	{
		LinkedHashMap<String, Object> order = new LinkedHashMap<String, Object>();
		order.put("ordNum", ordNum.toString());
		order.put("time", time);
		order.put("symbol", code);
		order.put("code", code);
		order.put("id", id);
		order.put("PositionTypeID", PositionTypeID.toString());
		order.put("HogaTypeID", HogaTypeID.toString());
		order.put("name", name);
		order.put("volOrder", utils.toNumFormat(volOrder.toString()));
		order.put("volOutstd", utils.toNumFormat(volOutstd.toString()));
		order.put("price", utils.toNumFormat(price.toString()));
		order.put("nHogaIndexOnOrder", nHogaIndexOnOrder.toString());
		order.put("nRemainPreContractToOrder", nRemainPreContractToOrder.toString());
		order.put("cancelMode", cancelMode.toString());
		order.put("realOrderOrdNum", realOrderOrdNum.toString());
		order.put("MerchandiseTypeID", MerchandiseTypeID.toString());
		order.put("MarketTypeID", MarketTypeID.toString());
		order.put("RealOrdNum", RealOrdNum.toString());
		order.put("league", league.toString());
		return order;
	}
	
	public LinkedHashMap<String, Object> toPrint()
	{
		LinkedHashMap<String, Object> order = new LinkedHashMap<String, Object>();
		order.put("ordNum", ordNum.toString());
		order.put("time", time);
		order.put("symbol", code);
		order.put("code", code);
		order.put("id", id);
		order.put("PositionTypeID", PositionTypeID.toString());
		order.put("HogaTypeID", HogaTypeID.toString());
		order.put("name", name);
		order.put("volOrder", utils.toNumFormat(volOrder.toString()));
		order.put("volOutstd", utils.toNumFormat(volOutstd.toString()));
		order.put("price", utils.toNumFormat(price.toString()));
		//order.put("MerchandiseTypeID", MerchandiseTypeID.toString());
		//order.put("MarketTypeID", MarketTypeID.toString());
		return order;
	}
	
	@Override
	public String toString() {
		LinkedHashMap<String, Object> order = new LinkedHashMap<String, Object>();
		order.put("ordNum", ordNum.toString());
		order.put("time", time);
		order.put("code", code);
		order.put("name", name);
		order.put("id", id);
		order.put("PositionTypeID", PositionTypeID.toString());
		order.put("HogaTypeID", HogaTypeID.toString());
		order.put("volOrder", volOrder.toString());
		order.put("volOutstd", volOutstd.toString());
		order.put("price", price.toString());
		order.put("nHogaIndexOnOrder", nHogaIndexOnOrder.toString());
		order.put("nRemainPreContractToOrder", nRemainPreContractToOrder.toString());
		order.put("cancelMode", cancelMode.toString());
		order.put("realOrderOrdNum", realOrderOrdNum.toString());
		order.put("MerchandiseTypeID", MerchandiseTypeID.toString());
		order.put("MarketTypeID", MarketTypeID.toString());
		order.put("RealOrdNum", RealOrdNum.toString());
		order.put("league", league.toString());
		String data = runner.gson.toJson(order);
		return data;
	}
}

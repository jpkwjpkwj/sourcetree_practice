package com.uplus.ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

public class Depth {
	
	public class Hoga {
		public String price;
		public String count;
	}
	
	ArrayList<LinkedTreeMap<String,Object>> bid_array = new ArrayList<LinkedTreeMap<String,Object>>();
	ArrayList<LinkedTreeMap<String,Object>> ask_array = new ArrayList<LinkedTreeMap<String,Object>>();		
	
	HashMap<String, Object>	depth = new HashMap<String, Object>();
	
	public String symbol = new String();
	public Long open = new Long(0);
	public Long high = new Long(0);
	public Long low = new Long(0);
	public Long price = new Long(0);
	public Long change = new Long(0);
	public BigDecimal diff = new BigDecimal("0");
	public Long volume = new Long(0);
	public Long cvolume = new Long(0);
	public Long Index = new Long(0);
	public LedgerRunner runner = null;
	
	public String check = new String();
	
	boolean IsNull()
	{
		boolean bResult = true;
		if ( bid_array != null && ask_array != null )
		{
			bResult = false;
		}			
		return bResult;
	}
	
	public Depth ( LedgerRunner runner, HashMap<String, Object> depth_from )
	{
		try {
			this.runner = runner;
			this.depth.putAll(depth_from);			
			
			if ( depth.get("symbol") != null )
			{
				symbol = depth.get("symbol").toString();
			}
			
			ArrayList<LinkedTreeMap<String,Object>> tmp_ask_array = null;
			if ( depth.get("ask") != null )
			{
				tmp_ask_array = (ArrayList<LinkedTreeMap<String,Object>>)depth.get("ask");
			}
			ArrayList<LinkedTreeMap<String,Object>> tmp_bid_array = null;
			if ( depth.get("bid") != null )
			{
				tmp_bid_array = (ArrayList<LinkedTreeMap<String,Object>>)depth.get("bid");
			}
			if ( tmp_bid_array != null )
				bid_array.addAll(tmp_bid_array);
			if ( tmp_ask_array != null )
				ask_array.addAll(tmp_ask_array);	
			
			if ( depth.get("open") != null )
			{
				BigDecimal tmp_open = utils.toNumberDecimal(depth.get("open").toString());
				open = tmp_open.longValue();
			}
			if ( depth.get("high") != null )
			{
				BigDecimal tmp_high = utils.toNumberDecimal(depth.get("high").toString());
				high = tmp_high.longValue();
			}
			if ( depth.get("low") != null )
			{
				BigDecimal tmp_low = utils.toNumberDecimal(depth.get("low").toString());
				low = tmp_low.longValue();
			}
			if ( depth.get("price") != null )
			{
				BigDecimal tmp_price = utils.toNumberDecimal(depth.get("price").toString());
				price = tmp_price.longValue();
			}
			if ( depth.get("diff") != null )
			{
				BigDecimal tmp_diff = utils.toNumberDecimal(depth.get("diff").toString());
				diff = tmp_diff;
			}
			if ( depth.get("volume") != null )
			{
				BigDecimal tmp_volume = utils.toNumberDecimal(depth.get("volume").toString());
				volume = tmp_volume.longValue();
			}
			if ( depth.get("check") != null )
			{
				check = depth.get("check").toString();
			}
			if ( depth.get("cvolume") != null )
			{
				BigDecimal tmp_cvolume = utils.toNumberDecimal(depth.get("cvolume").toString());
				cvolume = tmp_cvolume.longValue();
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(Exception)DEPTH E - " + e.getMessage());
		}
	}
	
	public Depth ( LedgerRunner runner, String symbol )
	{
		try {
			
			this.runner = runner;
			Object mem_data = runner.redisTemplate.opsForHash().get("SYMBOL", symbol);
			if ( mem_data != null )
			{
				depth = runner.gson.fromJson(mem_data.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());		
				if ( depth != null)
				{
					this.depth.putAll(depth);
					
					if ( depth.get("symbol") != null )
					{
						symbol = depth.get("symbol").toString();
					}
					
					ArrayList<LinkedTreeMap<String,Object>> tmp_ask_array = null;
					if ( depth.get("ask") != null )
					{
						tmp_ask_array = (ArrayList<LinkedTreeMap<String,Object>>)depth.get("ask");
					}
					ArrayList<LinkedTreeMap<String,Object>> tmp_bid_array = null;
					if ( depth.get("bid") != null )
					{
						tmp_bid_array = (ArrayList<LinkedTreeMap<String,Object>>)depth.get("bid");
					}
					
					if ( tmp_bid_array != null )
						bid_array.addAll(tmp_bid_array);
					if ( tmp_ask_array != null )
						ask_array.addAll(tmp_ask_array);	
					
					if ( depth.get("open") != null )
					{
						BigDecimal tmp_open = utils.toNumberDecimal(depth.get("open").toString());
						open = tmp_open.longValue();
					}
					if ( depth.get("high") != null )
					{
						BigDecimal tmp_high = utils.toNumberDecimal(depth.get("high").toString());
						high = tmp_high.longValue();
					}
					if ( depth.get("low") != null )
					{
						BigDecimal tmp_low = utils.toNumberDecimal(depth.get("low").toString());
						low = tmp_low.longValue();
					}
					if ( depth.get("price") != null )
					{
						BigDecimal tmp_price = utils.toNumberDecimal(depth.get("price").toString());
						price = tmp_price.longValue();
					}
					if ( depth.get("diff") != null )
					{
						BigDecimal tmp_diff = utils.toNumberDecimal(depth.get("diff").toString());
						diff = tmp_diff;
					}
					if ( depth.get("volume") != null )
					{
						BigDecimal tmp_volume = utils.toNumberDecimal(depth.get("volume").toString());
						volume = tmp_volume.longValue();
					}
					if ( depth.get("check") != null )
					{
						check = depth.get("check").toString();
					}
					if ( depth.get("cvolume") != null )
					{
						BigDecimal tmp_cvolume = utils.toNumberDecimal(depth.get("cvolume").toString());
						cvolume = tmp_cvolume.longValue();
					}
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(Exception)DEPTH 2 E - " + e.getMessage());
		} 
	}
	
	public Long GetReadyFilledCount(OutStanding out)
	{
		Long ready_count = new Long(0);
		try {
			if ( out.PositionTypeID.equals(utils.POSITION_BUY) )
			{
				if ( bid_array != null )
				{
					for ( int i=0; i<bid_array.size(); i++ )
					{
						Long price = GetBidPrice(i);
						Long count = GetBidCount(i);
						if ( out.price.equals(price) )
						{
							Index = Long.valueOf(i);
							ready_count = count;
							break;
						}
					}
				}
			} else if ( out.PositionTypeID.equals(utils.POSITION_SELL) )
			{
				if ( ask_array != null )
				{
					for ( int i=0; i<ask_array.size(); i++ )
					{
						Long price = GetAskPrice(i);
						Long count = GetAskCount(i);
						if ( out.price.equals(price) )
						{
							Index = Long.valueOf(i);
							ready_count = count;
							break;
						}
					}
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(Exception)GetReadyFilledCount 2 E - " + e.getMessage());
		} 
		return ready_count;
	}
	
	public Long GetBidPrice ( int index )
	{
		Long price = new Long("0");	
		try {
			if ( index >= 0 && index <= 9 )
			if ( bid_array != null && bid_array.size() != 0 )
			{
				LinkedTreeMap<String, Object> object = bid_array.get(index);
				if ( object != null )
				{
					if ( object.get("price") != null )
					{
						BigDecimal tmp = utils.toNumberDecimal(object.get("price").toString());
						price = tmp.longValue();
					}
				}	
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)GetBidPrice E-" + e.getMessage());
		}
		return price;
	}
	
	public Long GetBidCount ( int index )
	{
		Long count = new Long("0");
		try {
			if ( index >= 0 && index <= 9 )
			if ( bid_array != null && bid_array.size() != 0 )
			{
				LinkedTreeMap<String, Object> object = bid_array.get(index);
				if ( object != null )
				{
					if ( object.get("count") != null )
					{
						BigDecimal tmp = utils.toNumberDecimal(object.get("count").toString());
						count = tmp.longValue();
					}
				}	
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)GetBidCount E-" + e.getMessage());
		}
		return count;
	}
	
	public Long GetAskPrice ( int index )
	{
		Long price = new Long("0");		
		try {
			if ( index >= 0 && index <= 9 )
			if ( ask_array != null && ask_array.size() != 0 )
			{
				LinkedTreeMap<String, Object> object = ask_array.get(index);
				if ( object != null )
				{
					if ( object.get("price") != null )
					{
						BigDecimal tmp = utils.toNumberDecimal(object.get("price").toString());
						price = tmp.longValue();
					}
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)GetAskPrice E-" + e.getMessage());
		}
		return price;
	}
	
	public Long GetAskCount ( int index )
	{
		Long count = new Long("0");	
		try {
			if ( index >= 0 && index <= 9 )
			if ( ask_array != null && ask_array.size() != 0 )
			{
				LinkedTreeMap<String, Object> object = ask_array.get(index);
				if ( object != null )
				{
					if ( object.get("count") != null )
					{
						BigDecimal tmp = utils.toNumberDecimal(object.get("count").toString());
						count = tmp.longValue();
					}
				}	
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)GetAskCount E-" + e.getMessage());
		}
		return count;
	}
	
	public Long GetBidLastPrice ( )
	{
		Long price = new Long("0");	
		try {
			if ( bid_array != null && bid_array.size() != 0 )
			{
				for ( int i=0; i<bid_array.size(); i++ )
				{
					LinkedTreeMap<String, Object> object = bid_array.get(i);
					if ( object != null )
					{
						if ( object.get("price") != null )
						{
							BigDecimal tmp = utils.toNumberDecimal(object.get("price").toString());
							if ( tmp.longValue() != 0 )
								price = tmp.longValue();
							else break;
						}
					}
				}	
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)GetBidLastPrice E-" + e.getMessage());
		}
		return price;
	}
	
	public Long GetAskLastPrice ( )
	{
		Long price = new Long("0");		
		try {
			if ( ask_array != null && ask_array.size() != 0 )
			{
				for ( int i=0; i<ask_array.size(); i++ )
				{
					LinkedTreeMap<String, Object> object = ask_array.get(i);
					if ( object != null )
					{
						if ( object.get("price") != null )
						{
							BigDecimal tmp = utils.toNumberDecimal(object.get("price").toString());
							if ( tmp.longValue() != 0 )
								price = tmp.longValue();
							else break;
						}
					}
				}	
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)GetAskLastPrice E-" + e.getMessage());
		}
		return price;
	}
	
	
	public Long GetBid1Price ()
	{
		Long price = new Long("0");	
		try {
			if ( depth != null && depth.get("bidprice1") != null )
			{
				BigDecimal tmp = utils.toNumberDecimal(depth.get("bidprice1").toString());
				price = tmp.longValue();
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)GetBidPrice E-" + e.getMessage());
		}
		return price;
	}
	
	public Long GetBid1Count ()
	{
		Long count = new Long("0");
		try {
			if ( depth != null && depth.get("bidcount1") != null )
			{
				BigDecimal tmp = utils.toNumberDecimal(depth.get("bidcount1").toString());
				count = tmp.longValue();
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)GetBidCount E-" + e.getMessage());
		}
		return count;
	}
	
	public Long GetAsk1Price ()
	{
		Long price = new Long("0");		
		try {
			if ( depth != null && depth.get("offprice1") != null )
			{
				BigDecimal tmp = utils.toNumberDecimal(depth.get("offprice1").toString());
				price = tmp.longValue();
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)GetAskPrice E-" + e.getMessage());
		}
		return price;
	}
	
	public Long GetAsk1Count ()
	{
		Long count = new Long("0");	
		try {
			if ( depth != null && depth.get("offcount1") != null )
			{
				BigDecimal tmp = utils.toNumberDecimal(depth.get("offcount1").toString());
				count = tmp.longValue();
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)GetAskCount E-" + e.getMessage());
		}
		return count;
	}
}

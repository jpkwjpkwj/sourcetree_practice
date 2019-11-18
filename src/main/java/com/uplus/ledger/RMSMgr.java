package com.uplus.ledger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.reflect.TypeToken;

public class RMSMgr {
	private LedgerRunner runner = null;
	
	public RMSMgr ( LedgerRunner runner )
	{
		this.runner = runner;
	}
	
	public void Initialization()
	{
		LoadExceptionSymbol(true);
	}
	
	public void LoadExceptionSymbol (boolean binit)
	{
		runner.redisTemplate.delete("RMS");
		ArrayList<HashMap<String, Object>> datalist = runner.mapper.GetExceptionSymbol();
		if ( datalist != null )
		{			
			for ( int i=0; i<datalist.size(); i++ )
			{
				HashMap<String, Object> item = datalist.get(i);
				if ( item.get("code") != null && item.get("use_yn") != null && item.get("reason") != null && item.get("reason_name") != null )
				{
					if ( item.get("use_yn").toString().equals("1") )
					{
						String symbol = item.get("code").toString();
						Object mem_symbol = runner.redisTemplate.opsForHash().get("SYMBOL", symbol);
						if ( mem_symbol != null )
						{
							HashMap<String, Object> object = runner.gson.fromJson(mem_symbol.toString(), new TypeToken<LinkedHashMap<String, Object>>(){}.getType());
							if ( object != null )
							{
								object.put("reason", item.get("reason").toString());
								object.put("reason_name", item.get("reason_name").toString());
								
								HashMap<String, Object> rms_item = new HashMap<String, Object>();
								rms_item.put("symbol", symbol);
								rms_item.put("reason", item.get("reason_name").toString());
								rms_item.put("reg_time", utils.GetDateTime() );
								String data = runner.gson.toJson(rms_item);
								runner.redisTemplate.opsForHash().put("RMS", symbol, data);
							}
						}
					}
				}
			}
		}
		
		ArrayList<HashMap<String, Object>> rms_user_list = runner.mapper.GetRMSUserList();
		if ( rms_user_list != null )
		{
			for ( int i=0; i<rms_user_list.size(); i++ )
			{
				HashMap<String, Object> item = rms_user_list.get(i);
				if ( item.get("code") != null )
				{
					String symbol = item.get("code").toString();
					if ( !runner.redisTemplate.opsForHash().hasKey("RMS", symbol) )
					{
						HashMap<String, Object> rms_item = new HashMap<String, Object>();
						rms_item.put("symbol", symbol);
						rms_item.put("reason", "RMS제한종목");
						rms_item.put("reg_time", utils.GetDateTime() );
						String data = runner.gson.toJson(rms_item);
						runner.redisTemplate.opsForHash().put("RMS", symbol, data);
					}
				}
			}
		}
		
	}	
}

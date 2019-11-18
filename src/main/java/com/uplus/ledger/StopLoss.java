package com.uplus.ledger;

import java.util.HashMap;

import com.google.gson.Gson;

public class StopLoss {
	public String id = new String();
	public String code = new String();
	public Long losstick = new Long(0);
	public Long earntick = new Long(0);
	public Gson gson = new Gson();
	StopLoss()
	{
		
	}
	StopLoss (HashMap<String, Object> data)
	{
		if ( data.get("id") != null )
		{
			id = data.get("id").toString();
		}
		if ( data.get("symbol") != null )
		{
			code = data.get("symbol").toString().trim();
		}
		if ( data.get("code") != null )
		{
			code = data.get("code").toString().trim();
		}
		if ( data.get("losstick") != null )
		{
			losstick = Long.valueOf(data.get("losstick").toString());
		}
		if ( data.get("earntick") != null )
		{
			earntick = Long.valueOf(data.get("earntick").toString());
		}
	}
	@Override
	public String toString() {
		HashMap<String, Object> data = new HashMap<String, Object>();	
		data.put("id", id);
		data.put("code", code);
		data.put("losstick", losstick.toString());
		data.put("earntick", earntick.toString());
		String s = gson.toJson(data);
		return s;
	}
}

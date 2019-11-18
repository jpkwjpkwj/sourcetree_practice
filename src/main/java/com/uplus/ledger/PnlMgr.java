package com.uplus.ledger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class PnlMgr {
	private LedgerRunner runner = null;
	public PnlMgr ( LedgerRunner runner )
	{
		this.runner = runner;
	}
	
	@Scheduled(initialDelay = 250, fixedDelay = 5000)
    public void StorePNL() {
		synchronized (this) {
			RunPNL();
		}
	}
	
	public void RunPNL()
	{
		//자바의 Timer는 다른 Thread로 처리되는거 같음. 
		//Iterator는 item을 메모리에 복사해서 쓰는것이 아는거 같고.. 
		//Item을 수정/삭제할때 다른 Thread에서 Update가 되었다면 Sync를 보장하지 않음 내부적으로 synchronized나 mutex같은걸 쓰지 않을수도 있고
		//Synchronize되지 않은 상태에서 item Iterator로 꺼내 쓰려고 할때 ConcurrentModificationException 발생
		//Array Reverse loop 고전적인 방법으로 해결함.
		try {
			Object[] array = runner.order_mgr.contracts.keySet().toArray();
			for ( int i=array.length-1; i>=0; i--)
			{
				String symbol = array[i].toString();
				ConcurrentHashMap <String, Position> pos = runner.order_mgr.contracts.get(symbol);
				if ( !pos.isEmpty() )
				{
					Object[] array_pos = pos.keySet().toArray();
					for ( int j=array_pos.length-1; j>=0; j-- )
					{
						String key = array_pos[j].toString();
						if ( pos.containsKey(key) )
						{
							Position pos_item = pos.get( key );
							if ( pos_item != null && pos.containsValue(pos_item) )
							{
								try {
									Long nResult = runner.mapper.GetPosition(pos_item.id, pos_item.code);
									if ( nResult > 0 )
									{
										nResult = runner.mapper.UpdateProfit ( pos_item.id, pos_item.code, pos_item.profit, pos_item.currprice );
										if ( nResult > 0 )
										{
											//runner.PrintLogForce("SetProfit Update.");
										} else {
											//runner.PrintLogForce("SetProfit Update Error");
										}
									}
								} catch ( Exception e )
								{
									runner.PrintLogForce("(Exception)SetProfit:" + e.getMessage());
								}
							}
						}
					}
				}
			}		
		} catch ( Exception e )
		{
			runner.PrintLogForce("(Exception)RunPNL-E:" + e.getMessage());
		}
	}
	
	public HashMap<String, Object> UpdatePNL ( HashMap<String, Object> item )
	{
		//contracts.
		//PNL 계산을 User List에서 하는게 좋을듯.
		try {
			if ( item.get("symbol") != null )
			{
				if ( item.get("price") != null )
				{
					String symbol = item.get("symbol").toString();
					ConcurrentHashMap <String, Position> pos = runner.order_mgr.contracts.get(symbol);
					if ( pos != null )
					{
						Long 총평가손익 = new Long(0);
						Object[] array_pos = pos.keySet().toArray();
						for ( int j=array_pos.length-1; j>=0; j-- )
						{
							String key = array_pos[j].toString();
							if ( pos.containsKey(key) )
							{
								Position pos_item = pos.get( key );
								if ( pos_item != null && pos.containsValue(pos_item) )
								{
									User_Info user_info = runner.user_mgr.GetUserInfo( pos_item.id );
									pos_item.SetProfit(symbol, item, true);	
									총평가손익 = 총평가손익 + pos_item.profit;										
									user_info.UpdateProfitRealTime();
									user_info.CalLosscutRemain();	//로스컷 여유금 계산.
									user_info.SendProfitNLoss();
									//실시간 손익을 계산 && FEEDING 후 Redis에 결과를 저장한다.
								}
							}
						}
					}
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce( "(Exception)UpdatePNL E:" + e.getMessage());
		}
		return null;
	}
}

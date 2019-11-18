package com.uplus.ledger;

import java.math.BigDecimal;
import java.util.HashMap;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class LosscutMgr {
	
	private LedgerRunner runner = null;
	
	public LosscutMgr ( LedgerRunner runner )
	{
		this.runner = runner;
	}
	
	public void InstallLosscut ( HashMap<String, Object> data )
	{
		
	}
	
	@Scheduled(initialDelay = 3000, fixedRateString = "3000")
	public void CheckCut()
	{
		synchronized (this) {
			RunLossCut();
		}
	}
	
	public void RunLossCut()
	{
		try {
			if ( runner.env_mgr.현재장상태.equals(MarketStatus.장운영) )
			{
				String time = utils.GetTime();
				Object[] array = runner.user_mgr.active_user_list.keySet().toArray();
				for ( int i=array.length-1; i>=0; i--)
				{
					String id = array[i].toString();
					if ( runner.user_mgr.active_user_list.containsKey(id) )
					{
						User_Info user_info = runner.user_mgr.active_user_list.get(id);
						if ( user_info != null && runner.user_mgr.active_user_list.containsValue(user_info) && user_info.positions.size() > 0 )
						{
							if ( !user_info.runlosscut.equals(Long.valueOf(2)) ) 
							{
								user_info.CalLosscutRemain();
								if ( user_info.로스컷여유금 <= 0 )
								{
									user_info.runlosscut = Long.valueOf(2);
									user_info.SaveMemory();	
									Long 평가담보금 = user_info.bankBalance + user_info.GetPNL();
									runner.PrintLogForce(String.format("로스컷여유금[%s] 담보금[%s]", user_info.로스컷여유금.toString(), 평가담보금.toString() ));
									String message = String.format("[로스컷실행][%s] [%s] 담보금:%s 로스컷비율:%s 로스컷여유금:%s", time, user_info.id, user_info.bankBalance.toString(), user_info.losscutLimit.toString(), user_info.로스컷여유금.toString());
									//runner.PrintLogForce(message);
									if ( runner.order_mgr.RemoveOutStandingID_Losscut( user_info ) )
									{
										runner.PrintLogForce("미체결주문성공:" + user_info.id);
										user_info.SendOutStanding();
									}
									if ( runner.order_mgr.RemovePositionID( user_info, utils.LOSSCUT_ORDER ) )
									{
										runner.PrintLogForce("전체반대매매성공:" + user_info.id);
										runner.PrintLogForce("담보금1:" + user_info.bankBalance.toString());
										user_info.SendPosition(true);
										user_info.SendMessageToUser(message, "losscut");
										runner.PrintLogForce("담보금2:" + user_info.bankBalance.toString());
									}
								}
								if ( user_info.losscutLimit.longValue() > 0 && user_info.loanBalance.longValue() > 0 )
								{
									if ( user_info.runlosscut == 0 )
									{
										BigDecimal 로스컷알림 = BigDecimal.valueOf(user_info.losscutLimit).multiply(BigDecimal.valueOf(1.1)).multiply(BigDecimal.valueOf(0.001));	//10%정도 여유를 준다.
										BigDecimal 로스컷알림금액 = BigDecimal.valueOf(user_info.loanBalance).multiply(로스컷알림);			
										if ( 로스컷알림금액.longValue() > user_info.bankBalance )
										{
											user_info.SendMessageToUser("로스컷 경고 메세지 입니다. 로스컷 여유금을 확인하세요.", "losscut");
											user_info.runlosscut = Long.valueOf(1);
											user_info.SaveMemory();
											String message = String.format("[로스컷체크][%s] [%s] 담보금:%s 로스컷비율:%s 로스컷여유금:%s", time, user_info.id, user_info.bankBalance.toString(), user_info.losscutLimit.toString(), user_info.로스컷여유금.toString());
											runner.PrintLogForce(message);
										}
									}
								}
							}	
						}
					}
				}
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("(EXCEPTION)RunLossCut");
		}
		
	}
}

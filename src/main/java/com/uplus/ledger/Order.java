package com.uplus.ledger;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Order {
	
	private OrderMgr runner = null;
	
	public String id = new String();
	public String code = new String();
	public Long MerchandiseTypeID = new Long("0");
	public Long MarketTypeID = new Long("0");
	public Long ordNum = new Long("0");
	public String date = new String();
	public String time = new String();
	public Long PositionTypeID = new Long("0");
	public Long OrderTypeID = new Long("0");
	public Long HogaTypeID = new Long("0");
	public Long volOrder = new Long("0");
	public Long volContract = new Long("0");
	public Long priceOrder = new Long("0");
	public Long priceContract = new Long("0");
	public Long profit = new Long("0");
	public Long ordNumOrg = new Long("0");
	public Long ActorTypeID = new Long("0");
	public Long isRealContract = new Long("0");
	public Long RealOrdNum = new Long("0");
	public Long Amount = new Long("0");
	public Long fee_processing = new Long("0");
	public Long fee_trading = new Long("0");
	public Long fee_closing = new Long("0");
	public Long fee_losscut = new Long("0");
	public Long fee_overnight = new Long("0");
	public Long tax = new Long("0");
	public Long ClientLevelTypeID = new Long("0");
	public Long ORDER_ACTOR_TYPE = new Long("0");
	public String name = new String();
	public String league = new String("ALPHA");
	public String orderkey = new String();
	public String ip = new String();
	//public String actor = new String();
	//public String actor_name = new String();
	public String message = new String();
	public boolean isETF = false;
	
	public Order ( OrderMgr runner, Position position )
	{
		try {
			this.runner = runner;
			ordNum = runner.GetLastOrderNO();
			id = position.id;
			code = position.code;
			date = utils.GetDate();
			time = utils.GetTime();
			MerchandiseTypeID = position.MerchandiseTypeID;
			PositionTypeID = utils.POSITION_SELL;
			HogaTypeID = utils.시장가;
			volOrder = position.volume;
			priceOrder = position.price;
			ActorTypeID = utils.ORDER_ACTOR_MANAGER;
			ORDER_ACTOR_TYPE = utils.ORDER_ACTOR_MANAGER;
			//actor_name = "SYSTEM";
			Amount = position.amount;
			name = runner.GetLedger().GetSymbolValue(code, "name");
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)Order 3 E" + e.getMessage());
		}
	}
	
	public Order ( OrderMgr runner, OutStanding outstanding )
	{
		try { 
			this.runner = runner;
			ordNum = runner.GetLastOrderNO();
			id = outstanding.id;
			code = outstanding.code;
			date = utils.GetDate();
			time = utils.GetTime();
			MerchandiseTypeID = outstanding.MerchandiseTypeID;
			MarketTypeID = outstanding.MarketTypeID;
			HogaTypeID = outstanding.HogaTypeID;
			PositionTypeID = outstanding.PositionTypeID;
			HogaTypeID = utils.시장가;
			volOrder = outstanding.volOutstd;
			priceOrder = outstanding.price;
			ActorTypeID = utils.ORDER_ACTOR_MANAGER;
			ORDER_ACTOR_TYPE = utils.ORDER_ACTOR_MANAGER;
			ordNumOrg = outstanding.ordNum;
			ordNum = runner.GetLastOrderNO();
			name = runner.GetLedger().GetSymbolValue(code, "name");
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)Order 1 E" + e.getMessage());
		}
	}
	
	public Order ( OrderMgr runner, OutStanding outstanding, Long out_ordNum )
	{
		try { 
			this.runner = runner;
			ordNum = out_ordNum;
			id = outstanding.id;
			code = outstanding.code;
			date = utils.GetDate();
			time = utils.GetTime();
			MerchandiseTypeID = outstanding.MerchandiseTypeID;
			MarketTypeID = outstanding.MarketTypeID;
			HogaTypeID = outstanding.HogaTypeID;
			PositionTypeID = outstanding.PositionTypeID;
			HogaTypeID = utils.시장가;
			volOrder = outstanding.volOutstd;
			priceOrder = outstanding.price;
			ActorTypeID = utils.ORDER_ACTOR_MANAGER;
			ORDER_ACTOR_TYPE = utils.ORDER_ACTOR_MANAGER;
			name = runner.GetLedger().GetSymbolValue(code, "name");
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)Order 1 E" + e.getMessage());
		}
	}

	public Order ( OrderMgr runner, HashMap<String, Object> order, boolean newOrdNum )
	{
		try { 
			this.runner = runner;
			
			if ( newOrdNum )
			{
				ordNum = runner.GetLastOrderNO();
			} else {
				if ( order.get("ordNum") != null )
				{
					BigDecimal tmp_ordNum = new BigDecimal(order.get("ordNum").toString());
					ordNum = tmp_ordNum.longValue(); 
				}
			}
			id = order.get("id").toString();
			if ( order.get("symbol") != null )
				code = order.get("symbol").toString();
			if ( order.get("code") != null )
				code = order.get("code").toString();
			if ( order.get("orderkey") != null )
				orderkey = order.get("orderkey").toString();
			
			if ( order.get("org_nm") != null )
			{
				BigDecimal tmp_ordNumOrg = new BigDecimal(order.get("org_nm").toString());
				ordNumOrg = tmp_ordNumOrg.longValue();
				if ( order.get("position") == null )
				{
					User_Info user_info = runner.GetLedger().user_mgr.GetUserInfo(id);
					if ( user_info != null )
					{
						/*
						Order org_ord = user_info.orders.get(ordNumOrg);
						if ( org_ord != null )
						{
							PositionTypeID = org_ord.PositionTypeID;
						}
						*/
						
						OutStanding out = user_info.GetOutStanding(ordNumOrg);
						if ( out != null )
						{
							PositionTypeID = out.PositionTypeID;
						}
						
					}
				}
			}
					
			if ( order.get("date") != null )
			{
				date = order.get("date").toString();
			}
			
			if ( order.get("time") != null )
			{
				time = order.get("time").toString();
			}
			
			if ( order.get("ordNumOrg") != null )
			{
				BigDecimal tmp_ordNumOrg = new BigDecimal(order.get("ordNumOrg").toString()); 
				ordNumOrg = tmp_ordNumOrg.longValue();
			}
			
			if ( order.get("MarketTypeID") != null )
			{
				BigDecimal temp_MarketTypeID = new BigDecimal(order.get("MarketTypeID").toString());
				MarketTypeID = temp_MarketTypeID.longValue();
			}
			
			if ( order.get("MerchandiseTypeID") != null )
			{
				BigDecimal temp_MerchandiseTypeID = new BigDecimal(order.get("MerchandiseTypeID").toString());
				MerchandiseTypeID = temp_MerchandiseTypeID.longValue();
			}
			
			if ( order.get("hogatype") != null )
			{
				BigDecimal tmp_HogaTypeID = new BigDecimal(order.get("hogatype").toString());
				HogaTypeID = tmp_HogaTypeID.longValue();
			}
			
			if ( order.get("HogaTypeID") != null )
			{
				BigDecimal tmp_HogaTypeID = new BigDecimal(order.get("HogaTypeID").toString());
				HogaTypeID = tmp_HogaTypeID.longValue();
			}
			
			if ( order.get("volume") != null )
			{
				BigDecimal tmp_volume = new BigDecimal(order.get("volume").toString());
				volOrder = tmp_volume.longValue();
			}
			
			if ( order.get("volOrder") != null )
			{
				BigDecimal tmp_volume = new BigDecimal(order.get("volOrder").toString());
				volOrder = tmp_volume.longValue();
			}
			
			if ( order.get("price") != null )
			{
				BigDecimal tmp = new BigDecimal(order.get("price").toString());
				priceOrder = tmp.longValue();
			}
			
			if ( order.get("priceOrder") != null )
			{
				BigDecimal tmp = new BigDecimal(order.get("priceOrder").toString());
				priceOrder = tmp.longValue();		
			}
			
			if ( runner.GetLedger().env_mgr.현재장상태.equals(MarketStatus.동시호가) )
			{
				if ( HogaTypeID.equals(utils.시장가) )
				{
					HogaTypeID = utils.동시호가시장가;
				} else if ( HogaTypeID.equals(utils.지정가) )
				{
					HogaTypeID = utils.동시호가지정가;
				}
			}
			
			if ( HogaTypeID.equals(utils.시장가) || HogaTypeID.equals(utils.동시호가시장가) )
			{
				priceOrder = Long.valueOf(0);
			}
			
			if ( order.get("PositionTypeID") != null )
			{
				BigDecimal temp_PositionTypeID = new BigDecimal(order.get("PositionTypeID").toString());
				PositionTypeID = temp_PositionTypeID.longValue();
			}
			
			if ( order.get("position") != null )
			{
				if ( order.get("position").toString().equals("B") )
				{
					PositionTypeID = Long.valueOf(utils.POSITION_BUY);
				} else if ( order.get("position").toString().equals("S") )
				{
					PositionTypeID = Long.valueOf(utils.POSITION_SELL);
				}
			}
			
			if ( order.get("ip") != null )
			{
				ip = order.get("ip").toString();
			}
			
			if ( order.get("message") != null )
			{
				message = order.get("message").toString();
			}
	
			if ( order.get("actor") != null )
			{
				ORDER_ACTOR_TYPE = utils.GetOrderActor(order.get("actor").toString());
			}
			if ( order.get("ActorTypeID") != null )
			{
				Long tmp_ActorTypeID = new Long(order.get("ActorTypeID").toString());
				ActorTypeID = tmp_ActorTypeID;		
			}
			if ( order.get("actor") != null )
			{
				ActorTypeID = utils.GetActor(order.get("actor").toString());
			}
			league = "ALPHA";
			
			String etf = runner.GetLedger().GetSymbolValue(code, "etfchk");
			if ( etf.equals("1") )
				isETF = true;
			
			name = runner.GetLedger().GetSymbolValue(code, "name");
			
			//취소 주문일 경우에 
			//PositionTypeID
			//if ( )
			
			if ( order.get("OrderTypeID") != null )
			{
				OrderTypeID = Long.valueOf(order.get("OrderTypeID").toString());
			}
			
			if ( OrderTypeID.longValue() == utils.REMOVE_ORDER )
			{
				User_Info user_info = runner.GetLedger().user_mgr.GetUserInfo(id);
				if ( user_info != null )
				{
					Order org_ord = user_info.orders.get(ordNumOrg);
					if ( org_ord != null )
					{
						PositionTypeID = org_ord.PositionTypeID;
					}
				}
			}
			
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)Order 2 E" + e.getMessage());
		}
	}
	
	public void Calculate_profit ( Long current, Long PositionTypeID )
	{
		try {
			User_Info user_info = runner.GetLedger().user_mgr.GetUserInfo(id);
			if (user_info != null)
			{
				Long diff = current - Long.valueOf(priceContract);
				if (PositionTypeID.equals(utils.POSITION_BUY))
				{
					diff = current - Long.valueOf(priceContract);
					profit = diff * volContract;
					if ( priceContract.longValue() == priceOrder.longValue() )
					{
						profit = Long.valueOf(0);
					}
					if ( user_info.매수손익적용.longValue() == 0 )
					{
						profit = Long.valueOf(0);
					}
				} else if (PositionTypeID.equals(utils.POSITION_SELL))
				{
					diff = Long.valueOf(priceContract) - current;
					//포지션 계산 해야됨
					Position pos = user_info.positions.get( code );
					if ( pos != null )
					{
						diff = priceContract - pos.price;
						profit = diff * volContract;
						UplusLedgerApplication.PrintLog(String.format("1.symbol[%s] 손익[%s]", code, profit.toString()));
					}
				}
				user_info.bankBalance = user_info.bankBalance + profit; 
				user_info.todayProfitRealized = user_info.todayProfitRealized + profit;
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)Calculate_processing_fee - E");
		}
	}
	
	public void Calculate_trading_fee ( )
	{
		try {
			User_Info user_info = runner.GetLedger().user_mgr.GetUserInfo(id);
			if ( user_info != null )
			{
				BigDecimal fee_trading = new BigDecimal(0);
				if ( !isETF )
				{
					fee_trading = user_info.fee_trading.multiply(BigDecimal.valueOf(Amount)).multiply(BigDecimal.valueOf(0.01));
				} else {
					fee_trading = user_info.fee_trading2.multiply(BigDecimal.valueOf(Amount)).multiply(BigDecimal.valueOf(0.01));
				}
				user_info.bankBalance = user_info.bankBalance - fee_trading.longValue();		
				user_info.todayFee = user_info.todayFee + fee_trading.longValue();
				user_info.totalBalance = user_info.bankBalance + user_info.loanBalance;
				this.fee_trading = fee_trading.longValue();
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("Calculate_processing_fee - E");
		}
	}
		
	public void Calculate_closing_fee ( )
	{
		try {
			User_Info user_info = runner.GetLedger().user_mgr.GetUserInfo(id);
			if ( user_info != null )
			{
				BigDecimal fee_closing = new BigDecimal("0");
				if ( !isETF )
				{
					fee_closing = user_info.fee_closing.multiply(BigDecimal.valueOf(Amount)).multiply(BigDecimal.valueOf(0.01));
				} else {
					fee_closing = user_info.fee_closing2.multiply(BigDecimal.valueOf(Amount)).multiply(BigDecimal.valueOf(0.01));
				}
				user_info.bankBalance = user_info.bankBalance - fee_closing.longValue();		
				user_info.todayFee = user_info.todayFee + fee_closing.longValue();
				user_info.totalBalance = user_info.bankBalance + user_info.loanBalance;
				this.fee_closing = fee_closing.longValue();
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)Calculate_processing_fee - E");
		}
	}
	
	public void Calculate_processing_fee ( )
	{
		try {
			User_Info user_info = runner.GetLedger().user_mgr.GetUserInfo(id);
			if ( user_info != null )
			{
				BigDecimal fee_processing = new BigDecimal("0");
				if ( !isETF )
				{
					fee_processing = user_info.fee_processing.multiply(BigDecimal.valueOf(Amount)).multiply(BigDecimal.valueOf(0.01));
				} else {
					fee_processing = user_info.fee_processing2.multiply(BigDecimal.valueOf(Amount)).multiply(BigDecimal.valueOf(0.01));
				}
				user_info.bankBalance = user_info.bankBalance - fee_processing.longValue();		
				user_info.todayFee = user_info.todayFee + fee_processing.longValue();
				user_info.totalBalance = user_info.bankBalance + user_info.loanBalance;
				this.fee_processing = fee_processing.longValue();
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)Calculate_processing_fee - E");
		}
	}
	
	public void Calculate_overnight_fee ( )
	{
		try {
			User_Info user_info = runner.GetLedger().user_mgr.GetUserInfo(id);
			if ( user_info != null )
			{
				BigDecimal fee_overnight = new BigDecimal("0");
				if ( !isETF )
				{
					fee_overnight = user_info.fee_overnight.multiply(BigDecimal.valueOf(Amount)).multiply(BigDecimal.valueOf(0.01));
				} else {
					fee_overnight = user_info.fee_overnight2.multiply(BigDecimal.valueOf(Amount)).multiply(BigDecimal.valueOf(0.01));
				}
				user_info.bankBalance = user_info.bankBalance - fee_overnight.longValue();		
				user_info.todayFee = user_info.todayFee + fee_overnight.longValue();
				user_info.totalBalance = user_info.bankBalance + user_info.loanBalance;
				this.fee_overnight = fee_overnight.longValue();
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)Calculate_processing_fee - E");
		}
	}
	
	public void Calculate_losscut_fee ( )
	{
		try {
			User_Info user_info = runner.GetLedger().user_mgr.GetUserInfo(id);
			if ( user_info != null )
			{
				BigDecimal fee_losscut = new BigDecimal("0");
				Long v = volContract * priceContract;
				if ( !isETF )
				{
					fee_losscut = user_info.fee_losscut.multiply(BigDecimal.valueOf(v)).multiply(BigDecimal.valueOf(0.01));
				} else {
					fee_losscut = user_info.fee_losscut.multiply(BigDecimal.valueOf(v)).multiply(BigDecimal.valueOf(0.01));
				}
				user_info.bankBalance = user_info.bankBalance - fee_losscut.longValue();		
				user_info.todayFee = user_info.todayFee + fee_losscut.longValue();
				user_info.totalBalance = user_info.bankBalance + user_info.loanBalance;
				
				runner.GetLedger().PrintLogForce( String.format("로스컷실행====>id:%s, 로스컷수수료:%s, 로스컷수수료율:%s, 금액:%s, 수량:%s, 가격:%s, 전체금액:%s", user_info.id, 
							fee_losscut.toString(), user_info.fee_losscut.toString(), Amount.toString(), volContract.toString(), priceContract.toString(), v.toString()));
				
				this.fee_losscut = fee_losscut.longValue();
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)Calculate_processing_fee - E");
		}
	}
	
	public void Calculate_tax ( )
	{
		try {
			if ( PositionTypeID.equals(utils.POSITION_SELL) )
			{
				User_Info user_info = runner.GetLedger().user_mgr.GetUserInfo(id);
				if ( user_info != null )
				{
					BigDecimal tax = new BigDecimal("0");
					if ( !isETF )
					{
						tax = user_info.tax.multiply(BigDecimal.valueOf(Amount)).multiply(BigDecimal.valueOf(0.01));
					} else {
						tax = user_info.tax2.multiply(BigDecimal.valueOf(Amount)).multiply(BigDecimal.valueOf(0.01));
					}
					user_info.bankBalance = user_info.bankBalance - tax.longValue();		
					user_info.todayFee = user_info.todayFee + tax.longValue();
					user_info.totalBalance = user_info.bankBalance + user_info.loanBalance;
					this.tax = tax.longValue();
				}
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)Calculate_processing_fee - E");
		}
	}
	
	public long InsertNewOrder ( User_Info user_info )
	{
		date = utils.GetDate();
		time = utils.GetTime();
		long nResult = 0;
		
		Long tmp_ClientLevelTypeID = new Long(user_info.ClientLevelTypeID);
		ClientLevelTypeID = tmp_ClientLevelTypeID;
		
		OrderTypeID = Long.valueOf(utils.NEW_ORDER);
		ActorTypeID = utils.ACTOR_EXCHANGE;
		//동시호가인 경우 지정가 -> 동시호가 지정가, 시장가 -> 동시호가 시장가로 처리한다.
		if ( runner.GetLedger().env_mgr.현재장상태.equals(MarketStatus.동시호가) )
		{
			if ( HogaTypeID == utils.지정가 )
			{
				HogaTypeID = utils.동시호가지정가;
			} else if ( HogaTypeID == utils.시장가 )
			{
				HogaTypeID = utils.동시호가시장가;
			}
		}
		try {
			nResult = runner.GetLedger().mapper.NewOrder(id, code, MerchandiseTypeID, MarketTypeID, ordNum,
					  									 date, time, PositionTypeID, OrderTypeID, HogaTypeID, 
					  									 volOrder, volContract, priceOrder.toString(), priceContract.toString(), profit, 
					  									 ordNumOrg, ORDER_ACTOR_TYPE, isRealContract, RealOrdNum,
					  									 league, Long.valueOf(0), fee_processing, fee_trading, fee_closing, 
					  									 fee_losscut, fee_overnight, tax, ClientLevelTypeID);
			
			if ( nResult > 0 )
			{
				if ( HogaTypeID.equals(utils.NEW_ORDER) || HogaTypeID.equals(utils.동시호가주문_지정가) || HogaTypeID.equals(utils.동시호가주문_시장가) )
				{
					SendToOrderStatus ( user_info, utils.OS_NEW );
				}
			}
			
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)InsertNewOrder-E:" + e.getMessage());
		}
		return nResult;
	}
	
	public long InsertRemoveOrder ( User_Info user_info )
	{
		date = utils.GetDate();
		time = utils.GetTime();
		long nResult = 0;
		
		Long tmp_ClientLevelTypeID = new Long(user_info.ClientLevelTypeID);
		ClientLevelTypeID = tmp_ClientLevelTypeID;
		
		OrderTypeID = Long.valueOf(utils.REMOVE_ORDER);
		ActorTypeID = utils.ACTOR_EXCHANGE;
		try {
			nResult = runner.GetLedger().mapper.NewOrder(id, code, MerchandiseTypeID, MarketTypeID, ordNum,
					  									 date, time, PositionTypeID, OrderTypeID, HogaTypeID, 
					  									 volOrder, volContract, priceOrder.toString(), priceContract.toString(), profit, 
					  									 ordNumOrg, ORDER_ACTOR_TYPE, isRealContract, RealOrdNum,
					  									 league, Long.valueOf(0), fee_processing, fee_trading, fee_closing, 
						  									fee_losscut, fee_overnight, tax, ClientLevelTypeID);
			
			if ( nResult > 0 )
			{
				SendToOrderStatus ( user_info, utils.OS_CANCEL );
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)InsertRemoveOrder-E:" + e.getMessage());
			runner.GetLedger().PrintLogForce(this.toString());
		}
		return nResult;
	}
	
	public long InsertReplaceOrder ( User_Info user_info )
	{
		date = utils.GetDate();
		time = utils.GetTime();
		long nResult = 0;
		
		Long tmp_ClientLevelTypeID = new Long(user_info.ClientLevelTypeID);
		ClientLevelTypeID = tmp_ClientLevelTypeID;
		
		OrderTypeID = Long.valueOf(utils.REPLACE_ORDER);
		ActorTypeID = utils.ACTOR_EXCHANGE;
		try {
			nResult = runner.GetLedger().mapper.NewOrder(id, code, MerchandiseTypeID, MarketTypeID, ordNum,
					  									 date, time, PositionTypeID, OrderTypeID, HogaTypeID, 
					  									 volOrder, volContract, priceOrder.toString(), priceContract.toString(), profit, 
					  									 ordNumOrg, ORDER_ACTOR_TYPE, isRealContract, RealOrdNum,
					  									 league, Long.valueOf(0), fee_processing, fee_trading, fee_closing, 
						  									fee_losscut, fee_overnight, tax, ClientLevelTypeID);
			
			if ( nResult > 0 )
			{
				SendToOrderStatus ( user_info, utils.OS_CORRECTION );
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)InsertReplaceOrder-E:" + e.getMessage());
		}
		return nResult;
	}
	
	public long InsertRejectOrder ( User_Info user_info )
	{
		date = utils.GetDate();
		time = utils.GetTime();
		long nResult = 0;
		
		///거부주문은 주문번호를 다시 딴다.
		ordNum = runner.GetLastOrderNO();
		
		Long tmp_ClientLevelTypeID = new Long(user_info.ClientLevelTypeID);
		ClientLevelTypeID = tmp_ClientLevelTypeID;
		
		ActorTypeID = utils.ACTOR_EXCHANGE;
		ORDER_ACTOR_TYPE = utils.ORDER_ACTOR_EXCHANGE;
		
		OrderTypeID = Long.valueOf(utils.REJECT_ORDER);
		
		try {
			nResult = runner.GetLedger().mapper.NewOrder(id, code, MerchandiseTypeID, MarketTypeID, ordNum,
					  									 date, time, PositionTypeID, OrderTypeID, HogaTypeID, 
					  									 volOrder, Long.valueOf(0), priceOrder.toString(), Long.valueOf(0).toString(), Long.valueOf(0), 
					  									 ordNumOrg, ORDER_ACTOR_TYPE, isRealContract, RealOrdNum,
					  									 league, Long.valueOf(0), Long.valueOf(0), Long.valueOf(0), Long.valueOf(0), 
					  									Long.valueOf(0), Long.valueOf(0), Long.valueOf(0), ClientLevelTypeID);
			
			if ( nResult > 0 )
			{
				SendToOrderStatus ( user_info, utils.OS_REJECT );
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)InsertRejectOrder-E:" + e.getMessage());
		}
		return nResult;
	}
	
	public long InsertOverNightOrder ( User_Info user_info )
	{
		date = utils.GetDate();
		time = utils.GetTime();
		long nResult = 0;
		
		Long tmp_ClientLevelTypeID = new Long(user_info.ClientLevelTypeID);
		ClientLevelTypeID = tmp_ClientLevelTypeID;
		
		OrderTypeID = Long.valueOf(utils.NEW_ORDER);
		
		ActorTypeID = utils.ACTOR_EXCHANGE;
		ORDER_ACTOR_TYPE = utils.ORDER_ACTOR_EXCHANGE;
		//오버나잇시 수수료 징수
		//Calculate_overnight_fee();
		
		Calculate_closing_fee();
		
		try {
			nResult = runner.GetLedger().mapper.NewOrder(id, code, MerchandiseTypeID, MarketTypeID, ordNum,
					  									 date, time, PositionTypeID, OrderTypeID, HogaTypeID, 
					  									 volOrder, volContract, priceOrder.toString(), priceContract.toString(), profit, 
					  									 ordNumOrg, ORDER_ACTOR_TYPE, isRealContract, RealOrdNum,
					  									 league, Long.valueOf(0), fee_processing, fee_trading, fee_closing, 
						  								 fee_losscut, fee_overnight, tax, ClientLevelTypeID);
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)InsertOverNightOrder-E:" + e.getMessage());
		}
		return nResult;
	}
	
	public long InsertOverNightFeeOrder ( User_Info user_info )
	{
		date = utils.GetDate();
		time = utils.GetTime();
		long nResult = 0;
		
		Long tmp_ClientLevelTypeID = new Long(user_info.ClientLevelTypeID);
		ClientLevelTypeID = tmp_ClientLevelTypeID;
		
		OrderTypeID = Long.valueOf(utils.OVERNIGHT_ORDER);
		
		ActorTypeID = utils.ACTOR_EXCHANGE;
		ORDER_ACTOR_TYPE = utils.ORDER_ACTOR_EXCHANGE;
		
		//오버나잇시 수수료 징수
		Calculate_overnight_fee();
		
		try {
			nResult = runner.GetLedger().mapper.NewOrder(id, code, MerchandiseTypeID, MarketTypeID, ordNum,
					  									 date, time, PositionTypeID, OrderTypeID, HogaTypeID, 
					  									 volOrder, volContract, priceOrder.toString(), priceContract.toString(), profit, 
					  									 ordNumOrg, ORDER_ACTOR_TYPE, isRealContract, RealOrdNum,
					  									 league, Long.valueOf(0), fee_processing, fee_trading, fee_closing, 
						  								 fee_losscut, fee_overnight, tax, ClientLevelTypeID);
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)InsertOverNightFeeOrder-E:" + e.getMessage());
		}
		return nResult;
	}
	
	public long InsertFilledOrder ( User_Info user_info )
	{
		date = utils.GetDate();
		time = utils.GetTime();
		long nResult = 0;
		
		Long tmp_ClientLevelTypeID = new Long(user_info.ClientLevelTypeID);
		ClientLevelTypeID = tmp_ClientLevelTypeID;
		
		if ( ORDER_ACTOR_TYPE.equals(utils.ORDER_ACTOR_LOSSCUT) )
		{
			Calculate_losscut_fee();
		}
		
		OrderTypeID = Long.valueOf(utils.FILLED_ORDER);
		
		//#{Amount}, #{fee_processing}, #{fee_trading}, #{fee_closing}, #{fee_losscut}, #{fee_overnight},
		//주문체결시 거래수수료,취급, 매도시에는 TAX를 징수한다.
		if ( PositionTypeID.equals(utils.POSITION_BUY) )
			Calculate_processing_fee();
		
		Calculate_trading_fee();
		Calculate_tax();

		ActorTypeID = utils.ACTOR_EXCHANGE;

		try {
			nResult = runner.GetLedger().mapper.NewOrder(id, code, MerchandiseTypeID, MarketTypeID, ordNum,
					  									 date, time, PositionTypeID, OrderTypeID, HogaTypeID, 
					  									 volOrder, volContract, priceOrder.toString(), priceContract.toString(), profit, 
					  									 ordNumOrg, ORDER_ACTOR_TYPE, isRealContract, RealOrdNum,
					  									 league, Amount, fee_processing, fee_trading, fee_closing, 
						  									fee_losscut, fee_overnight, tax, ClientLevelTypeID);
			
			if ( nResult > 0 )
			{
				SendToOrderStatus ( user_info, utils.OS_CONTRACT );
			}
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)InsertFilledOrder-E:" + e.getMessage());
		}
		return nResult;
	}
	
	public long InsertLosscutOrder ( User_Info user_info )
	{
		date = utils.GetDate();
		time = utils.GetTime();
		long nResult = 0;
		
		Long tmp_ClientLevelTypeID = new Long(user_info.ClientLevelTypeID);
		ClientLevelTypeID = tmp_ClientLevelTypeID;
		
		OrderTypeID = Long.valueOf(utils.NEW_ORDER);
		
		ActorTypeID = utils.ACTOR_SYSTEM;
		ORDER_ACTOR_TYPE = utils.ORDER_ACTOR_LOSSCUT;
		
		try {
			nResult = runner.GetLedger().mapper.NewOrder(id, code, MerchandiseTypeID, MarketTypeID, ordNum,
					  									 date, time, PositionTypeID, OrderTypeID, HogaTypeID, 
					  									 volOrder, volContract, priceOrder.toString(), priceContract.toString(), profit, 
					  									 ordNumOrg, ORDER_ACTOR_TYPE, isRealContract, RealOrdNum,
					  									 league, Amount, fee_processing, fee_trading, fee_closing, 
						  									fee_losscut, fee_overnight, tax, ClientLevelTypeID);	
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)InsertLosscutOrder-E:" + e.getMessage());
		}
		return nResult;
	}
	
	public long InsertStopLossOrder ( User_Info user_info, Long StopType )
	{
		date = utils.GetDate();
		time = utils.GetTime();
		long nResult = 0;
		
		Long tmp_ClientLevelTypeID = new Long(user_info.ClientLevelTypeID);
		ClientLevelTypeID = tmp_ClientLevelTypeID;
		
		OrderTypeID = Long.valueOf(utils.NEW_ORDER);
		
		ActorTypeID = utils.ACTOR_SYSTEM;
		if ( StopType == utils.STOP_PLUS)
		{
			ORDER_ACTOR_TYPE = utils.ORDER_ACTOR_STOP_PLUS;
		} else if ( StopType == utils.STOP_MINUS)
		{
			ORDER_ACTOR_TYPE = utils.ORDER_ACTOR_STOP_MINUS;
		}
		
		try {
			nResult = runner.GetLedger().mapper.NewOrder(id, code, MerchandiseTypeID, MarketTypeID, ordNum,
					  									 date, time, PositionTypeID, OrderTypeID, HogaTypeID, 
					  									 volOrder, volContract, priceOrder.toString(), priceContract.toString(), profit, 
					  									 ordNumOrg, ORDER_ACTOR_TYPE, isRealContract, RealOrdNum,
					  									 league, Amount, fee_processing, fee_trading, fee_closing, 
						  									fee_losscut, fee_overnight, tax, ClientLevelTypeID);	
		} catch ( Exception e )
		{
			runner.GetLedger().PrintLogForce("(EXCEPTION)InsertStopLossOrder-E:" + e.getMessage());
		}
		return nResult;
	}
	
	public long InserActionLog ( String status, User_Info user_info )
	{
		long nResult = 0;
		String action = new String();
		String contracts = user_info.toPositionString();
		String outstandings = user_info.toOutStandingString();
		action = String.format("[%s] [%s] [%s] [주문가:%s] [체결가:%s]", status, code, name, priceOrder.toString(), priceContract.toString());
		nResult = runner.GetLedger().mapper.InsertUserActionLog ( id, ActorTypeID.toString(), action, 
							ordNum.toString(), code, priceOrder.toString(), PositionTypeID.toString(), volOrder.toString(), fee_processing.toString(),
							fee_trading.toString(), fee_closing.toString(), fee_losscut.toString(), fee_overnight.toString(), tax.toString(),
							profit.toString(), user_info.totalBalance.toString(), contracts, outstandings );
		return nResult;
	}
	
	public void SendToOrderStatus ( User_Info user_info, Long ord_status  )
	{
		HashMap<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("tr", utils.MTS_ORDER_STATUS);
		data.put("status", ord_status.toString() );
		data.put("id", user_info.id);
		data.put("ord_no", ordNum.toString());
		data.put("org_no", ordNumOrg.toString());
		data.put("symbol", code);
		data.put("name", name);
		data.put("position", PositionTypeID.toString());
		data.put("hogatype", HogaTypeID.toString());
		data.put("price", priceOrder.toString());
		data.put("volume", volOrder.toString());
		data.put("filled_price", priceContract.toString());
		data.put("message", "");
		user_info.SendOrderStatus( data );
		
	}
	
	public HashMap<String, Object> toMap()
	{
		HashMap<String, Object> order = new LinkedHashMap<String, Object>();
		order.put("id", id);
		order.put("code", code);
		order.put("MerchandiseTypeID", MerchandiseTypeID.toString());
		order.put("MarketTypeID", MarketTypeID.toString());
		order.put("ordNum", ordNum.toString());
		order.put("date", date);
		order.put("time", time);
		order.put("PositionTypeID", PositionTypeID.toString());
		order.put("OrderTypeID", OrderTypeID.toString());
		order.put("HogaTypeID", HogaTypeID.toString());
		order.put("volOrder", volOrder.toString());
		order.put("volContract", volContract.toString());
		order.put("priceOrder", priceOrder.toString());
		order.put("priceContract", priceContract.toString());
		order.put("profit", profit.toString());
		order.put("ordNumOrg", ordNumOrg.toString());
		order.put("ActorTypeID", ActorTypeID.toString());
		order.put("isRealContract", isRealContract.toString());
		order.put("RealOrdNum", RealOrdNum.toString());
		order.put("Amount", Amount.toString());
		order.put("fee_processing", fee_processing.toString());
		order.put("fee_trading", fee_trading.toString());
		order.put("fee_closing", fee_closing.toString());
		order.put("fee_losscut", fee_losscut.toString());
		order.put("tax", tax.toString());
		order.put("ClientLevelTypeID", ClientLevelTypeID.toString());
		order.put("league", league);
		order.put("orderkey", orderkey);
		order.put("ip", ip);
		order.put("ActorTypeID", ActorTypeID.toString());
		order.put("ORDER_ACTOR_TYPE", ORDER_ACTOR_TYPE.toString());
		return order;
	}
	
	@Override
	public String toString() {
		HashMap<String, Object> order = toMap();
		String data = runner.GetLedger().gson.toJson(order);
		return data;
	}
	
	public String toPrint() {
		HashMap<String, Object> order = new LinkedHashMap<String, Object>();		
		order.put("id", id);
		order.put("code", code);
		order.put("ordNum", ordNum.toString());
		order.put("ordNumOrg", ordNumOrg.toString());
		order.put("PositionTypeID", PositionTypeID.toString());
		order.put("OrderTypeID", OrderTypeID.toString());
		order.put("volOrder", volOrder.toString());
		order.put("volContract", volContract.toString());
		order.put("priceOrder", priceOrder.toString());
		order.put("priceContract", priceContract.toString());
		order.put("ActorTypeID", ActorTypeID.toString());
		order.put("ORDER_ACTOR_TYPE", ORDER_ACTOR_TYPE.toString());	
		order.put("time", time);		
		String data = runner.GetLedger().gson.toJson(order);
		return data;
	}
}

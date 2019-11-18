package com.uplus.ledger.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

@Service
@Mapper
public interface uplus_mapper {
	public String GetInterest(String id);
	public Integer InsertInterest(String id, String codes);
	public Integer UpdateInterest(String id, String codes);
	public ArrayList<HashMap<String, Object>> GetLoanHistoryAccept(String accept);
	public ArrayList<HashMap<String, Object>> GetLoanHistory(String accept);
	public ArrayList<HashMap<String, Object>> GetLoanHistoryId(String id);
	public String GetLoanMax(String id);
	public Long GetOverNightDays(String id);
	public ArrayList<HashMap<String, Object>> GetContractsUser(String id);
	public Long GetOverNightMargin(String id);
	public Long GetTotalAmount(String id);
	public HashMap<String, Object> GetOverNightInfo(String id);
	public Integer UpdateOverNightStatus ( String id, Integer status );
	public ArrayList<HashMap<String, Object>> GetEachDayProfit ( String id, String dateBegin, String dateEnd, String bIncludeTester );
	public ArrayList<HashMap<String, Object>> GetOrderHistory ( String id, String dateBegin, String dateEnd, String bContractOnly );
	public ArrayList<HashMap<String, Object>> GetContractList ( String dateBegin, String dateEnd, String type, String filter, String bIncludeTester, String submanager );
	public ArrayList<HashMap<String, Object>> GetProfitAllUser ( String dateBegin, String dateEnd, String bIncludeTester );
	public ArrayList<HashMap<String, Object>> GetTotalBankIO ( String dateBegin, String dateEnd );
	public ArrayList<HashMap<String, Object>> GetRecommenderProfit ( String dateBegin, String dateEnd );
	public ArrayList<HashMap<String, Object>> GetEachClientTotalProfit ( String dateBegin, String dateEnd, String whereColumn, String whereKey, String bIncludeTester );	
	public ArrayList<HashMap<String, Object>> GetOrderActionLog ( String id, String dateBegin, String isMaster );
	public ArrayList<HashMap<String, Object>> GetBankinIOHistory ( String BankingIOType, String accountType, String user, 
																	String queryField, String query, String dateBegin, String dateEnd, String submanager );
	public ArrayList<HashMap<String, Object>> GetTodayProfitBalance ( String whereColumn, String whereKey, String isIncludeTester );
	
	public ArrayList<HashMap<String, Object>> GetOutStanding_1 ( String isIncludeTester );
	public ArrayList<HashMap<String, Object>> GetOutStanding_2 ( String id );
	public ArrayList<HashMap<String, Object>> GetOutStanding_3 ( String isIncludeTester, String submanager );
	public ArrayList<HashMap<String, Object>> GetOutStanding_4 ( String id, String submanager );
	
	public ArrayList<HashMap<String, Object>> GetOrderPosition_NO_ID ( String isIncludeTester );
	public ArrayList<HashMap<String, Object>> GetOrderPosition ( String id );
	
	public HashMap<String, Object> CheckPassword(String id);
	
	public ArrayList<HashMap<String, Object>> GetEnvironment();
	public HashMap<String, Object> GetUserInfo(String id);
	
	public ArrayList<HashMap<String, Object>> GetUserList();
	
	public Integer InsertActionLog(String id, String actor, String action, String memo);
	public Integer UpdateLoginStatus(String id);
	
	public ArrayList<HashMap<String, Object>> GetUserBankIO(String id, String banktype, String dateBegin, String dateEnd);
	
	public ArrayList<HashMap<String, Object>> GetContracts();
	public ArrayList<HashMap<String, Object>> GetOutStandings();
	public HashMap<String, Object> GetOrder(String date, Long ordNum);
	
	public ArrayList<HashMap<String, Object>> GetExceptionSymbol();
	
	public ArrayList<HashMap<String, Object>> GetHoliday();
	
	public ArrayList<HashMap<String, Object>> GetStopLoss();
	
	//주문관련
	public String GetLastOrderNO ( String date );
	
	public Long NewOrder ( String id, String code, Long MerchandiseTypeID, Long MarketTypeID, Long ordNum,
						  String date, String time, Long PositionTypeID, Long OrderTypeID, Long HogaTypeID, 
						  Long volOrder, Long volContract, String priceOrder, String priceContract, Long profit, 
						  Long ordNumOrg, Long ActorTypeID, Long isRealContract, Long RealOrdNum,
						  String league, Long Amount, Long fee_processing, Long fee_trading, Long fee_closing, 
						  Long fee_losscut, Long fee_overnight, Long tax, Long ClientLevelTypeID );
	
	public Long NewOutStanding ( String code, String id, Long ordNum, Long PositionTypeID, Long HogaTypeID, Long volOrder, 
								Long volOutstd, Long price, Long nHogaIndexOnOrder, Long nRemainPreContractToOrder,
								Long cancelMode, Long realOrderOrdNum, Long MerchandiseTypeID, Long MarketTypeID, String league );	
	
	public Long UpdateOutStanding ( Long ordno, Long origin, Long price );
	
	public Long RemoveOutStanding ( Long ordNum );	
	
	public Long NewPosition ( Long ordNum, String time, String code, String id, Long PositionTypeID, Long volume, Long price, 
							  Long MerchandiseTypeID, Long MarketTypeID, Long isReal, String league, Long ov, Long profit,
							  String ovtime, Long currprice );
	
	public Long UpdatePosition ( String id, String code, Long volume, Long currprice, Long price, Long profit );
	
	public Long RemovePosition ( String id, String code );
	
	public Long UpdateProfit ( String id, String code, Long profit, Long currprice );
	
	public Long GetPosition ( String id, String code );
	
	public Long UpdateOrderOutStandingCount ( String id, String code, Long ordno, Long volOutStd );
	
	public Long UpdateClientProfit ( Long bankBalance, Long todayProfitRealized, Long todayFee, String id );
	
	public Long InitDB ( String league );
	
	public Long UpdateContract ( String volume, String price, String ov, String ovtime, String time, String id, String code );
	
	public Long InsertUserActionLog ( String id, String ActorTypeID, String action, String OrdNum, String code, String priceOrder, 
									  String PositionTypeID, String volOrder, String fee_processing,
									  String fee_trading, String fee_closing, String fee_losscut, 
									  String fee_overnight, String tax, String profit, String evalBalance, String contracts, String outstandings);
	
	public Long InitStopLoss();
	
	public Long ClearUserData(String id);
	public Long ClearPosition(String id);
	public Long ClearOutStanding(String id);
	public Long ClearOrder(String id);
	
	public Long UnInstallStopLoss ( String id, String symbol );
	public Long UnInstallStopLossAll ();
	
	public Long InsertEnv ( String name, String value );
	
	public Long UpdateOVDays ( String id, String symbol, Long ov );
	
	public ArrayList<HashMap<String, Object>> GetRMSUserList();
}

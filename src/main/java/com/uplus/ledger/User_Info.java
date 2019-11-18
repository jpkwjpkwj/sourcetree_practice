package com.uplus.ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.uplus.ledger.OvernightMgr.AscendingPosition;

public class User_Info {
	String id = new String();	
	String name	= new String();
	String password	= new String();
	String email	= new String();
	String mobile	= new String();
	String bank	= new String();
	String bankAccount	= new String();
	String bankOwner	= new String();
	BigDecimal tax	= new BigDecimal("0");
	BigDecimal fee_processing	= new BigDecimal("0");
	BigDecimal fee_trading	= new BigDecimal("0");
	BigDecimal fee_closing	= new BigDecimal("0");
	BigDecimal fee_losscut	= new BigDecimal("0");
	BigDecimal fee_overnight	= new BigDecimal("0");
	BigDecimal tax2	= new BigDecimal("0");
	BigDecimal fee_processing2	= new BigDecimal("0");
	BigDecimal fee_trading2	= new BigDecimal("0");
	BigDecimal fee_closing2	= new BigDecimal("0");
	BigDecimal fee_losscut2	= new BigDecimal("0");
	BigDecimal fee_overnight2	= new BigDecimal("0");
	Long ClientLevelTypeID	= new Long("0");
	Long AccountStateTypeID	= new Long("0");
	String recommender	= new String();
	String registerDate	= new String();
	Long bankBalance	= new Long("0");
	Long loanBalance	= new Long("0");
	Long totalBalance	= new Long("0");
	Long todayProfitRealized	= new Long("0");
	Long todayFee	= new Long("0");
	Long last_bankBalance	= new Long("0");
	Long last_loanBalance	= new Long("0");
	Long last_ProfitRealized	= new Long("0");
	Long last_Fee	= new Long("0");
	Long enableOvernight	= new Long("0");
	Long OvernightDays	= new Long("0");
	Long securityRate	= new Long("0");
	Long loanLeverage	= new Long("0");
	Long loanMax	= new Long("0");
	Long losscutLimit	= new Long("0");
	Long enableShort	= new Long("0");
	String is_login	= new String("0");
	Long login_fail	= new Long("0");
	Long stockOrderLevel	= new Long("0");
	Long etfOrderLevel	= new Long("0");
	String league	= new String();
	Long useAccountIdx	= new Long("0");
	String company_bank	= new String();
	String company_account	= new String();
	String company_bank_owner	= new String();
	String macadress	= new String();
	String releaseDate	= new String();
	Long loanprocess	= new Long("0");
	Long baseBalance	= new Long("0");
	String birthday	= new String();
	Long focusing	= new Long("0");
	Long tradeauth	= new Long("0");
	String submanager	= new String();
	Long positionlimit	= new Long("0");
	Long outstandinglimit	= new Long("0");
	Long interestcount= new Long("0");
	
	Long 로스컷여유금 = new Long(0);
	Long 로스컷금액 = new Long(0);
	Long 총평가손익 = new Long(0);
	
	Long 이전총평가손익 = new Long(0);
	
	Long runlosscut = new Long(0);
	boolean runOV = false;
	
	LedgerRunner runner = null;
		
	Long 체결감도 = new Long(0);
	Long 체결방식 = new Long(0);
	Long 매수손익적용 = new Long(0);
	Long 종목당최대금액 = new Long(0);
	
	//FOR RESPONSE
	public ConcurrentHashMap <String, Position> positions = new ConcurrentHashMap <String, Position>();
	public ConcurrentHashMap <Long, OutStanding> outstandings = new ConcurrentHashMap <Long, OutStanding>();
	public ConcurrentHashMap <Long, Order> orders = new ConcurrentHashMap <Long, Order>();
	public ConcurrentHashMap <String, StopLoss> stoploss_list = new ConcurrentHashMap <String, StopLoss>();
	
	public void InstallStopLoss ( String symbol, StopLoss stoploss )
	{
		if ( stoploss_list.get(symbol) != null )
		{
			stoploss_list.remove(symbol);
		}
		stoploss_list.put(symbol, stoploss);
	}
	
	public void UnInstallStopLoss ( String symbol, StopLoss stoploss )
	{
		if ( stoploss_list.get(symbol) != null )
		{
			stoploss_list.remove(symbol);
		}
	}

	public User_Info ( LedgerRunner runner, HashMap<String, Object> item )
	{
		this.runner = runner;
		SetData ( item );
		runOV = false;
	}
	
	public User_Info ( LedgerRunner runner )
	{
		this.runner = runner;
		runOV = false;
	}
	
	public void SetData ( HashMap<String, Object> item )
	{
		if ( item.get("id") != null )
		{
			id	= item.get("id").toString();
		}
		if ( item.get("name") != null )
		{
			name	= item.get("name").toString();
		}
		if ( item.get("password") != null )
		{
			password	= item.get("password").toString();
		}
		if ( item.get("email") != null )
		{
			email	= item.get("email").toString();
		}
		if ( item.get("mobile") != null )
		{
			mobile	= item.get("mobile").toString();
		}
		if ( item.get("bank") != null )
		{
			bank	= item.get("bank").toString();
		}
		if ( item.get("bankAccount") != null )
		{
			bankAccount	= item.get("bankAccount").toString();
		}
		
		if ( item.get("bankaccount") != null )
		{
			bankAccount	= item.get("bankaccount").toString();
		}
		
		if ( item.get("bankOwner") != null )
		{
			bankOwner	= item.get("bankOwner").toString();
		}
		
		if ( item.get("bankowner") != null )
		{
			bankOwner	= item.get("bankowner").toString();
		}
		
		if ( item.get("tax") != null )
		{
			BigDecimal tmp_tax = new BigDecimal(item.get("tax").toString());
			tax	= tmp_tax;
		}
		if ( item.get("fee_processing") != null )
		{
			BigDecimal tmp_fee_processing = new BigDecimal(item.get("fee_processing").toString());
			fee_processing	= tmp_fee_processing;
		}
		if ( item.get("fee_trading") != null )
		{
			BigDecimal tmp_fee_trading = new BigDecimal(item.get("fee_trading").toString());
			fee_trading	= tmp_fee_trading;
		}
		if ( item.get("fee_closing") != null )
		{
			BigDecimal tmp_fee_closing = new BigDecimal(item.get("fee_closing").toString());
			fee_closing	= tmp_fee_closing;
		}
		if ( item.get("fee_losscut") != null )
		{
			BigDecimal tmp_fee_losscut = new BigDecimal(item.get("fee_losscut").toString());
			fee_losscut	= tmp_fee_losscut;
		}
		if ( item.get("fee_overnight") != null )
		{
			BigDecimal tmp_fee_overnight = new BigDecimal(item.get("fee_overnight").toString());
			fee_overnight	= tmp_fee_overnight;
		}
		if ( item.get("tax2") != null )
		{
			BigDecimal tmp_tax2 = new BigDecimal(item.get("tax2").toString());
			tax2	= tmp_tax2;
		}
		if ( item.get("fee_processing2") != null )
		{
			BigDecimal tmp_fee_processing2 = new BigDecimal(item.get("fee_processing2").toString());
			fee_processing2	= tmp_fee_processing2;
		}
		if ( item.get("fee_trading2") != null )
		{
			BigDecimal tmp_fee_trading2 = new BigDecimal(item.get("fee_trading2").toString());
			fee_trading2	= tmp_fee_trading2;
		}
		if ( item.get("fee_closing2") != null )
		{
			BigDecimal tmp_fee_closing2 = new BigDecimal(item.get("fee_closing2").toString());
			fee_closing2	= tmp_fee_closing2;
		}
		if ( item.get("fee_losscut2") != null )
		{
			BigDecimal tmp_fee_losscut2 = new BigDecimal(item.get("fee_losscut2").toString());
			fee_losscut2	= tmp_fee_losscut2;
		}
		if ( item.get("fee_overnight2") != null )
		{
			BigDecimal tmp_fee_overnight2 = new BigDecimal(item.get("fee_overnight2").toString());
			fee_overnight2	= tmp_fee_overnight2;
		}
		if ( item.get("ClientLevelTypeID") != null )
		{
			Long tmp_ClientLevelTypeID = new Long(item.get("ClientLevelTypeID").toString());
			ClientLevelTypeID	= tmp_ClientLevelTypeID;
		}
		
		if ( item.get("clientleveltypeid") != null )
		{
			Long tmp_ClientLevelTypeID = new Long(item.get("clientleveltypeid").toString());
			ClientLevelTypeID	= tmp_ClientLevelTypeID;
		}
		
		if ( item.get("AccountStateTypeID") != null )
		{
			Long tmp_AccountStateTypeID = new Long(item.get("AccountStateTypeID").toString());
			AccountStateTypeID	= tmp_AccountStateTypeID;
		}
		
		if ( item.get("accountstatetypeid") != null )
		{
			Long tmp_AccountStateTypeID = new Long(item.get("accountstatetypeid").toString());
			AccountStateTypeID	= tmp_AccountStateTypeID;
		}
		
		if ( item.get("recommender") != null )
		{
			recommender	= item.get("recommender").toString();
		}
		if ( item.get("registerDate") != null )
		{
			registerDate	= item.get("registerDate").toString();
		}
		
		if ( item.get("registerdate") != null )
		{
			registerDate	= item.get("registerdate").toString();
		}
		
		if ( item.get("bankBalance") != null )
		{
			Long tmp_bankBalance = new Long(item.get("bankBalance").toString());
			bankBalance	= tmp_bankBalance;
		}
		
		if ( item.get("bankbalance") != null )
		{
			Long tmp_bankBalance = new Long(item.get("bankbalance").toString());
			bankBalance	= tmp_bankBalance;
		}
		
		if ( item.get("loanBalance") != null )
		{
			Long tmp_loanBalance = new Long(item.get("loanBalance").toString());
			loanBalance	= tmp_loanBalance;
		}
		
		if ( item.get("loanbalance") != null )
		{
			Long tmp_loanBalance = new Long(item.get("loanbalance").toString());
			loanBalance	= tmp_loanBalance;
		}
		
		if ( item.get("todayProfitRealized") != null )
		{
			Long tmp_todayProfitRealized = new Long(item.get("todayProfitRealized").toString());
			todayProfitRealized	= tmp_todayProfitRealized;
		}
		if ( item.get("todayprofitrealized") != null )
		{
			Long tmp_todayProfitRealized = new Long(item.get("todayprofitrealized").toString());
			todayProfitRealized	= tmp_todayProfitRealized;
		}
		
		if ( item.get("todayFee") != null )
		{
			Long tmp_todayFee = new Long(item.get("todayFee").toString());
			todayFee	= tmp_todayFee;
		}
		if ( item.get("todayfee") != null )
		{
			Long tmp_todayFee = new Long(item.get("todayfee").toString());
			todayFee	= tmp_todayFee;
		}
		
		if ( item.get("last_bankBalance") != null )
		{
			Long tmp_last_bankBalance = new Long(item.get("last_bankBalance").toString());
			last_bankBalance	= tmp_last_bankBalance;
		}
		if ( item.get("last_bankbalance") != null )
		{
			Long tmp_last_bankBalance = new Long(item.get("last_bankbalance").toString());
			last_bankBalance	= tmp_last_bankBalance;
		}
		
		if ( item.get("last_loanBalance") != null )
		{
			Long tmp_last_loanBalance = new Long(item.get("last_loanBalance").toString());
			last_loanBalance	= tmp_last_loanBalance;
		}
		if ( item.get("last_loanbalance") != null )
		{
			Long tmp_last_loanBalance = new Long(item.get("last_loanbalance").toString());
			last_loanBalance	= tmp_last_loanBalance;
		}
		
		if ( item.get("last_ProfitRealized") != null )
		{
			Long tmp_last_ProfitRealized = new Long(item.get("last_ProfitRealized").toString());
			last_ProfitRealized	= tmp_last_ProfitRealized;
		}
		if ( item.get("last_profitrealized") != null )
		{
			Long tmp_last_ProfitRealized = new Long(item.get("last_profitrealized").toString());
			last_ProfitRealized	= tmp_last_ProfitRealized;
		}
		
		if ( item.get("last_Fee") != null )
		{
			Long tmp_last_Fee = new Long(item.get("last_Fee").toString());
			last_Fee	= tmp_last_Fee;
		}
		if ( item.get("last_fee") != null )
		{
			Long tmp_last_Fee = new Long(item.get("last_fee").toString());
			last_Fee	= tmp_last_Fee;
		}
		
		if ( item.get("enableOvernight") != null )
		{
			Long tmp_enableOvernight = new Long(item.get("enableOvernight").toString());
			enableOvernight	= tmp_enableOvernight;
		}
		if ( item.get("enableovernight") != null )
		{
			Long tmp_enableOvernight = new Long(item.get("enableovernight").toString());
			enableOvernight	= tmp_enableOvernight;
		}
		
		if ( item.get("OvernightDays") != null )
		{
			Long tmp_OvernightDays = new Long(item.get("OvernightDays").toString());
			OvernightDays	= tmp_OvernightDays;
		}
		if ( item.get("overnightdays") != null )
		{
			Long tmp_OvernightDays = new Long(item.get("overnightdays").toString());
			OvernightDays	= tmp_OvernightDays;
		}
		
		
		if ( item.get("securityRate") != null )
		{
			Long tmp_securityRate = new Long(item.get("securityRate").toString());
			securityRate	= tmp_securityRate;
		}
		if ( item.get("securityrate") != null )
		{
			Long tmp_securityRate = new Long(item.get("securityrate").toString());
			securityRate	= tmp_securityRate;
		}
		
		if ( item.get("loanLeverage") != null )
		{
			Long tmp_loanLeverage = new Long(item.get("loanLeverage").toString());
			loanLeverage	= tmp_loanLeverage;
		}
		if ( item.get("loanleverage") != null )
		{
			Long tmp_loanLeverage = new Long(item.get("loanleverage").toString());
			loanLeverage	= tmp_loanLeverage;
		}
		
		if ( item.get("loanMax") != null )
		{
			Long tmp_loanMax = new Long(item.get("loanMax").toString());
			loanMax	= tmp_loanMax;
		}
		if ( item.get("loanmax") != null )
		{
			Long tmp_loanMax = new Long(item.get("loanmax").toString());
			loanMax	= tmp_loanMax;
		}
		
		if ( item.get("losscutLimit") != null )
		{
			Long tmp_losscutLimit = new Long(item.get("losscutLimit").toString());
			losscutLimit	= tmp_losscutLimit;
		}
		if ( item.get("losscutlimit") != null )
		{
			Long tmp_losscutLimit = new Long(item.get("losscutlimit").toString());
			losscutLimit	= tmp_losscutLimit;
		}
		
		if ( item.get("enableShort") != null )
		{
			Long tmp_enableShort = new Long(item.get("enableShort").toString());
			enableShort	= tmp_enableShort;
		}
		
		if ( item.get("enableshort") != null )
		{
			Long tmp_enableShort = new Long(item.get("enableshort").toString());
			enableShort	= tmp_enableShort;
		}
		
		if ( item.get("id") != null )
		{
			if ( item.get("id").toString().equals("test001") )
			{
				runner.PrintDBLog("");
			}
		}
		
		if ( item.get("is_login") != null )
		{
			is_login	= item.get("is_login").toString();
			if ( is_login.equals("false") || is_login.equals("0") )
			{
				is_login = "0";
			} else if ( is_login.equals("true") || is_login.equals("1") )
			{
				is_login = "1";
			}
		}

		if ( item.get("login_fail") != null )
		{		
			Long tmp_login_fail = new Long(item.get("login_fail").toString());
			login_fail	= tmp_login_fail;
		}
		
		if ( item.get("stockOrderLevel") != null )
		{
			Long tmp_stockOrderLevel = new Long(item.get("stockOrderLevel").toString());
			stockOrderLevel	= tmp_stockOrderLevel;
		}
		if ( item.get("stockorderlevel") != null )
		{
			Long tmp_stockOrderLevel = new Long(item.get("stockorderlevel").toString());
			stockOrderLevel	= tmp_stockOrderLevel;
		}

		if ( item.get("etfOrderLevel") != null )
		{
			Long tmp_etfOrderLevel = new Long(item.get("etfOrderLevel").toString());
			etfOrderLevel	= tmp_etfOrderLevel;
		}
		if ( item.get("etforderlevel") != null )
		{
			Long tmp_etfOrderLevel = new Long(item.get("etforderlevel").toString());
			etfOrderLevel	= tmp_etfOrderLevel;
		}
		
		if ( item.get("league") != null )
		{	
			league	= item.get("league").toString();
		}
		
		if ( item.get("useAccountIdx") != null )
		{
			Long tmp_useAccountIdx = new Long(item.get("useAccountIdx").toString());
			useAccountIdx	= tmp_useAccountIdx;
		}
		if ( item.get("useaccountidx") != null )
		{
			Long tmp_useAccountIdx = new Long(item.get("useaccountidx").toString());
			useAccountIdx	= tmp_useAccountIdx;
		}

		if ( useAccountIdx.equals(Long.valueOf(0)) )
		{
			company_bank = runner.GetEnv("ENV_BANK");
			company_account = runner.GetEnv("ENV_BANK_ACCOUNT");
			company_bank_owner = runner.GetEnv("ENV_BANK_OWNER");
		} else if ( useAccountIdx.equals(Long.valueOf(1)) )
		{
			company_bank = runner.GetEnv("ENV_BANK2");
			company_account = runner.GetEnv("ENV_BANK_ACCOUNT2");
			company_bank_owner = runner.GetEnv("ENV_BANK_OWNER2");
		}
		
		/*
		if ( item.get("company_bank") != null )
		{
			company_bank	= item.get("company_bank").toString();
		}
		
		if ( item.get("company_account") != null )
		{
			company_account	= item.get("company_account").toString();
		}
		
		if ( item.get("company_bank_owner") != null )
		{
			company_bank_owner = item.get("company_bank_owner").toString();
		}
		*/
		
		if ( item.get("loanprocess") != null )
		{
			Long tmp_loanprocess = new Long(item.get("loanprocess").toString());
			loanprocess	= tmp_loanprocess;
		}

		if ( item.get("baseBalance") != null )
		{		
			Long tmp_baseBalance = new Long(item.get("baseBalance").toString());
			baseBalance	= tmp_baseBalance;
		}
		if ( item.get("basebalance") != null )
		{		
			Long tmp_baseBalance = new Long(item.get("basebalance").toString());
			baseBalance	= tmp_baseBalance;
		}
		
		if ( item.get("focusing") != null )
		{		
			birthday	= item.get("birthday").toString();
		}
		if ( item.get("focusing") != null )
		{	
			Long tmp_focusing = new Long(item.get("focusing").toString());
			focusing	= tmp_focusing;
		}
		if ( item.get("tradeauth") != null )
		{	
			Long tmp_tradeauth = new Long(item.get("tradeauth").toString());
			tradeauth	= tmp_tradeauth;
		}
		if ( item.get("submanager") != null )
		{	
			submanager	= item.get("submanager").toString();
		}
		if ( item.get("positionlimit") != null )
		{
			Long tmp_positionlimit = new Long(item.get("positionlimit").toString());
			positionlimit	= tmp_positionlimit;
		}
		if ( item.get("outstandinglimit") != null )
		{	
			Long tmp_outstandinglimit = new Long(item.get("outstandinglimit").toString());
			outstandinglimit			= tmp_outstandinglimit;
		}
		if ( item.get("interestcount") != null )
		{	
			Long tmp_interestcount = new Long(item.get("interestcount").toString());
			interestcount = tmp_interestcount;
		}
		if ( item.get("totalBalance") != null )
		{
			Long tmp_totalBalance = new Long(item.get("totalBalance").toString());
			totalBalance = tmp_totalBalance;
		}
		if ( item.get("totalbalance") != null )
		{
			Long tmp_totalBalance = new Long(item.get("totalbalance").toString());
			totalBalance = tmp_totalBalance;
		}
		
		if ( item.get("로스컷여유금") != null )
		{
			Long tmp_로스컷여유금 = new Long(item.get("로스컷여유금").toString());
			로스컷여유금 = tmp_로스컷여유금;
		}
		if ( item.get("로스컷금액") != null )
		{
			Long tmp_로스컷금액 = new Long(item.get("로스컷금액").toString());
			로스컷금액 = tmp_로스컷금액;
		}
		if ( item.get("총평가손익") != null )
		{
			Long tmp_총평가손익 = new Long(item.get("총평가손익").toString());
			총평가손익 = tmp_총평가손익;
		}
		if ( item.get("runlosscut") != null )
		{
			Long tmp_runlosscut = new Long(item.get("runlosscut").toString());
			runlosscut = tmp_runlosscut;
		}
		if ( item.get("totalBalance") == null || item.get("totalbalance") == null )
		{
			totalBalance = bankBalance + loanBalance;
		}	
		
		if ( item.get("filled_sense") != null )
		{
			Long tmp_filled_sense = new Long(item.get("filled_sense").toString());
			체결감도 = tmp_filled_sense;
		} else {
			Long tmp_data = new Long(runner.GetEnv("ENV_CONTRACT_SENSITIVITY_STOCK"));
			체결감도 = tmp_data;
		}
		if ( item.get("filled_option") != null )
		{
			Long tmp_filled_option = new Long(item.get("filled_option").toString());
			체결방식 = tmp_filled_option;
		} else {
			Long tmp_data = new Long(runner.GetEnv("ENV_FILLED"));
			체결방식 = tmp_data;
		}
		if ( item.get("buy_profit") != null )
		{
			Long tmp_buy_profit = new Long(item.get("buy_profit").toString());
			매수손익적용 = tmp_buy_profit;
		} else {
			Long tmp_data = new Long(runner.GetEnv("ENV_BUY_PROFIT"));
			매수손익적용 = tmp_data;
		}
		if ( item.get("max_item") != null )
		{
			Long tmp_max_item = new Long(item.get("max_item").toString());
			종목당최대금액 = tmp_max_item;
		} else {
			Long tmp_max_item = new Long(runner.GetEnv("ENV_MAX_ITEM"));
			종목당최대금액 = tmp_max_item;
		}
	}
	
	public String toJson()
	{
		HashMap<String, Object> item = toMap();
		String data = runner.gson.toJson(item);
		return data;
	}
	
	public HashMap<String, Object> toMap()
	{
		HashMap<String, Object> item = new HashMap<String, Object>(); 			
		item.put("id", id);
		item.put("name", name);
		item.put("password", password);
		item.put("email", email);
		item.put("mobile", mobile);
		item.put("bank", bank);
		item.put("bankAccount", bankAccount);
		item.put("bankOwner", bankOwner);
		item.put("tax", tax.toString());
		item.put("fee_processing", fee_processing.toString());
		item.put("fee_trading", fee_trading.toString());
		item.put("fee_closing", fee_closing.toString());
		item.put("fee_losscut", fee_losscut.toString());
		item.put("fee_overnight", fee_overnight.toString());
		item.put("tax2", tax2.toString());
		item.put("fee_processing2", fee_processing2.toString());
		item.put("fee_trading2", fee_trading2.toString());
		item.put("fee_closing2", fee_closing2.toString());
		item.put("fee_losscut2", fee_losscut2.toString());
		item.put("fee_overnight2", fee_overnight2.toString());
		item.put("ClientLevelTypeID", ClientLevelTypeID.toString());
		item.put("AccountStateTypeID", AccountStateTypeID.toString());
		item.put("recommender", recommender);
		item.put("registerDate", registerDate);
		item.put("bankBalance", bankBalance.toString());
		item.put("Balance", bankBalance.toString());
		item.put("loanBalance", loanBalance.toString());
		item.put("todayProfitRealized", todayProfitRealized.toString());
		item.put("todayFee", todayFee.toString());
		item.put("last_bankBalance", last_bankBalance.toString());
		item.put("last_loanBalance", last_loanBalance.toString());
		item.put("last_ProfitRealized", last_ProfitRealized.toString());
		item.put("last_Fee", last_Fee.toString());
		item.put("enableOvernight", enableOvernight.toString());
		item.put("OvernightDays", OvernightDays.toString());
		item.put("securityRate", securityRate.toString());
		item.put("loanLeverage", loanLeverage.toString());	
		item.put("loanMax", loanMax.toString());
		item.put("losscutLimit", losscutLimit.toString());
		item.put("enableShort", enableShort.toString());
		item.put("is_login", is_login);
		item.put("login_fail", login_fail.toString());
		item.put("stockOrderLevel", stockOrderLevel.toString());
		item.put("etfOrderLevel", etfOrderLevel.toString());
		item.put("league", league);
		item.put("useAccountIdx", useAccountIdx.toString());
		item.put("company_bank", company_bank);
		item.put("company_account", company_account);
		item.put("company_bank_owner", company_bank_owner.toString());
		item.put("loanprocess", loanprocess.toString());
		item.put("baseBalance", baseBalance.toString());		
		item.put("birthday", birthday);
		item.put("focusing", focusing.toString());
		item.put("tradeauth",tradeauth.toString());
		item.put("submanager", submanager);
		item.put("positionlimit", positionlimit.toString());
		item.put("outstandinglimit", outstandinglimit.toString());
		item.put("interestcount", interestcount.toString());
		item.put("totalBalance", totalBalance.toString());
		item.put("로스컷여유금", 로스컷여유금.toString());
		item.put("로스컷금액", 로스컷금액.toString());
		item.put("총평가손익", 총평가손익.toString());
		item.put("runlosscut", runlosscut.toString());
		item.put("filled_sense", 체결감도.toString());
		item.put("filled_option", 체결방식.toString());
		item.put("buy_profit", 매수손익적용.toString());
		item.put("max_item", 종목당최대금액.toString());
		return item;
	}
	
	public void SaveMemory()
	{
		try {
			runner.redisTemplate.opsForHash().put("USER_INFO", id, toJson());
		} catch ( Exception e )
		{
			runner.PrintLogForce("SaveMemory-E-" + e.getMessage());
		}
	}
	
	public void UpdateProfit()
	{
		try {
	    	Long eval = new Long(0);
	    	총평가손익 = Long.valueOf(0);
			Set<String> sets = positions.keySet();
			if ( sets != null )
			{			
				
				Iterator<String> keys = sets.iterator();
				while ( keys.hasNext() )
				{
					Position obj = positions.get(keys.next());
					if ( obj != null )
					{
						obj.SetProfit(false, true);
						eval += obj.profit;
					}
				}
				총평가손익 = eval;
				//runner.PrintLogForce("총평가손익:" + 총평가손익.toString());
			}	
		} catch ( Exception e )
		{
			runner.PrintLogForce("UpdateProfit-Exception-" + toJson()); 
		}
	}
	
	public void UpdateProfitRealTime()
	{
		try {
			//if ( id.equals("test001") )
			{
				총평가손익 = Long.valueOf(0);
		    	Long eval = new Long(0);
				Set<String> sets = positions.keySet();
				if ( sets != null )
				{			
					Iterator<String> keys = sets.iterator();
					while ( keys.hasNext() )
					{
						Position obj = positions.get(keys.next());
						if ( obj != null )
						{
							eval += obj.profit;
						}
					}
					총평가손익 = eval;
				}	

				//runner.PrintLogForce("총평가손익:" + 총평가손익.toString()); 
			}
		} catch ( Exception e )
		{
			runner.PrintLogForce("UpdateProfit-Exception-" + toJson()); 
		}
	}
	
	public Long GetPNL()
	{
		총평가손익 = Long.valueOf(0);
    	Long eval = new Long(0);
		Set<String> sets = positions.keySet();
		if ( sets != null )
		{			
			Iterator<String> keys = sets.iterator();
			while ( keys.hasNext() )
			{
				Position obj = positions.get(keys.next());
				if ( obj != null )
				{
					eval += obj.profit;
				}
			}
			총평가손익 = eval;
		}	
		//runner.PrintLogForce("총평가손익:" + 총평가손익.toString()); 
		return 총평가손익;
	}
	
	public Order GetOrder( Long ord_no )
	{
		return orders.get( ord_no );
	}
	
	public Order AddOrder( Order order )
	{
		return orders.put(order.ordNum, order);
	}
	
	public Order RemoveOrder( Long ord_no )
	{
		return orders.remove( ord_no );
	}
	
	public OutStanding AddOutStandingFromDB( OutStanding out )
	{
		String date = utils.GetDate();
		HashMap<String, Object> order = runner.mapper.GetOrder(date, out.ordNum );
		if ( order != null )
		{
			Order new_order = new Order( runner.order_mgr, order, false );
			AddOrder(new_order);
		} else {
			runner.PrintLogForce("주문번호 없음 : " + out.ordNum.toString() );
		}
		return outstandings.put(out.ordNum, out);
	}
	
	public OutStanding AddOutStanding( OutStanding out )
	{
		return outstandings.put(out.ordNum, out);
	}	
	
	public OutStanding UpdateOutStanding( Order new_order )
	{
		return outstandings.remove(new_order.ordNumOrg);
	}
	
	public OutStanding RemoveOutStanding( Long org_no )
	{
		return outstandings.remove(org_no);
	}
	
	public Position AddPosition( Position pos )
	{
		return positions.put(pos.code, pos);
	}
	
	public Position RemovePosition( String symbol )
	{
		return positions.remove(symbol);
	}
	
	public OutStanding GetOutStanding ( Long org_no )
	{
		return outstandings.get(org_no);
	}
	
	public Long TotalOutStandingVolume(String symbol)
	{
		Long result = new Long(0);
		Set<Long> sets = outstandings.keySet();
		if ( sets != null )
		{
			Iterator<Long> keys = sets.iterator(); 
			while ( keys.hasNext() )
			{
				OutStanding obj = outstandings.get(keys.next());
				if ( obj != null && obj.code.equals(symbol) )
				{
					if ( obj.PositionTypeID.equals(utils.POSITION_SELL))
						result = result + obj.volOutstd;
				}
			}
		}
		return result;
	}
	
	public String toOutStandingString()
	{
		String result = new String("미체결:");
		Set<Long> sets = outstandings.keySet();
		if ( sets != null )
		{
			Iterator<Long> keys = sets.iterator(); 
			while ( keys.hasNext() )
			{
				OutStanding obj = outstandings.get(keys.next());
				if ( obj != null )
				{
					result = result + String.format("[%s]", obj.name);
				}
			}
		}
		return result;
	}
	
	public String toPositionString()
	{
		String result = new String("포지션:");
		Set<String> sets = positions.keySet();
		if ( sets != null )
		{			
			Iterator<String> keys = sets.iterator(); 
			while ( keys.hasNext() )
			{
				Position obj = positions.get(keys.next());
				if ( obj != null )
				{
					result = result + String.format("[%s]", obj.name);
				}
			}
		}
		return result;
	}
	
	class DescendingOutStading implements Comparator<OutStanding> 
	{ 
		@Override public int compare(OutStanding a, OutStanding b) 
		{ 
			return a.ordNum.compareTo(b.ordNum); 	
		} 
	}
	
	public void SendOutStanding()
	{
		try {
			HashMap<String, Object>	data = new LinkedHashMap<String, Object>();
			ArrayList<LinkedHashMap<String, Object>> datalist = new ArrayList<LinkedHashMap<String, Object>>();
			
			ArrayList<OutStanding> sorted_outstanding = new ArrayList<OutStanding>();
			
			Set<Long> sets = outstandings.keySet();
			if ( sets != null )
			{
				Iterator<Long> keys = sets.iterator(); 
				while ( keys.hasNext() )
				{
					Long key = keys.next();
					if ( outstandings.containsKey(key))
					{
						OutStanding obj = outstandings.get(key);
						if ( obj != null )
						{
							sorted_outstanding.add(obj);
						}
					}
				}
				
				Collections.sort(sorted_outstanding, new DescendingOutStading());
				for ( int i=0; i<sorted_outstanding.size(); i++)
				{
					OutStanding obj = sorted_outstanding.get(i);
					LinkedHashMap<String, Object> map = obj.toPrint();
					if ( map.get("PositionTypeID") != null )
					{
						Long Pt = Long.valueOf(map.get("PositionTypeID").toString());
						if ( Pt.equals(utils.POSITION_BUY) )
						{
							map.put("PositionTypeID", "매수");
						} else {
							map.put("PositionTypeID", "매도");
						}
					}
					if ( map.get("HogaTypeID") != null )
					{
						Long HogaTypeID = new Long(map.get("HogaTypeID").toString());
						map.put("HogaTypeID", utils.GetHogaType(HogaTypeID));
					}
					datalist.add(map);
				}
				
			}
			data.put("tr", utils.MTS_UPDATE_OUTSTANDINGS);
			data.put("id", id);
			data.put("datalist", datalist);
			
			
			//if ( is_login.equals("1") )
			{
				runner.Send_Response(runner.environment.getProperty("client.feed"), data);
			}
			//runner.Send_Response(runner.environment.getProperty("feed_all.exchange"), data);
			
			//runner.Send_Response(runner.environment.getProperty("feed_all.exchange"), data);
			
			runner.Send2MQLedger( data );
			
			CheckActiveUsers();
		} catch ( Exception e )
		{
			runner.PrintLogForce("SendOutStanding-ERROR");
		}
		
	}
	
    public Long CalEvaluation()
    {
    	Long eval = new Long(0);
		Set<String> sets = positions.keySet();
		if ( sets != null )
		{			
			Iterator<String> keys = sets.iterator();
			while ( keys.hasNext() )
			{
				Position obj = positions.get(keys.next());
				if ( obj != null )
				{
					obj.SetProfit(false, true);
					eval += obj.profit;
				}
			}
		}
    	return eval;
    }
    
    public void CalLosscutRemain()
    {
    	try {
			if ( losscutLimit > 0 && loanBalance > 0 )
			{
				BigDecimal tmp = new BigDecimal("0");
				tmp = BigDecimal.valueOf(loanBalance).multiply(BigDecimal.valueOf(losscutLimit).multiply(BigDecimal.valueOf(0.001)));
				로스컷금액 = tmp.longValue();
				로스컷여유금 = bankBalance - tmp.longValue() + GetPNL();
			} else {
				로스컷여유금 = Long.valueOf(0);
			}
			SaveMemory();
    	} catch ( Exception e )
    	{
    		runner.PrintLogForce("CalLosscutRemain-E-" + e.getMessage());
    	}
    }
    
	public void SendProfitNLoss()
	{
		//if ( id.equals("test992") )
		{
			Long tmp_real_tot_eval = todayProfitRealized - todayFee;
			Long tmp_총평가손익 = 총평가손익 + totalBalance;
			HashMap<String, Object>	data = new HashMap<String, Object>();
			data.put("tr", utils.MTS_UPDATE_EVALUATION);
			data.put("id", id);
			data.put("loanBalance", loanBalance.toString());
			data.put("totalBalance", totalBalance.toString());
			data.put("loanLeverage", loanLeverage.toString());
			data.put("loanMax", loanMax.toString());
			data.put("losscutLimit", losscutLimit.toString());
			data.put("loanprocess", loanprocess.toString());

			if ( 로스컷금액 > 0 )
			{
				data.put("losscut", "0");
			} else {
				data.put("losscut", "1");
			}
			data.put("balance", tmp_총평가손익.toString());
			data.put("bankBalance", bankBalance.toString());
			data.put("todayfee", todayFee.toString());
			data.put("real_tot_eval", tmp_real_tot_eval.toString());
			data.put("tot_eval", 총평가손익.toString());
			data.put("totalbalance", totalBalance.toString());
			data.put("userbalance", bankBalance.toString());
			data.put("로스컷여유금", 로스컷여유금.toString());
			//runner.PrintLogForce("totalBalance:" + totalBalance.toString());
			
			if ( enableOvernight.longValue() == 0 )
				data.put("overnight", "오버나잇불가");
			else if ( enableOvernight.longValue() == 1 )
				data.put("overnight", "오버나잇가능");
			
			if ( !이전총평가손익.equals(총평가손익) )
				runner.Send_Response("tiger.pnl", data);
			이전총평가손익 = 총평가손익;
		}
	}
	
	public Long GetBankBalance()
	{
		Long result = new Long(0);
		result = bankBalance + 총평가손익;
		return result;
	}
	
	public Long GetTotalBalance()
	{
		Long result = new Long(0);
		result = totalBalance + 총평가손익;
		return result;
	}
	
	public Long Get로스컷여유금()
	{
		Long result = new Long(0);
		result = 로스컷여유금 + 총평가손익;
		return result;
	}
	
	/*
	public Long Get당일실현손익()
	{
		Long result = new Long(0);
		result = todayProfitRealized  - todayFee;
		return result;
	}
	*/
	
	public boolean SendPosition(boolean bSendFeed)
	{
		boolean bResult = false;

		HashMap<String, Object>	data = new HashMap<String, Object>();
		ArrayList<HashMap<String, Object>> datalist = new ArrayList<HashMap<String, Object>>();
		Set<String> sets = positions.keySet();
		
		총평가손익 = Long.valueOf(0);
		Long real_tot_eval = new Long(0);
		if ( sets != null )
		{			
			Iterator<String> keys = sets.iterator(); 
			while ( keys.hasNext() )
			{
				Position obj = positions.get(keys.next());
				if ( obj != null )
				{
					obj.SetProfit(false, false);
					HashMap<String, Object> map = obj.toMap();
					if ( map.get("PositionTypeID") != null )
					{
						Long Pt = Long.valueOf(map.get("PositionTypeID").toString());
						if ( Pt.equals(utils.POSITION_BUY) )
						{
							map.put("PositionTypeID", "매수");
						} else {
							map.put("PositionTypeID", "매도");
						}
					}
					if ( map.get("HogaTypeID") != null )
					{
						Long HogaTypeID = new Long(map.get("HogaTypeID").toString());
						map.put("HogaTypeID", utils.GetHogaType(HogaTypeID));
					}

					datalist.add(map);
					real_tot_eval += obj.profit;
				}
			}
		}
		총평가손익 = real_tot_eval;
		
		//todayProfitRealized = todayProfitRealized - todayFee;
		Long nResult = runner.mapper.UpdateClientProfit( bankBalance, todayProfitRealized, todayFee, id );
		if ( nResult > 0 )
		{
			//runner.PrintLogForce("담보금3:" + bankBalance.toString());
			
			CalLosscutRemain();		
			data.put("tr", utils.MTS_UPDATE_POSITIONS);
			data.put("id", id);
			data.put("datalist", datalist);
			data.put("real_tot_eval", 총평가손익.toString());		//당일실현손익
			data.put("todayfee", todayFee.toString());
			data.put("tot_eval", todayProfitRealized.toString());
			data.put("datalist", datalist);
			data.put("balance", bankBalance.toString());
			data.put("loanBalance", loanBalance.toString());
			data.put("totalBalance", totalBalance.toString());
			data.put("losscutLimit", losscutLimit.toString());
			data.put("로스컷여유금", 로스컷여유금.toString());	
			SaveMemory();
			//if ( is_login.equals("1") )
			{
				runner.Send_Response(runner.environment.getProperty("client.feed"), data);
			}
			if ( bSendFeed )
				runner.Send2MQLedger( data );
				//runner.Send_Response(runner.environment.getProperty("feed_all.exchange"), data);
			
			bResult = true;
		}

		CheckActiveUsers();
		
		return bResult;
	}
	
	public void SendMessageToUser( String msg, String sub_tr )
	{
		//if ( is_login.equals("1") )
		{
			HashMap<String, Object>	data = new HashMap<String, Object>();
			data.put("tr", "9999");
			data.put("sub-tr", sub_tr);
			data.put("id", id);
			data.put("message", msg);
			runner.Send_Response(runner.environment.getProperty("client.feed"), data);
		}
	}
	
	public void SendMessageStopLoss( String code, String stoptype, String tick )
	{
		HashMap<String, Object>	data = new HashMap<String, Object>();
		data.put("tr", utils.MTS_UPDATE_STOPLOSS);
		data.put("id", id);
		ArrayList<HashMap<String, Object>> arraylist = new ArrayList<HashMap<String, Object>>();
		Object[] array = stoploss_list.keySet().toArray();
		for ( int i=array.length-1; i>=0; i--)
		{
			String symbol = array[i].toString();
			if ( stoploss_list.get(symbol) != null )
			{
				StopLoss stoploss = stoploss_list.get(symbol);
				HashMap<String, Object> stoploss_item = new HashMap<String, Object>();
				stoploss_item.put("symbol", stoploss.code);
				stoploss_item.put("earntick", stoploss.earntick.toString());
				stoploss_item.put("losstick", stoploss.losstick.toString());
				arraylist.add(stoploss_item);
			}
		}
		data.put("datalist", arraylist);
		String msg = String.format("스탑로스 실행 - 종목[%s] 구분[%s] 설정틱[%s]\r\n설정된 스탑로스는 해제됩니다.\r\n자세한 내역은 주문내역에서 확인하세요.", code, stoptype, tick );
		data.put("message", msg);
		runner.Send_Response(runner.environment.getProperty("client.feed"), data);
	}
	
	public void SendOrderStatus ( HashMap<String, Object> data )
	{
		//음성 전송을 위한 STATUS
		runner.Send_Response(runner.environment.getProperty("client.feed"), data);
	}
	
	public void SendMessageToUserOverNight5Before(Date now)
	{
		//if ( is_login.equals("1") )
		{
			String now_korea = utils.GetDateKorean(now);
			HashMap<String, Object>	data = new HashMap<String, Object>();
			data.put("tr", "9999");
			data.put("id", id);
			data.put("message", "오버나잇실행 시간:" + now_korea + "\r\n오버나잇 5분전입니다.\r\n미체결내역은 강제취소되며\r\n보유금액에 따라 일부 주문이 반대매도 될수 있습니다.");
			runner.Send_Response(runner.environment.getProperty("client.feed"), data);
		}
	}
	
	public void SendMessageToUserSystemExit(Date now)
	{
		HashMap<String, Object>	data = new HashMap<String, Object>();
		data.put("tr", utils.MTS_EMERGENCY_MESSAGE);
		data.put("id", id);
		data.put("message", "정산처리를 위하여 시스템을 종료합니다. 재접속후 이용하세요.");
		runner.Send_Response("tiger.pnl", data);
	}
	
    public Long 총매입금액 ()
    {
    	Long 총금액 = new Long(0);
		Set<String> sets = positions.keySet();
		if ( sets != null )
		{			
			Iterator<String> keys = sets.iterator();
			while ( keys.hasNext() )
			{
				Position obj = positions.get(keys.next());
				if ( obj != null )
				{
					총금액 = 총금액  + obj.amount;
				}
			}
		}
    	return 총금액;
    }
    
    public void CheckActiveUsers()
    {
    	if ( positions.size() == 0 && outstandings.size() == 0 )
    	{
    		User_Info active_user = runner.user_mgr.active_user_list.get(id);
    		if ( active_user != null )
    		{
    			runner.user_mgr.active_user_list.remove(id);
    		}
    	} else {
    		User_Info active_user = runner.user_mgr.active_user_list.get(id);
    		if ( active_user == null )
    		{
    			runner.user_mgr.active_user_list.put(id, this);
    		}
    	}
    	//runner.PrintLogForce(String.format("Active-User-Count:%d", runner.user_mgr.active_user_list.size()));
    }
}

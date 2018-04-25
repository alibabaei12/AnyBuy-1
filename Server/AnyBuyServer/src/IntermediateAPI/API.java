package IntermediateAPI;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import SQLControl.SQLOperation;

public class API {
	
	static Server s;
	static int failCounter = 0;
	
	public static void main (String[] args) {
		try {
			writeLog("Starting Server");
			s = new Server();
		} catch (IOException e) {
			failCounter++;
			writeLog("Server failed.");
			if (failCounter < 5) main(null);
			else System.exit(0);
		}
	}
	
	public static String getCommand (String str) throws SQLException {
		String[] strArr;
		if (str.length() >= 4) strArr = str.split("\\&");
		else return illegalInput();
		if (strArr[0].equalsIgnoreCase("reg")) return register(voidHead(strArr));
		else if (strArr[0].equalsIgnoreCase("lgi")) return login(voidHead(strArr));
		else if (strArr[0].equalsIgnoreCase("plo")) return placeOrder(voidHead(strArr));
		else if (strArr[0].equalsIgnoreCase("gvr")) return giveRate(voidHead(strArr));
		else if (strArr[0].equalsIgnoreCase("cco")) return cancelOrder(voidHead(strArr));
		else if (strArr[0].equalsIgnoreCase("art")) return acceptRate(voidHead(strArr));
		else if (strArr[0].equalsIgnoreCase("adc")) return addCard(voidHead(strArr));
		else return illegalInput();
		
	}
	
	static String register (String[] str) throws SQLException {
		writeLog("Register");
		if (str.length < 1) return "0x1A06";
		String[] str2 = str[0].split("\\?");
		String[] uInfo = str2[0].split("\\@");
		if (str2.length != 2 || uInfo.length != 2) return "0x1A01";
		Connection c = SQLControl.SQLOperation.getConnect("userInfo", "anybuy", "CMPS115.");
		String emailDomainCode = SQLControl.SQLOperation.readDatabase(c, "select code from domainCode"
				+ " where emailDomain='" + uInfo[1] + "'");
		if (emailDomainCode == null) {
			emailDomainCode = UserManage.createDomainCode(c, uInfo[1]);
		}
		if (emailDomainCode == "0x1A07") return "0x1A07";
		emailDomainCode = SQLControl.SQLOperation.readDatabase(c, "select code from domainCode"
				+ " where emailDomain='" + uInfo[1] + "'");
		String usr = SQLControl.SQLOperation.readDatabase(c, "select psc from " + emailDomainCode + " where name='" + uInfo[0] + "'");
		if (usr != null) return "0x1A08";
		int uid = SQLOperation.countLine(c, emailDomainCode) + 10000;
		String sql = "INSERT INTO " + emailDomainCode + "(name,psc,id) VALUES('" + uInfo[0] + "','" + str2[1] + "','" + uid + "');";
		SQLControl.SQLOperation.writeData(c, sql);
		SQLOperation.creatProfile(c, emailDomainCode + "" + uid);
		c.close();
		return "0x01";
	}
	
	static String login (String[] str) throws SQLException {
		writeLog("Login");
		String[] str2 = str[0].split("\\?");
		String[] uInfo = str2[0].split("\\@");
		Connection c = SQLControl.SQLOperation.getConnect("userInfo", "anybuy", "CMPS115.");
		String sql = "select code from domainCode where emailDomain='" + uInfo[1] + "'";
		String emailCode = SQLControl.SQLOperation.readDatabase(c, sql);
		sql = "select id from " + emailCode + " where name='" + uInfo[0] + "'";
		String uid = SQLControl.SQLOperation.readDatabase(c, sql);
		if (emailCode == null) return "0x1C01";
		sql = "select psc from " + emailCode + " where name='" + uInfo[0] + "'";
		System.out.println(sql);
		if (str2[1].equals(SQLControl.SQLOperation.readDatabase(c, sql)) ) {
			c.close();
			int authToken = (int) (Math.random() * 10 * 0xFFFF);
			// TODO improve authToken algorithm to make it has a high security level.
			c = SQLControl.SQLOperation.getConnect("accessLog", "anybuy", "CMPS115.");
			String usrStatus = SQLControl.SQLOperation.readDatabase(c, "select token from domainCode where emailDomain='" + uInfo[1] + "'");
			if (usrStatus == null) sql = "insert into authLog (uid, authTime, token) values ('" + emailCode + uid + "','','" + authToken + "');";
			SQLControl.SQLOperation.writeData(c, sql);
			String sessionID = emailCode + uid + "?" + authToken;
			return sessionID;
		} else {
			c.close();
			return "0x1C02";
		}
	}
	
	static String placeOrder (String[] str) {
		writeLog("Place Order");
		return null;
	}
	
	static String giveRate (String[] str) {
		writeLog("Give Rate");
		return null;
	}
	
	static String cancelOrder (String[] str) {
		writeLog("Cancel Order");
		return null;
	}
	
	static String acceptRate (String[] str) {
		writeLog("Accept Rate");
		return null;
	}
	
	static String addCard (String[] str) {
		writeLog("Add Payment Method");
		return null;
	}
	
	static String illegalInput() {
		writeLog("Illegal Input.");
		return null;
	}
	
	static void writeLog (String str) {
		System.out.println(str);
	}
	
	static String[] voidHead (String [] str) {
		String[] res = new String[str.length - 1];
		for (int i = 1; i < str.length; i++) res[i - 1] = str[i];
		return res;
	}
}

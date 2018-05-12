package IntermediateAPI;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import Object.LinkedList;
import Object.Node;
import Object.Order;
import Object.User;
import ServerManagement.FileRecivier;
import SQLControl.SQLOperation;

public class CoreOperations {

	private static FileRecivier server;
	private static User userObj = new User();
	private static Order orderObj = new Order();

	static String register (LinkedList ll) throws SQLException {
		// This linked list should have length of 1. The Node should contains a user object.
		writeLog("Register");
		Object o = ll.head.getObject();
		if (!o.getClass().equals(userObj)) return "0x1002";
		User u = (User) o;
		Connection c = SQLControl.SQLOperation.getConnect("userInfo");
		String emailDomainCode = SQLControl.SQLOperation.readDatabase(c, "select code from domainCode"
				+ " where emailDomain='" + u.getDomain() + "'");
		if (emailDomainCode == null) {
			emailDomainCode = ServerManagement.UserManage.createDomainCode(c, u.getDomain());
		}
		if (emailDomainCode == "0x1A07") return "0x1A07";
		emailDomainCode = SQLControl.SQLOperation.readDatabase(c, "select code from domainCode"
				+ " where emailDomain='" + u.getDomain() + "'");
		String usr = SQLControl.SQLOperation.readDatabase(c, "select psc from " + emailDomainCode + " where name='" + u.getUserName() + "'");
		if (usr != null) return "0x1A08";
		int uid = SQLOperation.countLine(c, emailDomainCode) + 10000;
		String sql = "INSERT INTO " + emailDomainCode + "(name,psc,id) VALUES('" + u.getUserName() + "','" + u.getPassword() + "','" + uid + "');";
		SQLControl.SQLOperation.updateData(c, sql);
		SQLOperation.createProfile(c, emailDomainCode + "" + uid);
		c.close();
		return "0x01";
	}
	
	static String login (LinkedList ll) throws SQLException {
		// This linked list should have length of 1. The Node should contains a user object.
		writeLog("Login");
		Object o = ll.head.getObject();
		if (!o.getClass().equals(userObj)) return "0x1002";
		User u = (User) o;
		Connection c = SQLControl.SQLOperation.getConnect("userInfo");
		String sql = "select code from domainCode where emailDomain='" + u.getDomain() + "'";
		String emailCode = SQLControl.SQLOperation.readDatabase(c, sql);
		sql = "select id from " + emailCode + " where name='" + u.getUserName() + "'";
		String uid = SQLControl.SQLOperation.readDatabase(c, sql);
		if (emailCode == null) return "0x1C01";
		sql = "select psc from " + emailCode + " where name='" + u.getUserName() + "'";
		System.out.println(sql);
		if (u.getPassword().equals(SQLControl.SQLOperation.readDatabase(c, sql)) ) {
			c.close();
			int authToken = (int) (Math.random() * 10 * 0xFFFF);
			// TODO improve authToken algorithm to make it has a high security level.
			c = SQLControl.SQLOperation.getConnect("accessLog");
			sql = "select token from authLog where uid='" + emailCode + uid + "'";
			String usrStatus = SQLControl.SQLOperation.readDatabase(c, sql);
			if (usrStatus == null) {
				sql = "insert into authLog (uid, authTime, token) values ('" + emailCode + uid + "','" + System.currentTimeMillis() + "','" + authToken + "');";
				SQLControl.SQLOperation.updateData(c, sql);
			}
			else {
				sql = "update authLog set authTime='" + System.currentTimeMillis() + "' where uid='" + emailCode + uid + "';" ;
				SQLControl.SQLOperation.updateData(c, sql);
				sql = "update authLog set token='" + authToken + "' where uid='" + emailCode + uid + "';" ;
				SQLControl.SQLOperation.updateData(c, sql);
			}
			String sessionID = emailCode + uid + "?" + authToken;
			System.out.println(authToken);
			return sessionID;
		} else {
			c.close();
			return "0x1C02";
		}
	}
	
	static String placeOrder (LinkedList ll) throws SQLException {
		
		/**
		 * This LinkedList should includes at least 2 Node. 
		 * The first Node should contains sessionID.
		 * Starting from the second node, each of them should includes an Order object.
		 * plo&sessionID&<Country>?<Product>?<Brand>?<Image>?<Quantity>
		 */
		
		writeLog("Place Order");
		
		Node temp = ll.head;
		LinkedList orderList = new LinkedList();
		
		if (!temp.getClass().equals("".getClass())) return "0x1003";
		String uid = sessionVerify((String)temp.getObject());
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		API.voidHead(ll);
		
		temp = ll.head;
		while (temp != null){
			Object o = temp.getObject();
			if (!o.getClass().equals(orderObj)) return "0x1002";
			Order od = (Order)o;
			orderList.insert(od);
			temp = temp.getNext();
		}
		
		temp = orderList.head;
		while (temp != null) {
			Order obj = (Order)temp.getObject();
			long time = System.currentTimeMillis();
			
			// make string for INSERT buy order into generalOrder
			Connection c = SQLControl.SQLOperation.getConnect("generalOrder");
			
			// make new table for country if needed
			String countryStatus = SQLControl.SQLOperation.readDatabase(c, "SELECT * FROM" + obj.getCountry());
			if (countryStatus == null) {
				SQLControl.SQLOperation.createCountryTable(c, obj.getCountry());
			}
			
			String orderID = obj.getCountry() + (SQLOperation.countLine(c, obj.getCountry()) + 10000);
			boolean imageExist = (obj.getImage() != null);
			
			String value = "'" + obj.getProduct() + "','" + obj.getBrand() + "','" + imageExist + "','" + obj.getQuantity() + "','" + time + "','" + orderID + "'";
			String sql = "INSERT INTO " + obj.getCountry() +" (Product, Brand, Image, Quantity, orderTime, orderID) VALUES (" + value + ");"; 
			
			
			// insert data into table
			System.out.println(SQLOperation.updateData(c, sql));
			
			// get orderID that was just INSERT'ed
			
			// make string for INSERT orderID into user's account
			c.close();
			c = SQLControl.SQLOperation.getConnect(uid);
			sql = "INSERT INTO `order` (`orderID`) VALUES ('" + orderID + "');";
			System.out.println(SQLOperation.updateData(c, sql));

			c.close();
			return acceptImage(obj.getImage(), orderID);
		}
	}
	
	
	public static String acceptImage(File img, String orderID) {
		System.out.println("image process started.");
		try {  
//			server = new FileRecivier();
//            server.load();
            ServerManagement.Task.setID(orderID);
            ServerManagement.Task.setImage(img);
        } catch (Exception e) {  
            return "0x1F04";
        }
		return "0x01";
	}
	
	static String loadOrder (String[] str) throws SQLException {
		/* POTENTIALLY USELESS CODE
		// ldo&sessionID&<orderID>?<Country>
		writeLog("Load Order");
		
		// verify session
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		
		// get data for buy order
		String[] order = str[1].split("\\?");
		String orderID = order[0];
		String country = order[1];
		
		Connection c = SQLOperation.getConnect("generalOrder");
		String sql = "SELECT * FROM " + country + " where orderID = '" + orderID + "'";
		ResultSet rs = SQLOperation.readDatabaseRS(c, sql);
		String res = generateResWithRS(rs, 4);
		c.close();
		
		if (res.equals("")) return "0x1F02"; // Order not found
		else return res;
		*/
		writeLog("Load Order");
		return null;
	}
	
	static String cancelOrder (String[] str) {
		// cco&sessionID&<orderID>
		writeLog("Cancel Order");
		return "0x01";
	}
	
	// Load list of buy orders (for sell page / for profile page?)
	static String loadOrderList (String[] str) throws SQLException {
		// ldl&sessionID&<Country>
		writeLog("Load Order List");
		
		// verify session
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		
		// country to load orders from
		String country = str[1];
		
		// load orders from country table
		Connection c = SQLOperation.getConnect("generalOrder");
		String sql = "SELECT Product, Brand, Quantity, Image, orderID FROM " + country;
		ResultSet rs = SQLOperation.readDatabaseRS(c, sql);
		String res = generateResWithRS(rs, 5);
		c.close();
		
		if (res.equals("")) return "0x1F03"; // Country table not found
		else return res;
	}
	
	static String giveRate (String[] str) {
		writeLog("Give Rate");
		return null;
	}
	
	static String acceptRate (String[] str) {
		writeLog("Accept Rate");
		return null;
	}
	
	static String addCard (String[] str) throws SQLException {
		//adc&snok10000?538847&yoona?lim&amex=375987654321001&1220?95064
		
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		
		
		String[] name = str[1].split("\\?");
		String[] cardNum = str[2].split("\\=");
		String[] expInfo = str[3].split("\\?");
		
		Connection c = SQLOperation.getConnect(uid);
		String cardStatus = SQLControl.SQLOperation.readDatabase(c, "select issuer from payment where cardNumber='" + cardNum[1] + "'");
		if (cardStatus != null) {
			c.close();
			return "0x1E01";
		}
		
		cardStatus = validateCardInfo(name, cardNum, expInfo);
		if ( !cardStatus.equals("0x01") ) {
			c.close();
			return cardStatus;
		}
		
		String value = "'" + name[0] + "','" + name[1] + "','" + cardNum[0] + "','" + cardNum[1] + "','" + expInfo[0] + "','" + expInfo[1] + "'";
		String sql = "INSERT INTO payment(fn, ln, issuer, cardNumber, exp, zip) VALUES(" + value + ");"; 
		System.out.println(SQLOperation.updateData(c, sql));
		c.close();
		return "0x01";
	}
	
	static String loadCard (String[] str) throws SQLException {
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		Connection c = SQLOperation.getConnect(uid);
		String sql = "SELECT * FROM payment";
		ResultSet rs = SQLOperation.readDatabaseRS(c, sql);
		String res = generateResWithRS(rs, 6);
		c.close();
		if (res.equals("")) return "0x1E04";
		else return res;
	}
	
	static String deleteCard(String[] str) throws SQLException {
		//dlc&sid&card#
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		String sql = "delete from payment where cardNumber=" + str[1] + ";";
		Connection c = SQLControl.SQLOperation.getConnect(uid);
		String cardStatus = SQLControl.SQLOperation.readDatabase(c, "select issuer from payment where cardNumber='" + str[1] + "'");
		if (cardStatus == null) {
			c.close();
			return "0x1E04";
		}
		String res = SQLControl.SQLOperation.updateData(c, sql);
		c.close();
		if (res != "UPS") return res;
		else return "0x01";
	}
	
	static String addAddress(String[] str) throws SQLException {
		//<veri>&yoona?lim&SM Ent'l?Yeongdong-daero 513?Gangnam-gu?Seoul?KR?00000
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		String[] name = str[1].split("\\?");
		String[] info = str[2].split("\\?");
		
		Connection c = SQLOperation.getConnect(uid);
		String addStatus = SQLControl.SQLOperation.readDatabase(c, "select line2 from address where line1='" + info[1] + "'");
		if (addStatus != null) {
			c.close();
			return "0x1E06";
		}
		
		String value = "('" + name[0] + "','" + name[1] + "','" + info[0] + "','" + info[1] + "','" + info[2] + "','" + info[3] + "','" + info[4] + "','" + info[5] + "')";
		String sql = "INSERT INTO address(fn, ln, company, line1, line2, city, state, zip) VALUES" + value + ";";
		System.out.println(SQLOperation.updateData(c, sql));
		c.close();
		return "0x01";
	}
	
	static String loadAddress (String[] str) throws SQLException {
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		Connection c = SQLOperation.getConnect(uid);
		String sql = "SELECT * FROM address";
		ResultSet rs = SQLOperation.readDatabaseRS(c, sql);
		String res = generateResWithRS(rs, 8);
		c.close();
		if (res.equals("")) return "0x1E05";
		else return res;
	}
	
	static String deleteAddress(String[] str) throws SQLException {
		//dlc&sid&line1#
		String uid = sessionVerify(str[0]);
		if (uid.length() == 6 && uid.charAt(0) == '0' && uid.charAt(1) == 'x') return uid;
		Connection c = SQLControl.SQLOperation.getConnect(uid);
		String cardStatus = SQLControl.SQLOperation.readDatabase(c, "select zip from address where line1='" + str[1] + "'");
		System.out.println(str[1]);
		if (cardStatus == null) {
			c.close();
			return "0x1E07";
		}
		String sql = "delete from address where line1='" + str[1] + "';";
		String res = SQLControl.SQLOperation.updateData(c, sql);
		c.close();
		if (res != "UPS") return res;
		else return "0x01";
	}
	
	private static String generateResWithRS(ResultSet rs, int len) throws SQLException {
		String res = "";
		while (rs.next()) {
			if (res != "") res += "&";
			for (int i = 1; i <= len; i++) {
				res  += rs.getString(i);
				if (i < len) res += "?";
			}
		}
		return res;
	}
	
	private static String validateCardInfo(String[] name, String[] card, String[] exp) {
		return "0x01";
	}
	
	static String sessionVerify (String sessionID) throws SQLException {
		if (sessionID.equalsIgnoreCase("null")) return "0x1D03";
		String[] veri = sessionID.split("\\?");
		Connection c = SQLControl.SQLOperation.getConnect("accessLog");
		String sql = "select token from authLog where uid='" + veri[0] + "'";
		String res = SQLOperation.readDatabase(c, sql);
		if (!veri[1].equals(res)) {
			c.close();
			return "0x1D01";
		}
		sql = "select authTime from authLog where uid='" + veri[0] + "'";
		Long l = Long.parseLong(SQLOperation.readDatabase(c, sql));
		if (System.currentTimeMillis() - l > 0x927C0 || System.currentTimeMillis() < l) return "0x1D02";
		sql = "update authLog set authTime='" + System.currentTimeMillis() + "' where uid='" + veri[0] + "';" ;
		SQLControl.SQLOperation.updateData(c, sql);
		c.close();
		return veri[0];
	}
	
	static String illegalInput() {
		writeLog("Illegal Input.");
		return null;
	}
	
	public static void writeLog (String str) {
		System.out.println(str);
	}
	
	
}

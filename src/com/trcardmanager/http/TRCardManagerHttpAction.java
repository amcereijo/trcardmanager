package com.trcardmanager.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerLoginException;
import com.trcardmanager.exception.TRCardManagerUpdateCardException;
import com.trcardmanager.string.TRCardManagerStringHelper;

public class TRCardManagerHttpAction {

	private static final int TIMEOUT = 5000;
	private static final String LOGIN_RESPONSE_OK = "2";
	
	private static final String COOKIE_NAME = "CWNWSESSION";
    private static final String URL_BASE = "https://ticketrestaurant.edenred.es/TRC/";
    private static final String URL_LOGIN = "checkUserLogin.php";
    private static final String URL_BALANCE = "consulta_tarjeta.html";
    private static final String URL_MY_ACCOUNT = "mi_cuenta.html";
    private static final String TYPE_PARAMETER = "trc";
    private static final String URL_UPDATE_CARD = "sendMyAccountCard.php";
    private static final String UPDATE_CARD_ID_PARAMETER = "updCard";
    private static final String UPDATE_CARD_PROFILE_PARAMETER = "TRCU";
    private static final String UPDATE_CARD_RESPONSE_OK = "OK";
    private static final String URL_PREPARE_UPDATE_CARD = "mi_cuenta.html";
    private static final String PREPARE_UPDATE_CARD_START_SEARCH = "<input type=\"hidden\" name=\"id\" value=\"";
    private static final String PREPARE_UPDATE_CARD_END_SEARCH = "\">";
    
    
       
    public void getCookieLogin(UserDao user) throws TRCardManagerLoginException, 
    		ClientProtocolException, IOException{
		Map<String, String> postMap = new HashMap<String, String>();
			postMap.put("user", user.getEmail());
			postMap.put("passwd", user.getPassword());
			postMap.put("type", TYPE_PARAMETER);
		
		Response response = Jsoup.connect(URL_BASE+URL_LOGIN).data(postMap).execute();
		if(response != null){
			String responseString = response.body();
	    	if(LOGIN_RESPONSE_OK.equals(responseString)){
	    		 String cookieValue = response.cookie(COOKIE_NAME);
	    		 user.setCookieValue(cookieValue);
	    	}else{
	    		throw new TRCardManagerLoginException();
	    	}
		}else{
			throw new TRCardManagerLoginException();
		}
    }
    
    public void getActualCard(UserDao user) throws ClientProtocolException,
    		IOException, TRCardManagerDataException{
        Document htmlDocument = getHttpPage(URL_MY_ACCOUNT,user.getCookieValue());
        Element elNumCard = htmlDocument.getElementById("num_card");
        String numCardValue = elNumCard.attr("value");
        Log.i("", "consulta tarjeta get: " + numCardValue);
        CardDao card = new CardDao(numCardValue);
        user.setActualCard(card);
    }
    
    public void getActualCardBalance(UserDao user) throws ClientProtocolException,
		IOException, TRCardManagerDataException{  
    	
    	Document htmlDocument = getHttpPage(URL_BALANCE,user.getCookieValue());
        Elements elementsResult = htmlDocument.getElementsByClass("result");
        Element elBalance = elementsResult.first();
        String balance = elBalance.html();
        Log.i("", "consulta saldo get: " + balance);
        int substringposition = balance.indexOf(" ");
        user.getActualCard().setBalance(balance.substring(0,
        		substringposition>0?substringposition:balance.length()-1));
    }
    
    
    private Document getHttpPage(String httpPage, String cookieValue) throws IOException{
    	return Jsoup.connect(URL_BASE+httpPage).cookie(COOKIE_NAME,cookieValue).timeout(TIMEOUT).get();
    }
    
    public String getPrepareUpdateCard(UserDao user) throws ClientProtocolException,
		IOException, TRCardManagerDataException{
		
    	String id = "";
    	DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(URL_BASE+URL_PREPARE_UPDATE_CARD);
		get.addHeader("Cookie", COOKIE_NAME+"="+user.getCookieValue());
		
		HttpResponse response = httpClient.execute(get);
		HttpEntity entity = response.getEntity();
		
		Log.i("","prepare update card get: " + response.getStatusLine());
		if (entity != null) {
			String html = EntityUtils.toString(entity,HTTP.UTF_8);
			id = new TRCardManagerStringHelper(html)
				.getStringBetwen(PREPARE_UPDATE_CARD_START_SEARCH, PREPARE_UPDATE_CARD_END_SEARCH);
		    System.out.println("id: "+id);
		}
		return id;
	}
    
    public void activateCard(UserDao user, CardDao card) throws IOException, TRCardManagerUpdateCardException, TRCardManagerDataException{
    /*
		var f = document.getElementById('updCard');
		var _id = f.id.value;
		var _profile = f.profile.value;
        var _num_card = f.num_card.value; 			
	    
        $.ajax({
           url: "sendMyAccountCard.php",
           type: "POST",
           dataType: "text",
           data: {swlang: swlang, id: "updCard", profile: "TRCU", num_card: _num_card},	           		
           error: function(req, err, obj) {
    	 */
    	//TRC4e1ad6e9cc5d0
    	
    	//Get 
    	//<input type="hidden" name="id" value="TRC4e1ad6e9cc5d0">
    	String id = getPrepareUpdateCard(user);
    	
    	HttpPost post = new HttpPost(URL_BASE+URL_UPDATE_CARD);
    	List<NameValuePair> params = createPostListParameters(new String[][]{
    			{"swlang",Locale.getDefault().getCountry()},
    			{"id",id},
    			{"profile",UPDATE_CARD_PROFILE_PARAMETER},
    			{"num_card",card.getCardNumber()}
    	});
    	
		post.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
		//post.addHeader("Cookie", COOKIE_NAME+"="+user.getCookieValue());
		DefaultHttpClient httpClient = new DefaultHttpClient();
		BasicClientCookie cookie = new BasicClientCookie(COOKIE_NAME, user.getCookieValue());
		cookie.setDomain("ticketrestaurant.edenred.es");
		cookie.setPath("/");
		httpClient.getCookieStore().addCookie(cookie);
		HttpResponse response = httpClient.execute(post);
		HttpEntity entity = response.getEntity();

		System.out.println("update card post response: " + response.getStatusLine());
		if (entity != null){
			String html = EntityUtils.toString(entity,HTTP.UTF_8); 
			if(!UPDATE_CARD_RESPONSE_OK.equals(html)){
				throw new TRCardManagerUpdateCardException(); 
			}
		}else{
			throw new TRCardManagerUpdateCardException();
		}
    }
 
    
    private String processLoginResponse(DefaultHttpClient httpClient, HttpEntity entity) throws TRCardManagerLoginException,IOException{
    	String responseString = EntityUtils.toString(entity,HTTP.UTF_8);
    	if(LOGIN_RESPONSE_OK.equals(responseString)){
    		 return findCookieLogin(httpClient);
    	}
    	throw new TRCardManagerLoginException();
    }
    
    private String findCookieLogin(DefaultHttpClient httpClient)throws TRCardManagerLoginException{
    	System.out.println("Post logon cookies:");
		List<Cookie> cookies = httpClient.getCookieStore().getCookies();
		if (!cookies.isEmpty()) {
			for(Cookie cookie : cookies){
				if(COOKIE_NAME.equals(cookie.getName())){
					String cookieValue = cookie.getValue();
					System.out.println("VALUE get - " + cookieValue);
					return cookieValue;
				}
			}
		}
		throw new TRCardManagerLoginException();
    }
    
    
    
    private List<NameValuePair> createPostListParameters(String[][] nameVaules){
    	List<NameValuePair> params = new ArrayList<NameValuePair>();
    	for(String[] nameValue : nameVaules){
    		params.add(new BasicNameValuePair(nameValue[0], nameValue[1]));
    	}
		return params;
    }
}

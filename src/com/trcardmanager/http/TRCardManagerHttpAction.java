package com.trcardmanager.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerLoginException;
import com.trcardmanager.string.TRCardManagerStringHelper;

public class TRCardManagerHttpAction {

	private static final String COOKIE_NAME = "CWNWSESSION";
    private static final String URL_BASE = "https://ticketrestaurant.edenred.es/TRC/";
    private static final String URL_LOGIN = "checkUserLogin.php";
    private static final String URL_BALANCE = "consulta_tarjeta.html";
    private static final String URL_MY_ACCOUNT = "mi_cuenta.html";
    private static final String TYPE_PARAMETER = "trc";
    
    private static final String CARD_VALUE_START_SEARCH = "name=\"num_card\" value=\"";
    private static final String CARD_VALUE_END_SEARCH = "\"";
    
    private static final String BALANCE_VALUE_START_SEARCH = "<p class=\"result\">";
    private static final String BALANCE_VALUE_END_SEARCH = " ";
    
    private static final String LOGIN_RESPONSE_OK = "2";
    
       
    public void getCookieLogin(UserDao user) throws TRCardManagerLoginException, ClientProtocolException, IOException{
    	HttpPost post = new HttpPost(URL_BASE+URL_LOGIN);
    	List<NameValuePair> params = createLoginListParameters(user);
		
		post.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(post);
		HttpEntity entity = response.getEntity();

		System.out.println("Login form get: " + response.getStatusLine());
		if (entity != null) {
			String cookieValue = processLoginResponse(httpClient,entity);
			user.setCookieValue(cookieValue);
		}else{
			throw new TRCardManagerLoginException();
		}
    }
    
    public void getActualCard(UserDao user) throws ClientProtocolException,
    		IOException, TRCardManagerDataException{
    	DefaultHttpClient httpClient = new DefaultHttpClient();
    	HttpGet get = new HttpGet(URL_BASE+URL_MY_ACCOUNT);
    	get.addHeader("Cookie", COOKIE_NAME+"="+user.getCookieValue());
    	
    	HttpResponse response = httpClient.execute(get);
    	HttpEntity entity = response.getEntity();
    	
    	System.out.println("consulta tarjeta get: " + response.getStatusLine());
        if (entity != null) {
        	String html = EntityUtils.toString(entity,HTTP.UTF_8);
        	String cardNumber = new TRCardManagerStringHelper(html)
        		.getStringBetwen(CARD_VALUE_START_SEARCH, CARD_VALUE_END_SEARCH);
            System.out.println("Card Number:: "+cardNumber);
            
            CardDao card = new CardDao(cardNumber);
            user.setActualCard(card);
        }
    }
    
    public void getActualCardBalance(UserDao user) throws ClientProtocolException,
		IOException, TRCardManagerDataException{
    	DefaultHttpClient httpClient = new DefaultHttpClient();
    	HttpGet get = new HttpGet(URL_BASE+URL_BALANCE);
    	get.addHeader("Cookie", COOKIE_NAME+"="+user.getCookieValue());
    	get.addHeader("Accept-Charset","utf-8");
    
    	HttpResponse response = httpClient.execute(get);
    	HttpEntity entity = response.getEntity();
    	
    	System.out.println("consulta tarjeta get: " + response.getStatusLine());
        if (entity != null) {
        	String html = EntityUtils.toString(entity,HTTP.UTF_8);
        	String cardBalance = new TRCardManagerStringHelper(html)
        		.getStringBetwen(BALANCE_VALUE_START_SEARCH, BALANCE_VALUE_END_SEARCH);
            System.out.println("Card balance: "+cardBalance);
            
            user.getActualCard().setBalance(cardBalance);
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
    
    
    private List<NameValuePair> createLoginListParameters(UserDao user){
    	List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("user", user.getEmail()));
		params.add(new BasicNameValuePair("passwd", user.getPassword()));
		params.add(new BasicNameValuePair("type", TYPE_PARAMETER));
		return params;
    }
}

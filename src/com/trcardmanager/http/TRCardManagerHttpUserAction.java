package com.trcardmanager.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.util.Log;

import com.trcardmanager.dao.UserDao;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerLoginException;
import com.trcardmanager.exception.TRCardManagerRecoverPasswordException;
import com.trcardmanager.exception.TRCardManagerSessionException;
import com.trcardmanager.exception.TRCardManagerUpdatePasswordException;

/**
 * http operations for user account
 * @author angelcereijo
 *
 */
public class TRCardManagerHttpUserAction extends TRCardManagerHttpAction{
	
	private static final String TAG = TRCardManagerHttpUserAction.class.getName();
	
	private static final String LOGIN_FIELD_TYPE = "type";
	private static final String LOGIN_FIELD_PASSW = "passwd";
	private static final String LOGIN_FIELD_USER = "user";
	private static final String LOGIN_RESPONSE_OK = "2";
	private static final String URL_LOGIN = "checkUserLogin.php";
	private static final String TYPE_PARAMETER = "trc";
	private static final String URL_UPDATE_PASSWORD = "sendMyAccountPwd.php";
	private static final String URL_RECOVER_PASSWORD = "recoverPasswdTRC.php";
	private static final String RECOVER_USER_PARAMETER = "user";
	private static final String RECOVER_RESPONSE_OK = "2";
	private static final String RECOVER_RESPONSE_ERROR = "1";

	
	 /**
     * 
     * @param user
     * @throws TRCardManagerLoginException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void getCookieLogin(UserDao user) throws TRCardManagerLoginException, 
    		ClientProtocolException, IOException{
    	Map<String, String> postMap = new HashMap<String, String>();
			postMap.put(LOGIN_FIELD_USER, user.getEmail());
			postMap.put(LOGIN_FIELD_PASSW, user.getPassword());
			postMap.put(LOGIN_FIELD_TYPE, TYPE_PARAMETER);
			postMap.put(LANG_PARAMETER, LANG_PARAMETER_VALUE);
		
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
    
    
    /**
     * 
     * @param user
     * @param newPassword
     * @throws ClientProtocolException
     * @throws IOException
     * @throws TRCardManagerDataException
     * @throws TRCardManagerSessionException
     * @throws TRCardManagerUpdatePasswordException
     */
    public void changePassword(UserDao user,String newPassword) throws ClientProtocolException, IOException, 
    		TRCardManagerDataException, TRCardManagerSessionException, TRCardManagerUpdatePasswordException{
    	/* url: "sendMyAccountPwd.php",
           type: "POST",
           dataType: "text",
           data: {swlang: swlang, id: _id, pwd: _pwd},	           		
           error: function(req, err, obj) {
           	alert('error');
            alert(err);
            
            //$ret[errorcode] = "01";  // Usuario no existe o dado de baja .
   		//$ret[errorcode] = "02";  // Tarjeta no existe.
   		//$ret[errorcode] = "03";  // Tarjeta está asociada a otro usuario
   		//$ret[errorcode] = "04";  // Tarjeta con perfil diferente al del usuario
         */
    	String id = getPrepareUpdatePasswprd(user);
    	Map<String, String> postMap = new HashMap<String, String>();
    	postMap.put(LANG_PARAMETER, LANG_PARAMETER_VALUE);
		postMap.put("id",id);
		postMap.put("pwd",newPassword);
		Response response = Jsoup.connect(URL_BASE+URL_UPDATE_PASSWORD).cookie(COOKIE_NAME,user.getCookieValue())
			.timeout(TIMEOUT).data(postMap).execute();
		if(response != null){
			String responseString = response.body();
			if(!RESPONSE_OK.equals(responseString)){
				throw new TRCardManagerUpdatePasswordException(responseString); 
			}
		}else{
			throw new TRCardManagerUpdatePasswordException();
		}
    	
    }
    
    /**
     * 
     * @param user
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws TRCardManagerDataException
     * @throws TRCardManagerSessionException
     */
    public String getPrepareUpdatePasswprd(UserDao user) throws ClientProtocolException,
			IOException, TRCardManagerDataException, TRCardManagerSessionException{
		Document document = getHttpPage(URL_MY_ACCOUNT, user.getCookieValue());
		Element form = document.getElementById("updPwd");
		Element inputHiddenId = form.getElementsByAttributeValue("name", "id").first();
		String id = inputHiddenId.attr("value");
		return id;
	}
    
    
    
    /**
     * Call action to recover password
     * @param email
     */
    public void callRecoverPassword(String email)throws TRCardManagerRecoverPasswordException{
    	Response response;
    	try{
	    	Map<String, String> postMap = new HashMap<String, String>();
	    	postMap.put(LANG_PARAMETER, LANG_PARAMETER_VALUE);
			postMap.put(RECOVER_USER_PARAMETER,email);
			response = Jsoup.connect(URL_BASE+URL_RECOVER_PASSWORD).data(postMap).execute();
    	}catch(IOException e){
    		Log.e(TAG, e.getMessage(),e);
    		throw new TRCardManagerRecoverPasswordException(TRCardManagerRecoverPasswordException.CONECTION_ERROR);
    	}catch(Exception e){
    		Log.e(TAG, e.getMessage(),e);
    		throw new TRCardManagerRecoverPasswordException(TRCardManagerRecoverPasswordException.UNKNOWN_ERROR);
    	}
		if(response != null){
			String responseString = response.body();
			if(RECOVER_RESPONSE_ERROR.equals(responseString)){
				throw new TRCardManagerRecoverPasswordException(TRCardManagerRecoverPasswordException.BAD_USER);
			}else if(!RECOVER_RESPONSE_OK.equals(responseString)){
				throw new TRCardManagerRecoverPasswordException(TRCardManagerRecoverPasswordException.UNKNOWN_ERROR);
			}
	    }
    }
	
}

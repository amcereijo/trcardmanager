package com.trcardmanager.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.DirectionDao;
import com.trcardmanager.dao.LocationDao;
import com.trcardmanager.dao.MovementDao;
import com.trcardmanager.dao.MovementSeparatorDao;
import com.trcardmanager.dao.MovementsDao;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerLoginException;
import com.trcardmanager.exception.TRCardManagerSessionException;
import com.trcardmanager.exception.TRCardManagerUpdateCardException;
import com.trcardmanager.exception.TRCardManagerUpdatePasswordException;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerHttpAction {
	
	private static final int HISTORICAL_LIST_MOVEMENTS_START_POSITION = 2;

	private static final String TAG = TRCardManagerHttpAction.class.getName();
	
	private static final String LOGIN_FIELD_TYPE = "type";
	private static final String LOGIN_FIELD_PASSW = "passwd";
	private static final String LOGIN_FIELD_USER = "user";

	private static final int TIMEOUT = 15000;
	private static final String LOGIN_RESPONSE_OK = "2";
	
	private static final String COOKIE_NAME = "CWNWSESSION";
    private static final String URL_BASE = "https://ticketrestaurant.edenred.es/TRC/";
    private static final String URL_LOGIN = "checkUserLogin.php";
    private static final String URL_BALANCE = "consulta_tarjeta.html";
    private static final String URL_MY_ACCOUNT = "mi_cuenta.html";
    private static final String TYPE_PARAMETER = "trc";
    private static final String URL_HISTORICAL = "consulta_movimientos.html";
    private static final String HISTORICAL_SEARCH_DATE_FROM = "01/01/2001";
    private static final String CLASS_TO_SEARH_ACTUAL_BALANCE = "result";
    private static final String ID_TO_SEARCH_CARD_NUMBER = "num_card";
    private static final String ATTRIBUTE_TO_GET_PROPERTY_VALUE = "value";
    private static final String URL_UPDATE_CARD = "sendMyAccountCard.php";
    private static final String UPDATE_CARD_PROFILE_PARAMETER = "TRCU";
    private static final String UPDATE_CARD_RESPONSE_OK = "OK";
    private static final String URL_PREPARE_UPDATE_CARD_PASSWORD = "mi_cuenta.html";
    private static final String URL_UPDATE_PASSWORD = "sendMyAccountPwd.php";
    //private static final String URL_SEARCH_RESTAURANTS = "http://www.edenred.es/buscador-afiliados/imprimir_resultados";
    private static final String URL_SEARCH_RESTAURANTS = "http://www.edenred.es/affiliates_search/search";
    private static final String URL_INFO_RESTAURANT = "http://www.edenred.es/affiliates_search/show_affiliate/%s";
    private static final String SEARCH_RESTAURANTS_PRODUCT = "ticket-restaurant";
    private static final String SEARCH_RESTAURANTS_FORMAT = "tarjeta";
    
      
    
    public void getCookieLogin(UserDao user) throws TRCardManagerLoginException, 
    		ClientProtocolException, IOException{
		Map<String, String> postMap = new HashMap<String, String>();
			postMap.put(LOGIN_FIELD_USER, user.getEmail());
			postMap.put(LOGIN_FIELD_PASSW, user.getPassword());
			postMap.put(LOGIN_FIELD_TYPE, TYPE_PARAMETER);
		
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
    		IOException, TRCardManagerDataException, TRCardManagerSessionException{
        Document htmlDocument = getHttpPage(URL_MY_ACCOUNT,user.getCookieValue());
        Element elNumCard = htmlDocument.getElementById(ID_TO_SEARCH_CARD_NUMBER);
        String numCardValue = elNumCard.attr(ATTRIBUTE_TO_GET_PROPERTY_VALUE);
        CardDao card = new CardDao(numCardValue);
        user.setActualCard(card);
    }
    
    /**
     * Get lasta movements 
     * @param user
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     * @throws TRCardManagerDataException
     * @throws TRCardManagerSessionException 
     */
    public List<MovementDao> updateLastMovementsAndBalance(UserDao user) throws IOException,TRCardManagerDataException, TRCardManagerSessionException {  
    	
    	try{
	    	Document htmlDocument = getHttpPage(URL_BALANCE,user.getCookieValue());
	        //last movements
	    	List<MovementDao> newMovements = getMovementsList(htmlDocument);
			
	    	//Actual movement list
			MovementsDao movements = user.getActualCard().getMovementsData();
			List<MovementDao> actualList = movements.getMovements();
			
			if(actualList!=null && actualList.size()>0 && newMovements!=null && newMovements.size()>0){
				onlyNewMovements(newMovements, actualList);
				//Add new movements to saved movements
				user.getActualCard().getMovementsData().getMovements().addAll(0, newMovements);
			}
			
			if(newMovements.size()>0){
				String actualBalance = getActualBalance(htmlDocument);
		        user.getActualCard().setBalance(actualBalance);
			}
			
			return newMovements;
		}catch(TRCardManagerSessionException se){
			Log.e(TAG,se.getMessage(),se);
			throw se;
		}catch(Exception e){
			Log.e(TAG,e.getMessage(),e);
			throw new TRCardManagerDataException(e);
		}
	}

    
	private void onlyNewMovements(List<MovementDao> newMovements,
			List<MovementDao> actualList) {
		//Actual last movement
		MovementDao firstMovement = actualList.get(0);
		
		int newMovementsSize = newMovements.size();
		boolean movementFoundedInList = false;
		
		for(int i=-1;i<newMovementsSize;){
			if(movementFoundedInList){
				//delete movements currently in list
				newMovements.remove(i);
				--newMovementsSize;
			}else{
				MovementDao oneNewMovement = newMovements.get(++i);
				movementFoundedInList = (oneNewMovement.getOperationId().equals(firstMovement.getOperationId()));
			}
		}
	}
    
    
    public void getActualCardBalanceAndMovements(UserDao user) throws ClientProtocolException,
			IOException, TRCardManagerDataException, TRCardManagerSessionException{  
    	Document htmlDocument = getHttpPage(URL_BALANCE,user.getCookieValue());
        
    	String actualBalance = getActualBalance(htmlDocument);
        user.getActualCard().setBalance(actualBalance);
        
        List<MovementDao> movementList = getMovementsList(htmlDocument);
        MovementsDao movements = new MovementsDao();
        movements.setMovements(movementList);
        	
        getPaginationMovements(movements,htmlDocument);
        
        user.getActualCard().setMovementsData(movements);
    }
    
    
    private String getActualBalance(Document htmlDocument){
    	Elements elementsResult = htmlDocument.getElementsByClass(CLASS_TO_SEARH_ACTUAL_BALANCE);
        Element elBalance = elementsResult.first();
        String balance = elBalance.html();
        int substringposition = balance.indexOf(" ");
        return balance.substring(0,
        		substringposition>0?substringposition:balance.length()-1);
    }
    
    
    private void getPaginationMovements(MovementsDao movementsDao,Document htmlDocument) throws ClientProtocolException,
			IOException, TRCardManagerDataException{
    	
    	//SEARCH dates fromdate todate
    	String fromDate = htmlDocument.getElementById("fromdate").attr("value");
    	String toDate = htmlDocument.getElementById("todate").attr("value");
    	movementsDao.setDateStart(fromDate);
    	movementsDao.setDateEnd(toDate);
    	
    	//SEARH NUMBER OF PAGES class="tab_menu" --> class="derecha" --> <a /> hasta title="Imprimir"
    	Element tabMenu = htmlDocument.getElementsByClass("tab_menu").get(0);
    	Element pagesNumbers = tabMenu.getElementsByClass("derecha").get(0);
    	Elements pagesLinks = pagesNumbers.getElementsByTag("a");
    	
    	//FILL LIST OF MOVEMENTS FROM PAGES
    	List<String> listUrlLinks = new ArrayList<String>();
    	for(Element pageLink : pagesLinks){
    		String title = pageLink.attr("title");
    		//avoid print link
    		if(title==null || "".equals(title.trim())){
    			//call href value
    			String href = pageLink.attr("href");
    			listUrlLinks.add(href);
    		}
    	}
    	movementsDao.setPaginationLinks(listUrlLinks);
    	movementsDao.setActualPage(0);
    	movementsDao.setNumberOfPages(listUrlLinks.size());
    }
       
    
    public List<MovementDao> getNextMovements(UserDao user) throws IOException, TRCardManagerSessionException,
    		TRCardManagerDataException{
    	List<MovementDao> pageMovements = new ArrayList<MovementDao>();
    	MovementsDao movementsData = user.getActualCard().getMovementsData();    	
    	int actualPage = movementsData.getActualPage()+1;
    	if(actualPage<movementsData.getNumberOfPages()){
    		List<String> paginationUrls = movementsData.getPaginationLinks();
    		String url = paginationUrls.get(actualPage);
    		Document htmlDocument = getHttpPage(url,user.getCookieValue());
        	pageMovements = getMovementsList(htmlDocument);
        	//movementsData.getMovements().addAll(pageMovements);
        	movementsData.setActualPage(actualPage);
    	}else{
    		movementsData.setActualPage(movementsData.getNumberOfPages());
    		pageMovements = getNextHistoricalMovements(user);
    	}
    	
    	return pageMovements;
    	
    }
    
    private String getLastDate(List<MovementDao> movements){
    	String lastDate;
    	if(movements==null || movements.size()==0){
    		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    		lastDate = sdf.format(Calendar.getInstance().getTime());
    	}else{
    		lastDate = movements.get(movements.size()-1).getDate();
    	}
    	return lastDate;
    }
    
    private List<MovementDao> getNextHistoricalMovements(UserDao user) throws IOException, 
			TRCardManagerSessionException,TRCardManagerDataException{
		
		MovementsDao movementsDao = user.getActualCard().getMovementsData();
		int actualPage = movementsDao.getHistoricalActualPage();
		
		String lastDate = getLastDate(movementsDao.getMovements());
		
		StringBuilder strb = new StringBuilder().append("?pag=").append(actualPage).append("&fromdate=")
			.append(HISTORICAL_SEARCH_DATE_FROM).append("&todate=").append(lastDate).append("&consultamov=ALL");
		
		Document htmlDocument = getHttpPage(URL_HISTORICAL+strb,user.getCookieValue());
		
		Element dataTable = htmlDocument.body().getElementsByClass("tab_movs").first();
		//Look for pages?
		if(movementsDao.getHistoricalNumberOfPages()==-1){
			//look for number of pages
			movementsDao.setHistoricalNumberOfPages(lookForHistoricalTotalPages(dataTable));
		}
		
		List<MovementDao> historicalMovements = new ArrayList<MovementDao>();
		if(actualPage == 0){
			//add separator
			historicalMovements.add(new MovementSeparatorDao());
		}
		
		if(actualPage<movementsDao.getHistoricalNumberOfPages()){
			getPageHistoricalMovements(actualPage, dataTable,
					historicalMovements);
			movementsDao.setHistoricalActualPage(actualPage+1);
		}
		
		return historicalMovements; 
	}
	
	
	private void getPageHistoricalMovements(int actualPage, Element dataTable,
			List<MovementDao> historicalMovements) {
		Elements trListData = dataTable.getElementsByTag("tr");
		for(int i=HISTORICAL_LIST_MOVEMENTS_START_POSITION;i<trListData.size();i++){
			Element actualTr = trListData.get(i);
			MovementDao newMovement = getHistorialMovementData(actualPage, i, actualTr);
			historicalMovements.add(newMovement);
		}
	}
	
	
	private MovementDao getHistorialMovementData(int actualPage, int i,
			Element actualTr) {
		Elements tdListData = actualTr.getElementsByTag("td");
		String date = tdListData.get(0).html();
		String hour = getWhitOutHtmlWhiteSpaceTag(tdListData.get(1).html());
		String operationType = tdListData.get(2).html();
		String amount = getWhitOutHtmlWhiteSpaceTag(tdListData.get(3).html());
		String trade = tdListData.get(4).html();
		//generate an id
		String operationId = new StringBuilder().append(actualPage)
			.append(i-HISTORICAL_LIST_MOVEMENTS_START_POSITION).toString();
		MovementDao newMovement = new MovementDao();
			newMovement.setDate(date);
			newMovement.setHour(hour);
			newMovement.setOperationType(operationType);
			newMovement.setAmount(amount);
			newMovement.setTrade(trade);
			newMovement.setOperationId(operationId);
		return newMovement;
	}
	
	
	private int lookForHistoricalTotalPages(Element dataTable) throws TRCardManagerDataException {
		Element tdTabMenu = dataTable.getElementsByClass("tab_menu").first();
		Element spanData = tdTabMenu.getElementsByClass("izquierda").first();
		String spanContent = spanData.html();
		int lastWhiteSpacePosition = spanContent.lastIndexOf(" ");
		int lastTwoPointsPosition = spanContent.lastIndexOf(":");
		String totalPages = spanContent.substring(lastWhiteSpacePosition+1,lastTwoPointsPosition);
		try{
			return Integer.parseInt(totalPages);
		}catch(NumberFormatException nue){
			throw new TRCardManagerDataException("Error getting historical total page number:"+nue.getMessage());
		}
	}
    
	
    private Document getHttpPage(String httpPage, String cookieValue) throws IOException,TRCardManagerSessionException{
    	Connection connection = Jsoup.connect(URL_BASE+httpPage).cookie(COOKIE_NAME,cookieValue).timeout(TIMEOUT);
    	Response response = connection.execute();
    	String url = response.url().toString();
    	boolean logedIn = url.contains(httpPage);
    	if(!logedIn){
    		throw new TRCardManagerSessionException();
    	}
    	Document d = Jsoup.parse(new String(response.bodyAsBytes(),"ISO-8859-1"));
    	return d;
    }
    
   
    private List<MovementDao> getMovementsList(Document htmlDocument) throws IOException{
    	List<MovementDao> movementsList = new ArrayList<MovementDao>();
    
    	Element table =  htmlDocument.getElementsByClass("tab_movs").first();
    	Elements tableRows = table.getElementsByTag("tr");
    	int firstDataRow = 2;
    	for(int cont=firstDataRow;cont<tableRows.size();cont++){
    		MovementDao movement = getMovement(tableRows.get(cont));
    		movementsList.add(movement);
    	}
    	return movementsList;
    }
    
    private MovementDao getMovement(Element dataRow){
    	MovementDao movement = new MovementDao();
    	Elements dataCells = dataRow.getElementsByTag("td");
    	String date = dataCells.get(0).html();
    	String hour = dataCells.get(1).html();
    	String operationId = dataCells.get(2).html();
    	String operationType = dataCells.get(3).html();
    	
    	String amount = dataCells.get(4).html();
    	String trade = dataCells.get(5).html();
    	String state = dataCells.get(6).html();
    	
    	movement.setOperationId(operationId);
    	movement.setDate(getWhitOutHtmlWhiteSpaceTag(date));
    	movement.setHour(getWhitOutHtmlWhiteSpaceTag(hour));
    	movement.setOperationType(operationType);
    	movement.setAmount(getWhitOutHtmlWhiteSpaceTag(amount));
    	movement.setTrade(trade);
    	movement.setState(state);
    	
    	return movement;
    }
    
    
    
    /**
     * 
     * @param user
     * @param card
     * @throws IOException
     * @throws TRCardManagerUpdateCardException
     * @throws TRCardManagerDataException
     * @throws TRCardManagerSessionException
     */
    public void activateCard(UserDao user, CardDao card) throws IOException, TRCardManagerUpdateCardException,
    	TRCardManagerDataException, TRCardManagerSessionException{
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
		    	//$ret[errorcode] = "01";  // Usuario no existe o dado de baja .
		   		//$ret[errorcode] = "02";  // Tarjeta no existe.
		   		//$ret[errorcode] = "03";  // Tarjeta está asociada a otro usuario
		   		//$ret[errorcode] = "04";  // Tarjeta con perfil diferente al del usuario
        	//Get 
        	//<input type="hidden" name="id" value="TRC4e1ad6e9cc5d0">
        	String id = getPrepareUpdateCard(user);
        	
        	Map<String, String> postMap = new HashMap<String, String>();
				postMap.put("swlang",Locale.getDefault().getCountry());
				postMap.put("id",id);
				postMap.put("profile",UPDATE_CARD_PROFILE_PARAMETER);
				postMap.put("num_card",card.getCardNumber());

			Response response = Jsoup.connect(URL_BASE+URL_UPDATE_CARD).cookie(COOKIE_NAME,user.getCookieValue()).timeout(TIMEOUT).data(postMap).execute();
			if(response != null){
				String responseString = response.body();
				if(!UPDATE_CARD_RESPONSE_OK.equals(responseString)){
    				throw new TRCardManagerUpdateCardException(responseString); 
    			}
			}else{
				throw new TRCardManagerUpdateCardException();
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
		postMap.put("swlang",Locale.getDefault().getCountry());
		postMap.put("id",id);
		postMap.put("pwd",newPassword);
		Response response = Jsoup.connect(URL_BASE+URL_UPDATE_PASSWORD).cookie(COOKIE_NAME,user.getCookieValue())
			.timeout(TIMEOUT).data(postMap).execute();
		if(response != null){
			String responseString = response.body();
			if(!UPDATE_CARD_RESPONSE_OK.equals(responseString)){
				throw new TRCardManagerUpdatePasswordException(responseString); 
			}
		}else{
			throw new TRCardManagerUpdatePasswordException();
		}
    	
    }
    
    public String getPrepareUpdateCard(UserDao user) throws ClientProtocolException,
			IOException, TRCardManagerDataException, TRCardManagerSessionException{
    	Document document = getHttpPage(URL_PREPARE_UPDATE_CARD_PASSWORD, user.getCookieValue());
		Element form = document.getElementById("updCard");
		Element inputHiddenId = form.getElementsByAttributeValue("name", "id").first();
		String id = inputHiddenId.attr("value");
		return id;
	}
    
    
    public String getPrepareUpdatePasswprd(UserDao user) throws ClientProtocolException,
			IOException, TRCardManagerDataException, TRCardManagerSessionException{
		Document document = getHttpPage(URL_PREPARE_UPDATE_CARD_PASSWORD, user.getCookieValue());
		Element form = document.getElementById("updPwd");
		Element inputHiddenId = form.getElementsByAttributeValue("name", "id").first();
		String id = inputHiddenId.attr("value");
		return id;
	}
    
    
    
    /**
     * 
     * @param restaurantDao
     * @throws IOException
     */
    public void completeResaturantInfo(RestaurantDao restaurantDao) throws IOException{
    	Map<String, String> postMap = new LinkedHashMap<String, String>();
    	postMap.put("locale", Locale.getDefault().getCountry().toLowerCase());
    	String urlGetInfo = String.format(URL_INFO_RESTAURANT,restaurantDao.getRestaurantLink());
    	Response response = Jsoup.connect(urlGetInfo).header("X-Requested-With","XMLHttpRequest")
			.timeout(TIMEOUT).data(postMap).execute();
		if(response != null){
			String htmlParsedResponse = getParsedResponse(response);
			Document d = Jsoup.parse(new String(htmlParsedResponse.getBytes(),"UTF-8"));
			Element restaurantElement = d.getElementsByClass("meta").first();
			/*
			 <div class="meta">
			    <div class="clearfix">
			      <h2>KRIS</h2>
			      <p class="type">Internacional</p>
			    </div>
			    <p>
			      <strong>CAMPEZO, 8, 28022</strong> /
			      MADRID, MADRID
			    </p>
			    <p class="tel">Tel: <strong>917244798</strong></p>
			    <p class="num-afiliado">Cod. Afiliado: 57167</p>
      			<div class="comment_options"><h1>Ayúdanos a mejorar</h1><ul><li><a href="/affiliates_search/affiliate_comment/4510131?comment_type=closed" class="comment">El restaurante ha cerrado / ya no existe</a></li><li><a href="/affiliates_search/affiliate_comment/4510131?comment_type=wrong_location" class="comment">La ubicación en el mapa no es correcta</a></li><li><a href="/affiliates_search/affiliate_comment/4510131?comment_type=wrong_address" class="comment">Los datos del restaurante han cambiado</a></li><li><a href="/affiliates_search/affiliate_comment/4510131?comment_type=tickets_restricted" class="comment">No acepta el pago con Ticket Restaurant</a></li><li><a href="/affiliates_search/affiliate_comment/4510131?comment_type=other" class="comment">Otros comentarios</a></li></ul></div>
      			<span class="comments" data-original-title="">Enviar comentario</span>
  			</div>
			*/
			Element nameAndFootTypeElement = restaurantElement.getElementsByClass("clearfix").first();
			Element foodTypeElement = nameAndFootTypeElement.getElementsByClass("type").first();
			restaurantDao.setFoodType(htmlDecoded(foodTypeElement.html()));
		}
    	
    }
    
    
    /**
     * 
     * @param addressSearch
     * @param affiliate
     * @return
     * @throws IOException 
     */
    public List<RestaurantDao> getRestaurants(RestaurantSearchDao restaurantSeachDao) throws IOException{
    	boolean doOneMoreTime;
    	List<RestaurantDao> restaurants = new ArrayList<RestaurantDao>();
    	/*
    	 * http://www.edenred.es/buscador-afiliados/imprimir_resultados?address=avenida+de+manoteras%2C+Madrid&center_lat=40.487302150000005&center_lng=-3.665504950000013&especifico_producto=true&formato=tarjeta&limit_lat=40.490162&limit_lng=-3.65761510000002&producto=ticket-restaurant
    	 * 

    	 * url:http://www.edenred.es/buscador-afiliados/imprimir_resultados
    	 * parameters: 
    	 * producto:ticket-restaurant
			formato:papel/tarjeta
			affiliate: //restaurant
			address: //country, cp, city, etc
			center_lng:-3.7129999999999654
			center_lat:40.2085
			limit_lng:5.098999999999957
			limit_lat:45.245
			locale:es
    	 * 
    	 */
    	do{
    		doOneMoreTime = false;
	    	Map<String, String> postMap = createParameterPostMapper(restaurantSeachDao.getAddressSearch(), 
	    			restaurantSeachDao.getAffiliate(), restaurantSeachDao.getDirectionDao(),
	    			restaurantSeachDao.getCurrentPage());
					
			Response response = Jsoup.connect(URL_SEARCH_RESTAURANTS).header("X-Requested-With","XMLHttpRequest")
				.timeout(TIMEOUT).data(postMap).execute();
			if(response != null){
				try{
					String htmlParsedResponse = getParsedResponse(response);
					String json = prepareJsonMessage(htmlParsedResponse);
					JSONObject jsonObject = new JSONObject(json);
					JSONArray results = jsonObject.getJSONArray("results");
					for(int i=0;i<results.length();i++){
						JSONObject result = results.getJSONObject(i);
						RestaurantDao restaurant = createResaturant(result); 
						if(restaurant!=null){
							restaurants.add(restaurant);
						}
					}
				}catch(JSONException e){
					Log.e(TAG, "Error reading Json restaurants information",e);
					e.printStackTrace();
				}
			}
			restaurantSeachDao.setCurrentPage(restaurantSeachDao.getCurrentPage()+1);
			restaurantSeachDao.getRestaurantList().addAll(restaurants);
			restaurantSeachDao.setSearchDone(Boolean.TRUE);
    	}while(doOneMoreTime);
    	return restaurants;
    }


    private RestaurantDao createResaturant(JSONObject result){
    	RestaurantDao restaurant = new RestaurantDao();
    	try{
			/*
			{"marker_anchor":[15,36],
			"picture":"/images/map_marker.png",
			"shadow_picture":"/images/map_marker_shadow.png",
			"width":29,
			"list_container":4491236,
			"shadow_width":51,
			"height":36,
			"shadow_height":36,
			"lng":-3.5944642,
			"shadow_anchor":[15,36],
			"description":"BLANCO CARRO\\<br/\\>CL ONCE 6\\<br/\\>MADRID\\<br/\\>MADRID\\<br/\\>917422249",
			"lat":40.4445333}
			 */
			double longitude = Double.parseDouble(result.getString("lng"));
			double latitude = Double.parseDouble(result.getString("lat"));
			LocationDao location = new LocationDao(longitude, latitude);
			restaurant.setLocation(location);
			getRestaurantBasicInfo(result.getString("description"),restaurant);
			restaurant.setCountry("ESPAÑA");
			restaurant.setRestaurantLink(result.getString("list_container"));
    	}catch(Exception e){
    		Log.e(TAG, "Error reading json element: "+result,e);
    		restaurant = null;
    	}
		return restaurant;
    }
    
    
	private String prepareJsonMessage(String htmlParsedResponse) {
		int posI = htmlParsedResponse.indexOf("Gmaps.map.replaceMarkers([") + "Gmaps.map.replaceMarkers([".length();
		int posF = htmlParsedResponse.indexOf("]);",posI);
		
		String json = htmlParsedResponse.substring(posI, posF);
		json = "{\"results\":["+json+"]}";
		return json;
	}

    
    private void getRestaurantBasicInfo(String description, RestaurantDao restaurant){
    	StringTokenizer strk = new StringTokenizer(htmlDecoded(description),"\\<br/\\>");
		try{
			String name = strk.nextToken();
			restaurant.setRetaurantName(name);
			String street = strk.nextToken();
			restaurant.setStreet(street);
			String city = strk.nextToken();
			restaurant.setLocality(city);
			String area = strk.nextToken();
			restaurant.setArea(area);
			String tlf = strk.nextToken();
			restaurant.setPhoneNumber(tlf);
		}catch(Exception e){
			Log.e(TAG, "Error reading description data from: "+
					description,e);
		}
    }
    

	private Map<String, String> createParameterPostMapper(String addressSearch,
			String affiliate, DirectionDao direction, int page) {
		Map<String, String> postMap = new LinkedHashMap<String, String>();
		LocationDao location = direction.getLocation();
		/*
		 * advanced:1
			producto:ticket-restaurant
			formato:papel
			address:
			affiliate:
			center_lng:-3.665579149999985
			center_lat:40.487302150000005
			limit_lng:-3.65761510000002
			limit_lat:40.490162
			page:(optional)
			locale:es
		 */
		
		postMap.put("producto", SEARCH_RESTAURANTS_PRODUCT);
		postMap.put("formato",SEARCH_RESTAURANTS_FORMAT);
		postMap.put("affiliate",affiliate);
		postMap.put("address",addressSearch);
		postMap.put("center_lng",String.valueOf(location.getLongitude()));
		postMap.put("center_lat",String.valueOf(location.getLatitude()));
		
		//TODO variable data
		//More or less one kilometer 
		double searchRadius = 0.009;//*20;
		
		postMap.put("limit_lng",String.valueOf(location.getLongitude()+searchRadius));
		postMap.put("limit_lat",String.valueOf(location.getLatitude()+searchRadius));
		if(page!=1){
			postMap.put("page",String.valueOf(page));
		}
		postMap.put("locale", Locale.getDefault().getCountry().toLowerCase());
		return postMap;
	}
    
	
    private String getParsedResponse(Response response)throws UnsupportedEncodingException{
    	String htmlParsedResponse = new String(response.bodyAsBytes(),"UTF-8");
    	htmlParsedResponse = htmlParsedResponse.replaceAll(Pattern.quote("$(\"#results\").html(\""),"")
			.replaceAll(Pattern.quote("\\u003C"), "<")
			.replaceAll(Pattern.quote("\\u003E"), ">")
			.replaceAll(Pattern.quote("\")"),"")
			.replaceAll(Pattern.quote("\\\""), "\"");
		return htmlParsedResponse;
    }
    
    
    
    private String htmlDecoded(String html) {
		// Buscar elementos unicode :
		Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
		Matcher matcher = pattern.matcher(html);
		if (matcher.find()) {
			// Si hay creamos un StringBuffer para la nueva cadena
			StringBuffer sb = new StringBuffer(html.length());
			do {
				// cada vez que se encuentre un elemento se recupera su valor unicode 
				int codePoint = Integer.parseInt(matcher.group(1), 16);
				String value = new String(Character.toChars(codePoint));
				matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
			} while (matcher.find());
			// se copia el final de la cadena
			matcher.appendTail(sb);
			html = sb.toString();
		}
		return html;
	}


    
    private String getWhitOutHtmlWhiteSpaceTag(String value){
    	return value.replaceAll("&nbsp;", " ").replace("--", "+");
    }

}

package com.trcardmanager.http;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import android.util.Log;

import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.DirectionDao;
import com.trcardmanager.dao.LocationDao;
import com.trcardmanager.dao.MovementDao;
import com.trcardmanager.dao.MovementSeparatorDao;
import com.trcardmanager.dao.MovementsDao;
import com.trcardmanager.dao.RestaurantDao;
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
    	//Document document = Jsoup.parse(new URL(URL_BASE+httpPage).openStream(), "ISO-8859-1", url);
    	//return connection.get();
    	Document d = Jsoup.parse(new String(response.bodyAsBytes(),"ISO-8859-1"));
    	return d;
    	//return response.parse();
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
     * @param directionDao
     * @return
     * @throws IOException 
     */
    public List<RestaurantDao> getRestaurants(DirectionDao directionDao) throws IOException{
    	return getRestaurants(directionDao,"");
    }
    
    /**
     * 
     * @param directionDao
     * @param affiliate
     * @return
     * @throws IOException 
     */
    public List<RestaurantDao> getRestaurants(DirectionDao directionDao,String affiliate) throws IOException{
    	String addressSearch = new StringBuilder()
    		.append(directionDao.getStreet())
    		.append(",")
    		.append(directionDao.getLocality())
    		.append(",")
    		.append(directionDao.getSubArea())
    		.append(",")
    		.append(directionDao.getArea())
    		.append(",")
    		.append(directionDao.getPostalCode())
    		.append(",")
    		.append(directionDao.getCountry())
    		.toString();
    	return getRestaurants(addressSearch,affiliate,directionDao.getLocation());
    }
    
    /**
     * 
     * @param addressSearch
     * @return
     * @throws IOException 
     */
    public List<RestaurantDao> getRestaurants(String addressSearch,LocationDao location) throws IOException{
    	return getRestaurants(addressSearch, "",location);
    }
    
    
    /**
     * 
     * @param addressSearch
     * @param affiliate
     * @return
     * @throws IOException 
     */
    public List<RestaurantDao> getRestaurants(String addressSearch,String affiliate,LocationDao location) throws IOException{
    	List<RestaurantDao> restaurants = new ArrayList<RestaurantDao>();
    	int page = 1, numberOfPages = 1;
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
    	Map<String, String> postMap = new LinkedHashMap<String, String>();
		postMap.put("producto", SEARCH_RESTAURANTS_PRODUCT);
		postMap.put("formato",SEARCH_RESTAURANTS_FORMAT);
		postMap.put("affiliate",affiliate);
		//postMap.put("address",addressSearch);
		postMap.put("address","Avenida de manoteras 20, Madrid");
		postMap.put("center_lng",String.valueOf(location.getLongitude()));
		postMap.put("center_lng","-3.669615199999953");
		postMap.put("center_lat",String.valueOf(location.getLatitude()));
		postMap.put("center_lat","40.487491899999995");
		double searchRadius = 0.001;
		postMap.put("limit_lng",String.valueOf(location.getLongitude()+searchRadius));
		postMap.put("limit_lng","-3.667266219708471");
		postMap.put("limit_lat",String.valueOf(location.getLatitude()+searchRadius));
		postMap.put("limit_lat","40.489840880291496");
		if(page!=1){
			postMap.put("page",String.valueOf(page));
		}
		postMap.put("locale", Locale.getDefault().getCountry().toLowerCase());
				
		Response response = Jsoup.connect(URL_SEARCH_RESTAURANTS)
			.header("X-Requested-With","XMLHttpRequest")
			.timeout(TIMEOUT)
			.data(postMap).execute();
		if(response != null){
			/*
			 * clase result-list
			  bucle <li>
				<a href="coordenadas" class="viewmap"
				clase result
					<a class="name">nombre</a>
					"/"
					<span class="phone">teléfono
					<strong > calle, codigopostal
					" / "
					<span class="adores" > ciudad, comunidad
					" / tipocomida" 

			 */
			String htmlResponse = new String(response.bodyAsBytes(),"ISO-8859-1");
			htmlResponse = htmlResponse.replaceAll(Pattern.quote("$(\"#results\").html(\""),"")
				.replaceAll(Pattern.quote("\\u003C"), "<")
				.replaceAll(Pattern.quote("\\u003E"), ">")
				.replaceAll(Pattern.quote("\")"),"")
				.replaceAll(Pattern.quote("\\\""), "\"");
			
			Document d = Jsoup.parse(htmlResponse);
			Element resultList = d.getElementsByClass("result-list").get(0);
			Elements elementsLi = resultList.getElementsByTag("li");
			for(Element li : elementsLi){
				String logitudeAndLatitude = li.getElementsByClass("viewmap").get(0).attr("href");
				StringTokenizer strk = new StringTokenizer(logitudeAndLatitude, ",");
				double longitude = Double.parseDouble(((String)strk.nextElement()).trim());
				double latitude = Double.parseDouble(((String)strk.nextElement()).trim());
				Element divResult = li.getElementsByClass("result").get(0);
				String name = divResult.getElementsByClass("name").get(0).html();
				String phone = divResult.getElementsByClass("phone").get(0).html();
				String streetAndPostalCode = divResult.getElementsByTag("strong").get(0).html();
				strk = new StringTokenizer(streetAndPostalCode,",");
				String street = (String)strk.nextElement();
				String postalCode = ((String)strk.nextElement()).trim();
				String cityAndSubArea = divResult.getElementsByClass("address").get(0).html();
				strk = new StringTokenizer(cityAndSubArea,",");
				String city = (String)strk.nextElement();
				String subArea = ((String)strk.nextElement()).trim();
				String totalStringResult = divResult.html();
				int posLastBackslash = totalStringResult.lastIndexOf("/");
				String foodType = totalStringResult.substring(posLastBackslash+1).trim();
				
				
				Log.i(TAG, "Restaurant --> coordenates:"+longitude+","+latitude
						+"  Name:"+name
						+"  Phone:"+phone
						+"  Street:"+street
						+"  PostalCode:"+postalCode
						+"  City:"+city
						+"  SubArea:"+subArea
						+"  FoodType:"+foodType);
			}
			Element elementPagination = d.getElementsByClass("pagination").get(0);
			numberOfPages = elementPagination.getElementsByTag("a").size();
		}
		page++;
    	}while(page<=numberOfPages);
    	return restaurants;
    }
    
    private String getWhitOutHtmlWhiteSpaceTag(String value){
    	return value.replaceAll("&nbsp;", " ");
    }

}

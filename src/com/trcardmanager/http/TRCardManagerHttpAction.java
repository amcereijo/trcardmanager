package com.trcardmanager.http;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.MovementDao;
import com.trcardmanager.dao.MovementSeparatorDao;
import com.trcardmanager.dao.MovementsDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerLoginException;
import com.trcardmanager.exception.TRCardManagerSessionException;
import com.trcardmanager.exception.TRCardManagerUpdateCardException;

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
    private static final String URL_PREPARE_UPDATE_CARD = "mi_cuenta.html";
    
       
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
        Log.d(TAG, "Query actual card: " + numCardValue);
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
        Log.d(TAG, "consulta saldo get: " + balance);
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
    		Log.d(TAG, "Movement found: "+movement.getTrade()+" --> "+movement.getAmount());
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
    
    
    
    
    public void activateCard(UserDao user, CardDao card) throws IOException, TRCardManagerUpdateCardException, TRCardManagerDataException, TRCardManagerSessionException{
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
        	
        	Map<String, String> postMap = new HashMap<String, String>();
				postMap.put("swlang",Locale.getDefault().getCountry());
				postMap.put("id",id);
				postMap.put("profile",UPDATE_CARD_PROFILE_PARAMETER);
				postMap.put("num_card",card.getCardNumber());

			Response response = Jsoup.connect(URL_BASE+URL_UPDATE_CARD).cookie(COOKIE_NAME,user.getCookieValue()).timeout(TIMEOUT).data(postMap).execute();
			if(response != null){
				String responseString = response.body();
				if(!UPDATE_CARD_RESPONSE_OK.equals(responseString)){
    				throw new TRCardManagerUpdateCardException(); 
    			}
			}else{
				throw new TRCardManagerUpdateCardException();
			}
        }
    
    
    public String getPrepareUpdateCard(UserDao user) throws ClientProtocolException,
			IOException, TRCardManagerDataException, TRCardManagerSessionException{
    	Document document = getHttpPage(URL_PREPARE_UPDATE_CARD, user.getCookieValue());
		Element form = document.getElementById("updCard");
		Element inputHiddenId = form.getElementsByAttributeValue("name", "id").first();
		String id = inputHiddenId.attr("value");
		return id;
	}
    
    private String getWhitOutHtmlWhiteSpaceTag(String value){
    	return value.replaceAll("&nbsp;", " ");
    }

}

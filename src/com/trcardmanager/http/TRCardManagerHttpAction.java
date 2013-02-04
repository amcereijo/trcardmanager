package com.trcardmanager.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.trcardmanager.dao.DirectionDao;
import com.trcardmanager.dao.LocationDao;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.exception.TRCardManagerSessionException;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerHttpAction {

	private static final String TAG = TRCardManagerHttpAction.class.getName();
	
	//private static final String URL_SEARCH_RESTAURANTS = "http://www.edenred.es/buscador-afiliados/imprimir_resultados";
    private static final String URL_SEARCH_RESTAURANTS = "http://www.edenred.es/affiliates_search/search";//_locations";
    private static final String URL_INFO_RESTAURANT = "http://www.edenred.es/affiliates_search/show_affiliate/%s";
    private static final String SEARCH_RESTAURANTS_PRODUCT = "ticket-restaurant";
    private static final String SEARCH_RESTAURANTS_FORMAT = "tarjeta";
	
	protected static final String LANG_PARAMETER_VALUE = "es";
	protected static final String LANG_PARAMETER = "swlang";
	protected static final int TIMEOUT = 15000;
	protected static final String COOKIE_NAME = "CWNWSESSION";
    protected static final String URL_BASE = "https://ticketrestaurant.edenred.es/TRC/";
    protected static final String RESPONSE_OK = "OK";
    protected static final String URL_MY_ACCOUNT = "mi_cuenta.html";
    
    
   
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
    	Map<String, String> postMap = createParameterPostMapper(restaurantSeachDao.getAddressSearch(), 
    			restaurantSeachDao.getAffiliate(), restaurantSeachDao.getDirectionDao(),
    			restaurantSeachDao.getCurrentPage());
    	Connection con = Jsoup.connect(URL_SEARCH_RESTAURANTS)
			.method(Method.POST)
			.header("X-Requested-With","XMLHttpRequest")
			.timeout(TIMEOUT).data(postMap);
		Response response = con.execute();
		if(response != null){
			String htmlParsedResponse = getParsedResponse(response);
			String json = prepareJsonMessage(htmlParsedResponse);
			try{
				JSONObject jsonObject = new JSONObject(json);
				Log.i(TAG,"JSON:::: "+jsonObject.toString());
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
			}
		}
		restaurantSeachDao.setCurrentPage(restaurantSeachDao.getCurrentPage()+1);
		//restaurantSeachDao.getRestaurantList().addAll(restaurants);
		restaurantSeachDao.setSearchDone(Boolean.TRUE);
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
			String description = result.getString("description");
			if(description.contains("establecimientos en")){
				throw new Exception("No nos quedamos con este establecimiento porque no es concreto.");
			}
			getRestaurantBasicInfo(description,restaurant);
			restaurant.setCountry("ESPAÑA");
			restaurant.setRestaurantLink(result.getString("list_container"));
    	}catch(Exception e){
    		Log.e(TAG, "Error reading json element: "+result,e);
    		restaurant = null;
    	}
		return restaurant;
    }
    
    

    protected Document getHttpPage(String httpPage, String cookieValue) throws IOException,TRCardManagerSessionException{
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
    
    
    
    
	private String prepareJsonMessage(String htmlParsedResponse) {
		String jsonDataStart = "Gmaps.map.replaceMarkers([";
		int posI = htmlParsedResponse.indexOf(jsonDataStart) + jsonDataStart.length();
		int posF = htmlParsedResponse.indexOf("]);",posI);		
		String json = htmlParsedResponse.substring(posI, posF);
		json = "{\"results\":["+json+"]}";
		//If get a result that represents more than one element and scape " elements
		json = json.replaceAll("\\\\\"", "\\\"");
		return json;
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
		double searchRadius = 0.007;//*20;

		postMap.put("limit_lng",String.valueOf(location.getLongitude()+searchRadius));
		postMap.put("limit_lat",String.valueOf(location.getLatitude()+searchRadius));
		if(page!=1){
			postMap.put("page",String.valueOf(page));
		}
		
		postMap.put("locale", Locale.getDefault().getCountry().toLowerCase());

		return postMap;
	}
	
	
	/**
	 * 
	 * @param restaurantSeachDao
	 * @return
	 * @throws IOException
	 */
	public List<RestaurantDao> getRestaurantsAdvanced(RestaurantSearchDao restaurantSeachDao) throws IOException{
    	List<RestaurantDao> restaurants = new ArrayList<RestaurantDao>();
    	DirectionDao directionDao = restaurantSeachDao.getDirectionDao();
    	
    	do{
    	Map<String, String> postMap = new HashMap<String, String>();
			postMap.put("advanced", "1");
			postMap.put("producto", "ticket-restaurant");
			postMap.put("formato", "tarjeta");
			postMap.put("city", directionDao.getLocality());// "Madrid");
			postMap.put("province", directionDao.getArea());// "");
			postMap.put("address_cp", directionDao.getPostalCode());// "");
			postMap.put("address_type", directionDao.getAddressType());// "");
			postMap.put("address_name", directionDao.getStreet());// "");
			postMap.put("address_number", directionDao.getStreetNumber());
			postMap.put("affiliate", restaurantSeachDao.getAffiliate());// "Vips");
			postMap.put("locale", "es");
    		postMap.put("page", ""+restaurantSeachDao.getCurrentPage());
    		
    	Connection con = Jsoup.connect("http://www.edenred.es/affiliates_search/search")
			.method(Method.POST)
			.header("X-Requested-With","XMLHttpRequest")
			.timeout(TIMEOUT).data(postMap);
		Response response = con.execute();
		if(response != null){
			String htmlParsedResponse = getParsedResponse2(response);
			Document d = Jsoup.parse(htmlParsedResponse);
			//Log.i(TAG, htmlParsedResponse);
			Elements resultList = d.getElementsByClass("result-list");
			if(resultList!=null && resultList.size()>0){
				Elements restaurantsLi = resultList.get(0).getElementsByTag("li");
				boolean firstResult = true;
				for(Element elRest : restaurantsLi){
					RestaurantDao restaurantDao = new RestaurantDao();
					
					Element elemFoodType = elRest.getElementsByClass("tipo-label").first();
					restaurantDao.setFoodType(htmlDecoded(elemFoodType.html()));
					
					if("".equals(restaurantSeachDao.getFoodType()) || 
						(!"".equals(restaurantSeachDao.getFoodType()) && 
						restaurantDao.getFoodType().equalsIgnoreCase(restaurantSeachDao.getFoodType()))){
						
						Element map =  elRest.getElementsByTag("a").first();
							String hrefElemVal = map.attr("href"); // http://maps.google.com/?q=to:40.4426693,-3.7131982
							hrefElemVal = hrefElemVal.replaceAll(Pattern.quote("http://maps.google.com/?q=to:"), "");
							String[] longLat = hrefElemVal.split("[,]");
							LocationDao resLocationDao = new LocationDao(Float.parseFloat(longLat[1].trim()), Float.parseFloat(longLat[0].trim()));
							restaurantDao.setLocation(resLocationDao);
						Element elemClassResult = elRest.getElementsByClass("result").first();
						Element elementName = elemClassResult.getElementsByTag("a").first();
						restaurantDao.setRetaurantName(htmlDecoded(elementName.html()));
						Element elemPhone = elemClassResult.getElementsByClass("phone").first();
						restaurantDao.setPhoneNumber(elemPhone.html());
						Element elemStreetPostaCode = elemClassResult.getElementsByTag("strong").first();
						String[] valStreetPostaCode = elemStreetPostaCode.html().split(",");
						restaurantDao.setStreet(htmlDecoded(valStreetPostaCode[0]));
						restaurantDao.setPostalCode(valStreetPostaCode[1]);
						Element elemAddress = elemClassResult.getElementsByClass("address").first();
						String[] valAddress = elemAddress.html().split(",");
						restaurantDao.setLocality(htmlDecoded(valAddress[0]));
						restaurantDao.setSubArea(htmlDecoded(valAddress[1]));
						
						restaurantDao.setCompleteDataLoaded(Boolean.TRUE);
						
						restaurants.add(restaurantDao);
						
						if(firstResult && restaurantSeachDao.getCurrentPage()==1){
							restaurantSeachDao.getDirectionDao().setLocation(resLocationDao);
							firstResult = false;
						}
					}
				}
				
				Element elemPagination = d.getElementsByClass("pagination").first();
				if(elemPagination!=null){
					int lastPag = elemPagination.getElementsByTag("a").size()-2;
					Element lastSecureAElement = elemPagination.getElementsByTag("a").get(lastPag);
					int lastSecureAElementVal = Integer.parseInt(lastSecureAElement.html());;
					restaurantSeachDao.setCurrentPage(Integer.parseInt(elemPagination.getElementsByClass("current").first().html()));
					if(restaurantSeachDao.getCurrentPage()<=lastSecureAElementVal){
						restaurantSeachDao.setNumberOfPages(lastSecureAElementVal);
					}
				}else{
					restaurantSeachDao.setNumberOfPages(restaurantSeachDao.getCurrentPage());
				}
				
			}else{
				//no results
			}
		}
		Log.i(TAG, "Done page "+restaurantSeachDao.getCurrentPage()+" of "+restaurantSeachDao.getNumberOfPages()+"!!");
		restaurantSeachDao.setCurrentPage(restaurantSeachDao.getCurrentPage()+1);
    	}while(restaurantSeachDao.getCurrentPage() <= restaurantSeachDao.getNumberOfPages());
    	
//    	if(restaurantSeachDao.getRestaurantList()==null){
//    		restaurantSeachDao.setRestaurantList(restaurants);
//    	}else{
//    		restaurantSeachDao.getRestaurantList().addAll(restaurants);
//    	}
    	
    	restaurantSeachDao.setSearchDone(Boolean.TRUE);
    	return restaurants;
	}
	
	
	
	private String getParsedResponse2(Response response)throws UnsupportedEncodingException{
    	String htmlParsedResponse = new String(response.bodyAsBytes(),"UTF-8");
    	htmlParsedResponse = htmlParsedResponse.replaceAll(Pattern.quote("$(\"#results\").replaceWith(\""),"")
			.replaceAll(Pattern.quote("\\u003C"), "<")
			.replaceAll(Pattern.quote("\\u003E"), ">")
			.replaceAll(Pattern.quote("\")"),"")
			.replaceAll(Pattern.quote("\\\""), "\"")
			.replaceAll(Pattern.quote("\");"), "");
    	int startRemov = htmlParsedResponse.indexOf("$(\"#filter-advanced-search.replaceWith");
    	htmlParsedResponse = htmlParsedResponse.substring(0,startRemov);
		return htmlParsedResponse;
    }
	
	
}

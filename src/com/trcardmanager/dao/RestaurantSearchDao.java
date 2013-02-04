package com.trcardmanager.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class to manage data to find restaurants
 * @author angelcereijo
 *
 */
public class RestaurantSearchDao {
	
	public enum SearchViewType{
		MAP_VIEW,
		LIST_VIEW
	}
	
	public enum SearchType{
		DIRECTION_SEARCH,
		LOCATION_SEARCH
	}
	
	private String affiliate = "";
	private String foodType = "";
	
	private SearchType searchType;
	
	private DirectionDao directionDao;
	private List<RestaurantDao> restaurantList;
	private int numberOfPages;
	private int currentPage = 1;
	private SearchViewType searchViewType = SearchViewType.MAP_VIEW;
	private boolean searchDone = Boolean.FALSE;
	
	
	public DirectionDao getDirectionDao() {
		return directionDao;
	}
	public void setDirectionDao(DirectionDao directionDao) {
		this.directionDao = directionDao;
	}
	
	public String getAddressSearch() {
		return directionDaoToString();
	}
	
	
	public String getAffiliate() {
		return affiliate;
	}
	public void setAffiliate(String affiliate) {
		this.affiliate = affiliate;
	}
	
	public List<RestaurantDao> getRestaurantList() {
		if(restaurantList==null){
			restaurantList = new ArrayList<RestaurantDao>();
		}
		return restaurantList;
	}
	public void setRestaurantList(List<RestaurantDao> restaurantList) {
		this.restaurantList = restaurantList;
	}
	
	public int getNumberOfPages() {
		return numberOfPages;
	}
	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	
	public void setSearchViewType(SearchViewType searchViewType) {
		this.searchViewType = searchViewType;
	}
	public SearchViewType getSearchViewType() {
		return searchViewType;
	}
	
	public void setSearchDone(boolean searchDone) {
		this.searchDone = searchDone;
	}
	public boolean isSearchDone() {
		return searchDone;
	}
	
	public void setFoodType(String foodType) {
		this.foodType = foodType;
	}
	public String getFoodType() {
		return foodType;
	}
	
	public void setSearchType(SearchType searchType) {
		this.searchType = searchType;
	}
	public SearchType getSearchType() {
		return searchType;
	}
	
	private String directionDaoToString(){
		String addressSearch = "";
		if(directionDao!=null){
			addressSearch = String.format("%s %s, %s, %s, %s, %s, %s",directionDao.getAddressType(),
					directionDao.getStreet(), directionDao.getLocality(), directionDao.getSubArea(),
					directionDao.getArea(), directionDao.getPostalCode(), directionDao.getCountry())
					.trim();
			if(addressSearch.startsWith(", ")){
				addressSearch = addressSearch.replaceFirst(Pattern.quote(", "), "");
			}
			while(addressSearch.contains(", ,")){
				addressSearch = addressSearch.replaceFirst(Pattern.quote(", ,"), ",");
				addressSearch = addressSearch.replaceFirst(Pattern.quote(",,"), ",");
			}
			if(addressSearch.endsWith(",")){
				addressSearch = addressSearch.substring(0,addressSearch.length()-1);
			}
		}
		return addressSearch;
	}
	
}

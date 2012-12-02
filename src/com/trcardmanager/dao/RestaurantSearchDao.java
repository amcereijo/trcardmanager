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
	
	private DirectionDao directionDao;
	private String addressSearch = "";
	private String affiliate = "";
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
		if(addressSearch==null || "".equals(addressSearch)){
			addressSearch = directionDaoToString();
		}
		return addressSearch;
	}
	public void setAddressSearch(String addressSearch) {
		this.addressSearch = addressSearch;
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
	
	private String directionDaoToString(){
		String addressSearch = "";
		if(directionDao!=null){
			addressSearch = new StringBuilder()
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
			addressSearch = addressSearch.replaceAll(Pattern.quote("null,"), "")
				.replaceAll(Pattern.quote(",null"), "")
				.replaceAll(Pattern.quote(",,"), "");
		}
		return addressSearch;
	}

}

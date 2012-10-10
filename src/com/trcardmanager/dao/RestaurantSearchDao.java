package com.trcardmanager.dao;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Class to manage data to find restaurants
 * @author angelcereijo
 *
 */
public class RestaurantSearchDao {
	
	private DirectionDao directionDao;
	private String addressSearch = "";
	private String affiliate = "";
	private List<RestaurantDao> restaurantList;
	private int numberOfPages;
	private int currentPage = 1;
	
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

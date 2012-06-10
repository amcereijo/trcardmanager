package com.trcardmanager.dao;

import java.util.List;

public class MovementsDao {
	
	public final static int MAX_MOVEMENTS_PAGE = 10;
	
	private List<String> paginationLinks;
	private List<MovementDao> movements;
	private String dateStart;
	private String dateEnd;
	private int numberOfPages;
	private int actualPage;
	private int historicalNumberOfPages;
	private int historicalActualPage;
	
	public void setPaginationLinks(List<String> paginationLinks) {
		this.paginationLinks = paginationLinks;
	}
	public List<String> getPaginationLinks() {
		return paginationLinks;
	}
	
	public List<MovementDao> getMovements() {
		return movements;
	}
	public void setMovements(List<MovementDao> movements) {
		this.movements = movements;
	}
	
	public String getDateStart() {
		return dateStart;
	}
	public void setDateStart(String dateStart) {
		this.dateStart = dateStart;
	}
	
	public String getDateEnd() {
		return dateEnd;
	}
	public void setDateEnd(String dateEnd) {
		this.dateEnd = dateEnd;
	}
	
	public int getNumberOfPages() {
		return numberOfPages;
	}
	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}
	
	public int getActualPage() {
		return actualPage;
	}
	public void setActualPage(int actualPage) {
		this.actualPage = actualPage;
	}
	
	public void setHistoricalActualPage(int historicalActualPage) {
		this.historicalActualPage = historicalActualPage;
	}
	public int getHistoricalActualPage() {
		return historicalActualPage;
	}
	
	public void setHistoricalNumberOfPages(int historicalNumberOfPages) {
		this.historicalNumberOfPages = historicalNumberOfPages;
	}
	public int getHistoricalNumberOfPages() {
		return historicalNumberOfPages;
	}
	
}

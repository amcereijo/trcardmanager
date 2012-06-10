package com.trcardmanager.dao;

/**
 * 
 * @author angelcereijo
 *
 */
public class MovementSeparatorDao extends MovementDao {
	private boolean historical = true;
	
	public boolean isHistorical() {
		return historical;
	}
}

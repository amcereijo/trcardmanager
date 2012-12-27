package com.trcardmanager.action;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerSessionException;
import com.trcardmanager.http.TRCardManagerHttpCardAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class UserDataAction {

	/**
	 * Load balance and movement for a user´s actual card
	 * @param userDao
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws TRCardManagerDataException
	 * @throws TRCardManagerSessionException
	 */
	public void loadAndSaveUserData(UserDao userDao) throws ClientProtocolException, IOException,
		TRCardManagerDataException, TRCardManagerSessionException{
		
			TRCardManagerHttpCardAction httpAction = new TRCardManagerHttpCardAction();
			TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(TRCardManagerApplication.getActualActivity());
			//http actions
			httpAction.getActualCard(userDao);
			httpAction.getActualCardBalanceAndMovements(userDao);
			
			CardDao card = userDao.getActualCard();
			if( card == null || card.getMovementsData() == null ||
					card.getMovementsData().getMovements() ==null){
				throw new TRCardManagerDataException("Error getting user data");
			}else{
				//db actions
				dbHelper.addCard(userDao.getRowId(), userDao.getActualCard());
				dbHelper.updateCardBalance(userDao.getActualCard());
				dbHelper.findUserCards(userDao);
			}   	
	}
}

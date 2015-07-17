package com.bookstuf.appengine;

import java.io.Serializable;
import java.util.Iterator;

import org.slim3.datastore.Datastore;

import com.bookstuf.DatastoreHelper;
import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserInformation;
import com.bookstuf.datastore.UserInformationMeta;
import com.bookstuf.datastore.UserMeta;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Transaction;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Singleton;

@Singleton
public class UserService implements Serializable {
	private static final long serialVersionUID = 1043735132447223363L;

	public User getCurrentUser(
		final GitkitUser gitkitUser, 
		final Transaction transaction
	) throws
		NotLoggedInException
	{
		if (gitkitUser == null) {
			throw new NotLoggedInException();
		}
		
		User user = 
			DatastoreHelper.getInstanceByProperty(
				transaction, 
				User.class, 
				"gitkitUserId", 
				gitkitUser.getLocalId());
		
		if (user == null) {
			user = new User();
			user.setGitkitUserId(gitkitUser.getLocalId());
			user.setGitkitUserEmail(gitkitUser.getEmail());
		}
		
		return user;
	}

	public UserInformation getCurrentUserInformation(
		final GitkitUser gitkitUser, 
		final Transaction transaction
	) throws 
		NotLoggedInException 
	{
		if (gitkitUser == null) {
			throw new NotLoggedInException();
		}
		
		UserInformation userInformation =
			DatastoreHelper.getInstanceByProperty(
				transaction, 
				UserInformation.class, 
				"gitkitUserId", 
				gitkitUser.getLocalId());
		
		if (userInformation == null) {
			userInformation = new UserInformation();
			userInformation.setGitkitUserId(gitkitUser.getLocalId());
		}
		
		return userInformation;
	}
}

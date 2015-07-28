package com.bookstuf.appengine;

import java.io.Serializable;

import org.slim3.datastore.Datastore;
import org.slim3.datastore.EntityNotFoundRuntimeException;

import com.bookstuf.datastore.ProviderInformationStatus;
import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserInformation;
import com.bookstuf.datastore.UserServices;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class UserService implements Serializable {
	private static final long serialVersionUID = 1043735132447223363L;
	private Provider<GitkitUser> gitkitUser;

	@Inject UserService (
		final Provider<GitkitUser> gitkitUser
	) {
		this.gitkitUser = gitkitUser;
	}
	
	public Key getCurrentUserKey(
		final Class<?> kind
	) {
		if (kind == User.class) {
			return KeyFactory.createKey(kind.getSimpleName(), "gitkitLocalId:" + gitkitUser.get().getLocalId());
		
		} else {
			return KeyFactory.createKey(getCurrentUserKey(User.class), kind.getSimpleName(), "gitkitLocalId:" + gitkitUser.get().getLocalId());
		}
	}
	
	public User getCurrentUser(
		final Transaction transaction
	) {
		final Key userKey = 
			getCurrentUserKey(User.class);
		
		try {
			return Datastore.get(transaction, User.class, userKey);
		
		} catch (final EntityNotFoundRuntimeException e) {
			final GitkitUser gitkitUserValue = 
				gitkitUser.get();
			
			final User user = new User();
			user.setKey(userKey);
			user.setGitkitUserId(gitkitUserValue.getLocalId());
			user.setGitkitUserEmail(gitkitUserValue.getEmail());
			user.setStripeConnectStatus(null);
			user.setProviderInformationStatus(ProviderInformationStatus.MISSING);		
			return user;
		}
	}

	public UserInformation getCurrentUserInformation(
		final Transaction transaction
	) {
		final Key userInformationKey = 
			getCurrentUserKey(UserInformation.class);
		
		try {
			return Datastore.get(transaction, UserInformation.class, userInformationKey);
			
		} catch (final EntityNotFoundRuntimeException e) {
			final UserInformation userInformation = new UserInformation();
			userInformation.setKey(userInformationKey);
			return userInformation;
		}
	}

	public UserServices getCurrentUserServices(
		final Transaction transaction
	) {
		final Key userServicesKey = 
			getCurrentUserKey(UserServices.class);
		
		try {
			return Datastore.get(transaction, UserServices.class, userServicesKey);
			
		} catch (final EntityNotFoundRuntimeException e) {
			final UserServices userServices = new UserServices();
			userServices.setKey(userServicesKey);
			return userServices;
		}
	}
}

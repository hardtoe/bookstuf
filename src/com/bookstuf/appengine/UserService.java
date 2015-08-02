package com.bookstuf.appengine;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.slim3.datastore.Datastore;
import org.slim3.datastore.EntityNotFoundRuntimeException;

import com.bookstuf.DatastoreHelper;
import com.bookstuf.datastore.CancellationPolicy;
import com.bookstuf.datastore.ChargePolicy;
import com.bookstuf.datastore.ProviderInformationStatus;
import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserInformation;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class UserService implements Serializable {
	private static final long serialVersionUID = 1043735132447223363L;
	private Provider<GitkitUser> gitkitUser;
	private Provider<ListeningExecutorService> executorService;

	@Inject UserService (
		final Provider<GitkitUser> gitkitUser,
		final Provider<ListeningExecutorService> executorService
	) {
		this.gitkitUser = gitkitUser;
		this.executorService = executorService;
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
			user.setProviderServicesStatus(ProviderInformationStatus.MISSING);		
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
			userInformation.setPhotoUrls(new LinkedList<String>());
			userInformation.setCancellationPolicy(CancellationPolicy.REFUND_IF_CANCEL_IN_TIME);
			userInformation.setChargePolicy(ChargePolicy.CHARGE_AFTER);
			
			return userInformation;
		}
	}

	public UserInformation getUserInformationByHandle(final String handle) {
		return 
			DatastoreHelper.getInstanceByProperty(
				null, 
				UserInformation.class, 
				"handle", 
				handle);
	}
}

package com.bookstuf.appengine;

import java.io.Serializable;
import java.util.LinkedList;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.datastore.CancellationPolicy;
import com.bookstuf.datastore.ChargePolicy;
import com.bookstuf.datastore.PhotoUrl;
import com.bookstuf.datastore.ProviderInformationStatus;
import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserInformation;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

@Singleton
public class UserService implements Serializable {
	private static final long serialVersionUID = 1043735132447223363L;
	private Provider<GitkitUser> gitkitUser;

	@Inject UserService (
		final Provider<GitkitUser> gitkitUser
	) {
		this.gitkitUser = gitkitUser;
	}
	
	public User getCurrentUser() {
		try {
			return ofy().load().type(User.class).id(gitkitUser.get().getLocalId()).safe();
		
		} catch (final NotFoundException e) {
			final GitkitUser gitkitUserValue = 
				gitkitUser.get();
			
			final User user = new User();
			
			user.setGitkitUserId(gitkitUserValue.getLocalId());
			user.setGitkitUserEmail(gitkitUserValue.getEmail());
			user.setStripeConnectStatus(null);
			user.setProviderInformationStatus(ProviderInformationStatus.MISSING);	
			user.setProviderServicesStatus(ProviderInformationStatus.MISSING);	
			
			return user;
		}
	}

	public UserInformation getCurrentUserInformation() {
		try {
			return ofy().load().type(UserInformation.class).id(gitkitUser.get().getLocalId()).safe();
			
		} catch (final NotFoundException e) {
			final GitkitUser gitkitUserValue = 
				gitkitUser.get();
			
			final UserInformation userInformation = new UserInformation();

			userInformation.setGitkitUserId(gitkitUserValue.getLocalId());
			userInformation.setPhotoUrls(new LinkedList<PhotoUrl>());
			userInformation.setCancellationPolicy(CancellationPolicy.REFUND_IF_CANCEL_IN_TIME);
			userInformation.setChargePolicy(ChargePolicy.CHARGE_AFTER);
			
			return userInformation;
		}
	}

	public UserInformation getUserInformationByHandle(final String handle) {
		// TODO: cache the handle -> key relationship
		final Key<UserInformation> key = 
			ofy().load().type(UserInformation.class).filter("handle", handle).keys().first().now();
		
		return ofy().load().key(key).now();
	}
}

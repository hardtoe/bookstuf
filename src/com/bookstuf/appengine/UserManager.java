package com.bookstuf.appengine;

import java.io.Serializable;
import java.util.LinkedList;

import static com.googlecode.objectify.ObjectifyService.ofy;

import com.bookstuf.Memcacheable;
import com.bookstuf.datastore.CancellationPolicy;
import com.bookstuf.datastore.ChargePolicy;
import com.bookstuf.datastore.ConsumerInformation;
import com.bookstuf.datastore.PhotoUrl;
import com.bookstuf.datastore.ProviderInformationStatus;
import com.bookstuf.datastore.ProfessionalPrivateInformation;
import com.bookstuf.datastore.ProfessionalInformation;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Result;

@Singleton
public class UserManager implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final Provider<GitkitUser> gitkitUser;
	private final Memcacheable<String, Key<ProfessionalInformation>> handleToUserInformationKey;

	@Inject UserManager (
		final Provider<GitkitUser> gitkitUser,
		final HandleToProfessionalInformationKeyMemcacheable handleToUserInformationKey
	) {
		this.gitkitUser = gitkitUser;
		this.handleToUserInformationKey = handleToUserInformationKey;
	}
	
	public ProfessionalPrivateInformation getCurrentProfessionalPrivateInformation() {
		try {
			return ofy().load().type(ProfessionalPrivateInformation.class).id(gitkitUser.get().getLocalId()).safe();
		
		} catch (final NotFoundException e) {
			final GitkitUser gitkitUserValue = 
				gitkitUser.get();
			
			final ProfessionalPrivateInformation user = new ProfessionalPrivateInformation();
			
			user.setGitkitUserId(gitkitUserValue.getLocalId());
			user.setGitkitUserEmail(gitkitUserValue.getEmail());
			user.setStripeConnectStatus(null);
			user.setProviderInformationStatus(ProviderInformationStatus.MISSING);	
			user.setProviderServicesStatus(ProviderInformationStatus.MISSING);	
			
			return user;
		}
	}

	public ProfessionalInformation getCurrentProfessionalInformation() {
		try {
			return ofy().load().type(ProfessionalInformation.class).id(gitkitUser.get().getLocalId()).safe();
			
		} catch (final NotFoundException e) {
			final GitkitUser gitkitUserValue = 
				gitkitUser.get();
			
			final ProfessionalInformation userInformation = new ProfessionalInformation();

			userInformation.setGitkitUserId(gitkitUserValue.getLocalId());
			userInformation.setPhotoUrls(new LinkedList<PhotoUrl>());
			userInformation.setCancellationPolicy(CancellationPolicy.REFUND_IF_CANCEL_IN_TIME);
			userInformation.setChargePolicy(ChargePolicy.CHARGE_AFTER);
			
			return userInformation;
		}
	}

	public ProfessionalInformation getProfessionalInformationByHandle(final String handle) {
		return ofy().load().key(handleToUserInformationKey.get(handle)).now();
	}

	public Result<ConsumerInformation> getCurrentConsumerInformation() {
		final GitkitUser gitkitUserValue =
				gitkitUser.get();
		
		final LoadResult<ConsumerInformation> loadResult =
			ofy().load().type(ConsumerInformation.class).id(gitkitUserValue.getLocalId());
		
		return new Result<ConsumerInformation>() {
			private ConsumerInformation result = null;
			
			@Override
			public ConsumerInformation now() {
				if (result == null) {
					try {
						result = loadResult.safe();
						
					} catch (final NotFoundException e) {
						
						final ConsumerInformation consumerInformation = new ConsumerInformation();
						
						consumerInformation.setGitkitUserId(gitkitUserValue.getLocalId());
						consumerInformation.setContactEmail(gitkitUserValue.getEmail());
						
						result = consumerInformation;
					}
				}
				
				return result;
			}
		};
	}
}

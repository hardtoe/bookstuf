package com.bookstuf.appengine;

import java.io.Serializable;
import java.util.Iterator;

import org.slim3.datastore.Datastore;

import com.bookstuf.datastore.User;
import com.bookstuf.datastore.UserMeta;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.identitytoolkit.GitkitUser;
import com.google.inject.Singleton;

@Singleton
public class UserService implements Serializable {
	private static final long serialVersionUID = 1043735132447223363L;

	public User getCurrentUser(
		final GitkitUser gitkitUser
	) throws 
		NotLoggedInException 
	{
		return getCurrentUser(gitkitUser, null);
	}
	
	public User getCurrentUser(
		final GitkitUser gitkitUser, 
		final Transaction t
	) throws
		NotLoggedInException
	{
		if (gitkitUser == null) {
			throw new NotLoggedInException();
		}
		
		final UserMeta u =
			UserMeta.get();
		
		final Iterator<Key> userIterator = 
			Datastore
			.query(User.class)
			.filter(u.gitkitUserId.equal(gitkitUser.getLocalId()))
			.asKeyIterator();
		
		User user;
		
		if (userIterator.hasNext()) {
			if (t == null) {
				user = Datastore.get(User.class, userIterator.next());
				
			} else {
				user = Datastore.get(t, User.class, userIterator.next());
			}
			
		} else {
			user = new User();
			user.setGitkitUserId(gitkitUser.getLocalId());
			user.setGitkitUserEmail(gitkitUser.getEmail());
		}
		
		return user;
	}
}

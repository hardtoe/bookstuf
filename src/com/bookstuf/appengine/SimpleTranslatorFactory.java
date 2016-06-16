package com.bookstuf.appengine;

import org.threeten.bp.LocalDate;

import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.translate.CreateContext;
import com.googlecode.objectify.impl.translate.LoadContext;
import com.googlecode.objectify.impl.translate.SaveContext;
import com.googlecode.objectify.impl.translate.SkipException;
import com.googlecode.objectify.impl.translate.TypeKey;
import com.googlecode.objectify.impl.translate.ValueTranslator;
import com.googlecode.objectify.impl.translate.ValueTranslatorFactory;

public abstract class SimpleTranslatorFactory<P, D> extends ValueTranslatorFactory<P, D> {
	private final Class<D> datastoreType;

	protected SimpleTranslatorFactory(
		final Class<? extends P> pojoType, 
		final Class<D> datastoreType
	) {
		super(pojoType);
		
		this.datastoreType = datastoreType;
	}

	protected abstract D toDatastore(final P value);
	
	protected abstract P toPojo(final D value);
	
	@Override
	protected ValueTranslator<P, D> createValueTranslator(
		final TypeKey<P> typeKey, 
		final CreateContext ctx, 
		final Path path
	) {
		return new ValueTranslator<P, D>(datastoreType) {
			@Override
			protected P loadValue(
				final D value,
				final LoadContext arg1, 
				final Path arg2
			) throws 
				SkipException 
			{
				return toPojo(value);
			}

			@Override
			protected D saveValue(
				final P value, 
				final boolean arg1,
				final SaveContext arg2, 
				final Path arg3
			) throws 
				SkipException 
			{
				return toDatastore(value);
			}
		};
	}

}

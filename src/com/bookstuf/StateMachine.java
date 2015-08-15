package com.bookstuf;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class StateMachine<ContextType, StateType, EventType> {
	protected abstract StateType getCurrentState(final ContextType context);

	protected abstract void setCurrentState(final ContextType context, final StateType state);
	
	public StateType advance(
		final ContextType context, 
		final EventType event
	) {
		StateType nextState = null;
		
		final ArrayList<Arc> arcs =
			arcMap.get(getCurrentState(context));
		
		for (final Arc arc : arcs) {
			if (arc.isTrue(context, event)) {
				nextState = arc.getDstState(context, event);
				setCurrentState(context, nextState);
				break;
			}
		}
		
		return nextState;
	}
	
	protected abstract class Predicate {
		public abstract boolean isTrue(
			final ContextType context, 
			final EventType event);
	}
	
	protected abstract class Action {
		private final ArrayList<Arc> arcs =
			new ArrayList<Arc>();
		
		public void arc(final Arc arc) {
			arcs.add(arc);
		}

		public StateType getDstState(
			final ContextType context, 
			final EventType event
		) {
			for (final Arc arc : arcs) {
				if (arc.isTrue(context, event)) {
					return arc.getDstState(context, run(context, event));
				}
			}
			
			throw new RuntimeException("Actions must always have an arc exiting the action");
		}

		public abstract EventType run(
			final ContextType context, 
			final EventType event);
	}
	
	private abstract class Arc {
		private final Predicate predicate;
		
		protected Arc(final Predicate predicate) {
			this.predicate = predicate;
		}
		
		public final boolean isTrue(
			final ContextType context, 
			final EventType event
		) {
			return predicate.isTrue(context, event);
		}
		
		protected abstract StateType getDstState(
			final ContextType context, 
			final EventType event);
	}
	
	private class ActionArc extends Arc {
		private final Action dstAction;
		
		public ActionArc(
			final Action dstAction,
			final Predicate predicate
		) {
			super(predicate);
			
			this.dstAction = dstAction;
		}

		@Override
		protected StateType getDstState(
			final ContextType context, 
			final EventType event
		) {
			return dstAction.getDstState(context, event);
		}
		
	}
	
	private class StateArc extends Arc {
		private final StateType dstState;
		
		public StateArc(
			final StateType dstState,
			final Predicate predicate
		) {
			super(predicate);
			this.dstState = dstState;
		}

		@Override
		protected StateType getDstState(
			final ContextType context, 
			final EventType event
		) {
			return dstState;
		}	
	}
	
	protected final void arc(
		final StateType srcState,
		final StateType dstState,
		final Predicate predicate
	) {
		arc(srcState, new StateArc(dstState, predicate));
	}
	
	protected final void arc(
		final StateType srcState,
		final StateType dstState
	) {
		arc(srcState, new StateArc(dstState, TRUE));
	}
	
	protected final void arc(
		final StateType srcState,
		final Action dstAction,
		final Predicate predicate
	) {
		arc(srcState, new ActionArc(dstAction, predicate));
	}
	
	protected final void arc(
		final StateType srcState,
		final Action dstAction
	) {
		arc(srcState, new ActionArc(dstAction, TRUE));
	}

	protected final void arc(
		final Action srcAction,
		final StateType dstState,
		final Predicate predicate
	) {
		srcAction.arc(new StateArc(dstState, predicate));
	}
	
	protected final void arc(
		final Action srcAction,
		final StateType dstState
	) {
		srcAction.arc(new StateArc(dstState, TRUE));
	}
	
	protected final void arc(
		final Action srcAction,
		final Action dstAction,
		final Predicate predicate
	) {
		srcAction.arc(new ActionArc(dstAction, predicate));
	}
	
	protected final void arc(
		final Action srcAction,
		final Action dstAction
	) {
		srcAction.arc(new ActionArc(dstAction, TRUE));
	}
	
	final HashMap<StateType, ArrayList<Arc>> arcMap =
		new HashMap<>();
	
	private void arc(
		final StateType srcState,
		final Arc arc
	) {
		ArrayList<Arc> arcs =
			arcMap.get(srcState);
		
		if (arcs == null) {
			arcs = new ArrayList<Arc>();
			arcMap.put(srcState, arcs);
		}
		
		arcs.add(arc);
	}
	
	private Predicate TRUE = new Predicate() {
		@Override
		public boolean isTrue(
			final ContextType context, 
			final EventType event
		) {
			return true;
		}
	};
}

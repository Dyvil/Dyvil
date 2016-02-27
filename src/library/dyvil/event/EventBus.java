package dyvil.event;

public interface EventBus
{
	/**
	 * Registers an event handler.
	 *
	 * @param eventHandler
	 * 		the event handler to register
	 */
	default void register(Object eventHandler)
	{
		this.register(eventHandler, eventHandler.getClass());
	}

	/**
	 * Registers a static event handler. Events will only be dispatched to static methods of this handler.
	 *
	 * @param type
	 * 		the event handler type
	 */
	default void register(Class<?> type)
	{
		this.register(null, type);
	}

	/**
	 * Registers the given <code>eventHandler</code> of the given <code>type</code> for this Event Bus. The even handler
	 * can be null, in which case only dispatch to static handler methods will be enabled.
	 *
	 * @param eventHandler
	 * 		the event handler to register. May be null.
	 * @param type
	 * 		the type of the event handler
	 */
	void register(Object eventHandler, Class<?> type);

	/**
	 * Dispatches the given <code>event</code> to all event handlers.
	 *
	 * @param event
	 * 		the event to dispatch to all handlers.
	 */
	void dispatch(Object event);
}

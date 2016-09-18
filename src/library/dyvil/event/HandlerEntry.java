package dyvil.event;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;

import java.lang.reflect.Method;

public class HandlerEntry
{
	private final Object   eventHandler;
	private final Class<?> type;
	private final Map<Class<?>, Method> targetMethods = new HashMap<>();

	public HandlerEntry(Object eventHandler, Class<?> type)
	{
		this.eventHandler = eventHandler;
		this.type = type;
	}

	public void addMapping(Class<?> type, Method target)
	{
		this.targetMethods.put(type, target);
	}

	public Object getEventHandler()
	{
		return this.eventHandler;
	}

	public Class<?> getType()
	{
		return this.type;
	}

	public Method getTargetMethod(Class<?> type)
	{
		return this.targetMethods.get(type);
	}
}

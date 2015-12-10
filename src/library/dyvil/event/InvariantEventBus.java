package dyvil.event;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.reflect.Modifiers;

import java.lang.reflect.Method;

public class InvariantEventBus implements EventBus
{
	protected List<HandlerEntry> handlers = new ArrayList<>();

	@Override
	public void register(Object eventHandler, Class<?> type)
	{
		HandlerEntry entry = new HandlerEntry(eventHandler, type);
		this.handlers.add(entry);

		for (Method method : type.getDeclaredMethods())
		{
			if (method.getParameterCount() != 1)
			{
				continue;
			}
			if (method.getAnnotation(EventHandler.class) == null)
			{
				return;
			}
			if (((method.getModifiers() & Modifiers.STATIC) == 0) == (eventHandler == null))
			{
				continue;
			}

			Class<?> parameterType = method.getParameterTypes()[0];
			entry.addMapping(parameterType, method);
		}
	}

	@Override
	public void dispatch(Object event)
	{
		Class<?> type = event.getClass();

		for (HandlerEntry entry : this.handlers)
		{
			Object handler = entry.getEventHandler();
			Method handlerMethod = entry.getTargetMethod(type);

			if (handlerMethod != null)
			{
				this.invoke(handlerMethod, handler, event);
			}
		}
	}

	protected final void invoke(Method method, Object eventHandler, Object event)
	{
		try
		{
			method.setAccessible(true);
			method.invoke(eventHandler, event);
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace();
		}
	}
}

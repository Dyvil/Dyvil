package dyvil.event;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.reflect.Modifiers;

import java.lang.reflect.Method;

public class InvariantEventBus implements EventBus
{
	@NonNull
	protected List<HandlerEntry> handlers = new ArrayList<>();

	@Override
	public void register(@Nullable Object eventHandler, @NonNull Class<?> type)
	{
		final HandlerEntry entry = new HandlerEntry(eventHandler, type);
		this.handlers.add(entry);

		for (Method method : type.getDeclaredMethods())
		{
			// Must be unary method with @EventHandler annotation
			if (method.getParameterCount() != 1 || method.getAnnotation(EventHandler.class) == null)
			{
				continue;
			}
			// If the method is non-static, the event handler object must not be null
			if ((method.getModifiers() & Modifiers.STATIC) == 0 && eventHandler == null)
			{
				continue;
			}

			Class<?> parameterType = method.getParameterTypes()[0];
			entry.addMapping(parameterType, method);
		}
	}

	@Override
	public void dispatch(@NonNull Object event)
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

	protected final void invoke(@NonNull Method method, Object eventHandler, Object event)
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

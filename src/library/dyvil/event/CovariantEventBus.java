package dyvil.event;

import java.lang.reflect.Method;

public class CovariantEventBus extends InvariantEventBus
{
	@Override
	public void dispatch(Object event)
	{
		Class<?> type = event.getClass();

		for (HandlerEntry entry : this.handlers)
		{
			Object handler = entry.getEventHandler();

			this.invokeRecursively(entry, handler, type, event);
		}
	}

	private void invokeRecursively(HandlerEntry entry, Object handler, Class<?> type, Object event)
	{
		Method handlerMethod = entry.getTargetMethod(type);
		if (handlerMethod != null)
		{
			this.invoke(handlerMethod, handler, event);
		}

		Class<?> superClass = type.getSuperclass();
		if (superClass != null)
		{
			this.invokeRecursively(entry, handler, superClass, event);
		}

		for (Class<?> itf : type.getInterfaces())
		{
			this.invokeRecursively(entry, handler, itf, event);
		}
	}
}

package dyvil.reflect;

public class Caller
{
	/**
	 * Returns the caller {@link Class}.
	 * 
	 * @return the called class.
	 */
	public static Class getCallerClass()
	{
		try
		{
			return Class.forName(getCallerClassName());
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the name of the caller class.
	 * 
	 * @return the name of the caller class.
	 */
	public static String getCallerClassName()
	{
		return getCaller().getClassName();
	}
	
	/**
	 * Returns the caller {@link StackTraceElement}.
	 * 
	 * @return the caller stack trace element
	 */
	public static StackTraceElement getCaller()
	{
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		String callerClassName = null;
		
		for (int i = 1; i < stElements.length; i++)
		{
			StackTraceElement ste = stElements[i];
			String className = ste.getClassName();
			
			if (!ReflectUtils.class.getName().equals(className) && !className.startsWith("java.lang.Thread"))
			{
				if (callerClassName == null)
				{
					callerClassName = className;
				}
				else if (!callerClassName.equals(className))
				{
					return ste;
				}
			}
		}
		
		return null;
	}
}

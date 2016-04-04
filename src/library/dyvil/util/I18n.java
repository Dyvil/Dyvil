package dyvil.util;

public interface I18n
{
	String getString(String key);

	default String getString(String key, Object arg)
	{
		return this.getString(key, new Object[] {arg});
	}

	default String getString(String key, Object... args)
	{
		return String.format(this.getString(key), args);
	}
}

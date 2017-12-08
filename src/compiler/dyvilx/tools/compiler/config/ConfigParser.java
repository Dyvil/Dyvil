package dyvilx.tools.compiler.config;

import dyvil.string.CharUtils;

public final class ConfigParser
{
	public static final int KEY    = 1;
	public static final int EQUALS = 2;
	public static final int VALUE  = 4;
	public static final int ARRAY  = 8;

	public static void parse(String source, CompilerConfig config)
	{
		int len = source.length();
		int mode = KEY;
		int i = 0;
		String key = null;
		while (i < len)
		{
			switch (mode)
			{
			case KEY:
			{
				for (; CharUtils.isWhitespace(source.charAt(i)); )
				{
					if (++i >= len)
					{
						return;
					}
				}
				int l = i;
				for (char c; CharUtils.isLetter(c = source.charAt(i)) || c == '_'; )
				{
					if (++i >= len)
					{
						return;
					}
				}
				key = source.substring(l, i);
				mode = EQUALS;
				continue;
			}
			case EQUALS:
				for (char c; CharUtils.isWhitespace(c = source.charAt(i)) || c == '='; )
				{
					if (++i >= len)
					{
						return;
					}
				}
				mode = VALUE;
				continue;
			case VALUE:
			{
				if (source.charAt(i) == '[')
				{
					mode = ARRAY;
					i++;
					continue;
				}
				int l = i;
				for (; !CharUtils.isWhitespace(source.charAt(i)); )
				{
					if (++i >= len)
					{
						return;
					}
				}
				if (source.charAt(l) == ']')
				{
					mode = KEY;
					continue;
				}
				config.setProperty(key, source.substring(l, i));
				mode = KEY;
				continue;
			}
			case ARRAY:
				for (char c; CharUtils.isWhitespace(c = source.charAt(i)) || c == ',' || c == '['; )
				{
					if (++i >= len)
					{
						return;
					}
				}
				if (source.charAt(i) == ']')
				{
					mode = KEY;
					continue;
				}
				int l = i;
				for (char c; !CharUtils.isWhitespace(c = source.charAt(i)) && c != ',' && c != ']'; )
				{
					if (++i >= len)
					{
						return;
					}
				}
				config.addProperty(key, source.substring(l, i));
			}
		}
	}

	public static boolean readProperty(CompilerConfig config, String arg)
	{
		final int index = arg.indexOf('=');
		if (index <= 1)
		{
			return false;
		}

		final String key;
		final String value = arg.substring(index + 1);

		if (arg.charAt(index - 1) == '+')
		{
			// key+=value
			key = arg.substring(0, index - 1);
			return config.addProperty(key, value);
		}
		else
		{
			// key=value
			key = arg.substring(0, index);
			return config.setProperty(key, value);
		}

	}
}

package dyvil.tools.compiler.config;

import java.io.File;

import dyvil.strings.CharUtils;

public final class ConfigParser
{
	public static final int	KEY		= 1;
	public static final int	EQUALS	= 2;
	public static final int	VALUE	= 4;
	public static final int	ARRAY	= 8;
	
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
				for (; CharUtils.isWhitespace(source.charAt(i));)
				{
					if (++i >= len)
					{
						return;
					}
				}
				int l = i;
				for (char c; CharUtils.isLetter(c = source.charAt(i)) || c == '_';)
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
				for (char c; CharUtils.isWhitespace(c = source.charAt(i)) || c == '=';)
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
				for (; !CharUtils.isWhitespace(source.charAt(i));)
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
				setProperty(config, key, source.substring(l, i));
				mode = KEY;
				continue;
			}
			case ARRAY:
				for (char c; CharUtils.isWhitespace(c = source.charAt(i)) || c == ',' || c == '[';)
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
				for (char c; !CharUtils.isWhitespace(c = source.charAt(i)) && c != ',' && c != ']';)
				{
					if (++i >= len)
					{
						return;
					}
				}
				setProperty(config, key, source.substring(l, i));
			}
		}
	}
	
	private static void setProperty(CompilerConfig config, String name, String value)
	{
		switch (name)
		{
		case "jar_name":
			config.jarName = value;
			return;
		case "jar_vendor":
			config.jarVendor = value;
			return;
		case "jar_version":
			config.jarVersion = value;
			return;
		case "jar_format":
			config.jarNameFormat = value;
		case "source_dir":
			config.sourceDir = new File(value);
			return;
		case "output_dir":
			config.outputDir = new File(value);
			return;
		case "main_type":
			config.mainType = value;
			return;
		case "main_args":
			config.mainArgs.add(value);
			return;
		case "include":
			config.includeFile(value);
			return;
		case "exclude":
			config.excludeFile(value);
			return;
		case "libraries":
			config.addLibraryFile(new File(value));
			return;
		}
	}
}

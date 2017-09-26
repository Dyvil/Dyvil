package dyvilx.tools.parsing.lexer;

public abstract class StringLiterals
{
	private StringLiterals()
	{
		// no instances
	}

	public static void appendStringLiteral(String value, StringBuilder buffer)
	{
		buffer.ensureCapacity(buffer.length() + value.length() + 2);
		buffer.append('"');
		appendStringLiteralBody(value, buffer);
		buffer.append('"');
	}

	public static void appendStringLiteralBody(String value, StringBuilder buffer)
	{
		int len = value.length();
		for (int i = 0; i < len; i++)
		{
			char c = value.charAt(i);
			if (c == '"')
			{
				buffer.append("\\\"");
				continue;
			}

			appendLiteralChar(c, buffer);
		}
	}

	public static void appendCharLiteral(String value, StringBuilder buffer)
	{
		buffer.ensureCapacity(buffer.length() + value.length() + 2);
		buffer.append('\'');
		appendCharLiteralBody(value, buffer);
		buffer.append('\'');
	}

	public static void appendCharLiteralBody(String value, StringBuilder buffer)
	{
		int len = value.length();
		for (int i = 0; i < len; i++)
		{
			char c = value.charAt(i);
			if (c == '\'')
			{
				buffer.append("\\'");
				continue;
			}

			appendLiteralChar(c, buffer);
		}
	}

	private static void appendLiteralChar(char c, StringBuilder buffer)
	{
		switch (c)
		{
		case '\\':
			buffer.append("\\\\");
			return;
		case '\n':
			buffer.append("\\n");
			return;
		case '\t':
			buffer.append("\\t");
			return;
		case '\r':
			buffer.append("\\r");
			return;
		case '\b':
			buffer.append("\\b");
			return;
		case '\f':
			buffer.append("\\f");
			return;
		case '\u000B':
			buffer.append("\\v");
			return;
		case '\u0007':
			buffer.append("\\a");
			return;
		case '\u001B':
			buffer.append("\\e");
			return;
		case '\0':
			buffer.append("\\0");
			return;
		}

		if (c < 256 && c >= 32)
		{
			buffer.append(c);
			return;
		}
		buffer.append("\\u{").append(Integer.toHexString(c)).append('}');
	}
}

package com.clashsoft.jcp;

import java.lang.reflect.Modifier;

public class JCPHelper
{	
	public static boolean isClass(String token)
	{
		return "class".equals(token) || "interface".equals(token) || "enum".equals(token) || "@interface".equals(token);
	}
	
	public static boolean isPrimitiveType(String token)
	{
		return "void".equals(token) || "boolean".equals(token) || "byte".equals(token) || "short".equals(token) || "char".equals(token) || //
				"int".equals(token) || "long".equals(token) || "float".equals(token) || "double".equals(token);
	}
	
	public static int parseModifier(String s)
	{
		switch (s)
		{
		case "public": return Modifier.PUBLIC;
		case "protected": return Modifier.PROTECTED;
		case "private": return Modifier.PRIVATE;
		case "abstract": return Modifier.ABSTRACT;
		case "static": return Modifier.STATIC;
		case "final": return Modifier.FINAL;
		case "transient": return Modifier.TRANSIENT;
		case "volatile": return Modifier.VOLATILE;
		case "synchronized": return Modifier.SYNCHRONIZED;
		case "native": return Modifier.NATIVE;
		case "strictfp": return Modifier.STRICT;
		}
		return 0;
	}
	
	public static final Token tokenize(String code)
	{
		int len = code.length();
		StringBuilder buf = new StringBuilder(20);
		Token first = new Token(-1, "", 0, 0);
		
		char current = 0;
		char last = 0;
		
		int index = 0;
		int i = 0;
		int j = 0;
		
		boolean quote = false;
		boolean charQuote = false;
		boolean literal = false;
		
		for (i = 0; i < len; i++)
		{
			current = code.charAt(i);
			
			if (!literal)
			{
				if (current == '"')
				{
					quote = !quote;
				}
				else if (current == '\'')
				{
					charQuote = !charQuote;
				}
				else if (current == '\\')
				{
					literal = true;
				}
				else if (!quote && !charQuote)
				{
					if (!Character.isWhitespace(current))
					{
						if (!sameType(current, last))
						{
							addToken(first, buf, index, j, i);
							index++;
							j = i;
						}
					}
				}
			}
			else
			{
				literal = false;
			}
			
			buf.append(current);
			
			last = current;
		}
		
		return first.next();
	}
	
	private static void addToken(Token token, StringBuilder buf, int index, int start, int end)
	{
		Token t = new Token(index, buf.toString(), start, end);
		token.setNext(t);
		t.setPrev(token);
		buf.delete(0, buf.length());
	}
	
	private static boolean sameType(char c1, char c2)
	{
		if (c1 == '_' || c2 == '_')
		{
			return true;
		}
		return Character.isJavaIdentifierPart(c1) == Character.isJavaIdentifierPart(c2);
	}
}

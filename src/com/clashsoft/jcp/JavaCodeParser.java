package com.clashsoft.jcp;

import clashsoft.cslib.util.CSSource;

import com.clashsoft.jcp.ast.CompilationUnit;
import com.clashsoft.jcp.parser.JCP;

public class JavaCodeParser
{
	public static String	code	= "hello world;;;;; this is a \"test  ;;; hello world\" if this 'thing' works correctly";
	
	private JavaCodeParser()
	{
	}
	
	public static void parse(String code)
	{
		code = CSSource.stripComments(code);
		code = CSSource.replaceLiterals(code);
		
		JCP jcp = new JCP();
		try
		{
			CompilationUnit cu = jcp.compilationUnit(code);
		}
		catch (SyntaxException ex)
		{
			ex.printStackTrace();
		}
	}
}

package com.clashsoft.jcp.parser;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.ClassDecl;
import com.clashsoft.jcp.parser.classbody.ClassBodyParser;

public class ClassDeclParser extends Parser
{
	public static final int	NAME		= 0;
	public static final int	SUPERCLASS	= 1;
	public static final int	INTERFACES	= 2;
	
	public ClassDecl classDecl;
	public int				mode;
	
	public ClassDeclParser(ClassDecl classDecl)
	{
		this.classDecl = classDecl;
	}
	
	@Override
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
	{
		switch (value)
		{
		case "class":
			this.mode = NAME;
			classDecl.setType(ClassDecl.CLASS);
			return;
		case "interface":
			this.mode = NAME;
			classDecl.setType(ClassDecl.INTERFACE);
			return;
		case "enum":
			this.mode = NAME;
			classDecl.setType(ClassDecl.ENUM);
			return;
		case "@interface":
			this.mode = NAME;
			classDecl.setType(ClassDecl.ANNOTATION);
			return;
		case "extends":
			this.mode = SUPERCLASS;
			return;
		case "implements":
			this.mode = INTERFACES;
			return;
		case "{":
			jcp.pushParser(new ClassBodyParser());
			return;
		}
		
		switch (this.mode)
		{
		case NAME:
			classDecl.setName(value);
		case SUPERCLASS:
			classDecl.setSuperClass(value);
		case INTERFACES:
			classDecl.addInterface(value);
		}
	}
	
	@Override
	public void end(JCP jcp)
	{
		this.classDecl.setModifiers(this.modifiers);
	}
}

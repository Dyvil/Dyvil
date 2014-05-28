package com.clashsoft.jcp.parser;

import com.clashsoft.jcp.JCPHelper;
import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.ClassDecl;
import com.clashsoft.jcp.ast.CompilationUnit;
import com.clashsoft.jcp.ast.ImportDecl;
import com.clashsoft.jcp.ast.PackageDecl;

public class CompilationUnitParser extends Parser
{
	private CompilationUnit unit;
	
	public CompilationUnitParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
	{
		int mod;
		
		if ("package".equals(value))
		{
			PackageDecl packageDecl = new PackageDecl();
			this.unit.setPackageDecl(packageDecl);
			jcp.pushParser(new PackageParser(packageDecl));
			return;
		}
		else if ("import".equals(value))
		{
			ImportDecl importDecl = new ImportDecl();
			this.unit.addImportDecl(importDecl);
			jcp.pushParser(new ImportParser(importDecl));
			return;
		}
		else if (JCPHelper.isClass(value))
		{
			ClassDecl classDecl = new ClassDecl();
			this.unit.setClassDecl(classDecl);
			jcp.pushParser(new ClassDeclParser(classDecl));
		}
		else
		{
			this.checkModifier(value);
		}
	}
}

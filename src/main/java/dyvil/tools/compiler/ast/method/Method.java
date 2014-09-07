package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.statement.IStatement;

public class Method extends Member implements IMethod
{
	private IStatement			statement;
	
	private List<Parameter>		parameters			= new ArrayList();
	private List<ThrowsDecl>	throwsDeclarations	= new ArrayList();
	
	@Override
	public void setParameters(List<Parameter> parameters)
	{
		this.parameters = parameters;
	}
	
	@Override
	public void setStatement(IStatement implementation)
	{
		this.statement = implementation;
	}
	
	@Override
	public void setThrows(List<ThrowsDecl> throwsDecls)
	{
		this.throwsDeclarations = throwsDecls;
	}
	
	@Override
	public IStatement getStatement()
	{
		return this.statement;
	}
	
	@Override
	public List<Parameter> getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public List<ThrowsDecl> getThrows()
	{
		return this.throwsDeclarations;
	}
}

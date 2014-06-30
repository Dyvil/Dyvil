package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.api.IImplementable;
import dyvil.tools.compiler.ast.api.IParameterized;
import dyvil.tools.compiler.ast.api.IThrower;
import dyvil.tools.compiler.ast.statement.IStatement;

public class Method extends Member implements IThrower, IParameterized, IImplementable
{
	private IStatement				statement;
	
	private Map<String, Parameter>	parameters			= new HashMap();
	private List<ThrowsDecl>		throwsDeclarations	= new ArrayList();
	
	@Override
	public void setParameters(Map<String, Parameter> parameters)
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
	public Map<String, Parameter> getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public List<ThrowsDecl> getThrows()
	{
		return this.throwsDeclarations;
	}
}

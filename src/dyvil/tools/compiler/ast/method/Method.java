package dyvil.tools.compiler.ast.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.api.IImplementable;
import dyvil.tools.compiler.ast.api.IParameterized;
import dyvil.tools.compiler.ast.api.IThrower;
import dyvil.tools.compiler.ast.codeblock.CodeBlock;

public class Method extends Member implements IThrower, IParameterized, IImplementable
{
	private CodeBlock				implementation;
	
	private Map<String, Parameter>	parameters			= new HashMap();
	private List<ThrowsDecl>		throwsDeclarations	= new ArrayList();
	
	@Override
	public void setParameters(Map<String, Parameter> parameters)
	{
		this.parameters = parameters;
	}
	
	@Override
	public void setImplementation(CodeBlock implementation)
	{
		this.implementation = implementation;
	}
	
	@Override
	public void setThrows(List<ThrowsDecl> throwsDecls)
	{
		this.throwsDeclarations = throwsDecls;
	}
	
	@Override
	public CodeBlock getImplementation()
	{
		return this.implementation;
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

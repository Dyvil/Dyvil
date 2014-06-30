package dyvil.tools.compiler.ast.statement;

import java.util.List;

import dyvil.tools.compiler.ast.field.Variable;

public class ForStatement extends WhileStatement
{
	private List<Variable>	variables;
	private IStatement		action;
	
	public ForStatement()
	{
	}
	
	public void setVariables(List<Variable> variables)
	{
		this.variables = variables;
	}
	
	public void setAction(IStatement action)
	{
		this.action = action;
	}
	
	public List<Variable> getVariables()
	{
		return this.variables;
	}
	
	public void addVariable(Variable variable)
	{
		this.variables.add(variable);
	}
	
	public IStatement getAction()
	{
		return this.action;
	}
}

package dyvil.tools.compiler.ast.codeblock;

import java.util.ArrayList;
import java.util.List;

public class CodeBlock
{
	private List<IStatement> statements = new ArrayList();
	
	public void addStatement(IStatement statement)
	{
		this.statements.add(statement);
	}
	
	public List<IStatement> getStatements()
	{
		return this.statements;
	}
}

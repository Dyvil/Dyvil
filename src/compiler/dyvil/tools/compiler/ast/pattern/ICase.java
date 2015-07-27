package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.compiler.ast.consumer.IPatternConsumer;
import dyvil.tools.compiler.ast.expression.IValue;

public interface ICase extends IPatternConsumer
{
	public IPattern getPattern();
	
	@Override
	public void setPattern(IPattern pattern);
	
	public IValue getCondition();
	
	public void setCondition(IValue condition);
	
	public IValue getAction();
	
	public void setAction(IValue action);
}

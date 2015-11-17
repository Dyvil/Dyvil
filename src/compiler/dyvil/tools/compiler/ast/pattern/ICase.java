package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.compiler.ast.consumer.IPatternConsumer;
import dyvil.tools.compiler.ast.expression.IValue;

public interface ICase extends IPatternConsumer
{
	IPattern getPattern();
	
	@Override
	void setPattern(IPattern pattern);
	
	IValue getCondition();
	
	void setCondition(IValue condition);
	
	IValue getAction();
	
	void setAction(IValue action);
}

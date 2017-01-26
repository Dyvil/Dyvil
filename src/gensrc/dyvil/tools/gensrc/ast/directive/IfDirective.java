package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.Scope;
import dyvil.tools.gensrc.ast.Util;

import java.io.PrintStream;

public class IfDirective implements Directive
{
	public static final byte MODE_IF = 0;
	public static final byte MODE_IFDEF = 1;
	public static final byte MODE_IFNDEF = 2;

	private final byte mode;

	private final String condition;
	private Directive thenBlock;
	private Directive elseBlock;

	public IfDirective(String condition, byte mode)
	{
		this.condition = condition;
		this.mode = mode;
	}

	public Directive getThenBlock()
	{
		return this.thenBlock;
	}

	public void setThenBlock(Directive thenBlock)
	{
		this.thenBlock = thenBlock;
	}

	public Directive getElseBlock()
	{
		return this.elseBlock;
	}

	public void setElseBlock(Directive elseBlock)
	{
		this.elseBlock = elseBlock;
	}

	private boolean evaluate(Scope replacements)
	{
		final String processed = Util.processLine(this.condition, replacements);
		switch (this.mode)
		{
		case MODE_IF:
			return replacements.getBoolean(processed);
		case MODE_IFDEF:
			return replacements.isDefined(processed);
		case MODE_IFNDEF:
			return !replacements.isDefined(processed);
		}
		return false;
	}

	@Override
	public void specialize(GenSrc gensrc, Scope scope, PrintStream output)
	{
		if (this.evaluate(scope))
		{
			this.thenBlock.specialize(gensrc, scope, output);
		}
		else if (this.elseBlock != null)
		{
			this.elseBlock.specialize(gensrc, scope, output);
		}
	}
}

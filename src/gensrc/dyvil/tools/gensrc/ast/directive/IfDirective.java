package dyvil.tools.gensrc.ast.directive;

import dyvil.tools.gensrc.GenSrc;
import dyvil.tools.gensrc.ast.scope.Scope;

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
		switch (this.mode)
		{
		case MODE_IF:
			return replacements.getBoolean(this.condition);
		case MODE_IFDEF:
			return replacements.isDefined(this.condition);
		case MODE_IFNDEF:
			return !replacements.isDefined(this.condition);
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

	@Override
	public String toString()
	{
		return Directive.toString(this);
	}

	@Override
	public void toString(String indent, StringBuilder builder)
	{
		builder.append(indent);

		switch (this.mode)
		{
		case MODE_IF:
			builder.append("#if ");
			break;
		case MODE_IFDEF:
			builder.append("#ifdef ");
			break;
		case MODE_IFNDEF:
			builder.append("#ifndef ");
			break;
		}

		builder.append(this.condition).append('\n');

		final String newIndent = indent + '\t';
		this.thenBlock.toString(newIndent, builder);

		if (this.elseBlock != null)
		{
			builder.append(indent).append("#else\n");
			this.elseBlock.toString(newIndent, builder);
		}

		builder.append(indent).append("#end\n");
	}
}

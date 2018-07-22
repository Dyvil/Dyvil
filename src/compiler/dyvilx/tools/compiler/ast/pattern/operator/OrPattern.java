package dyvilx.tools.compiler.ast.pattern.operator;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class OrPattern extends BinaryPattern
{
	public OrPattern(Pattern left, SourcePosition token, Pattern right)
	{
		super(left, token, right);
	}

	@Override
	public int getPatternType()
	{
		return OR;
	}

	@Override
	public boolean isExhaustive()
	{
		return this.left.isExhaustive() || this.right.isExhaustive();
	}

	@Override
	protected Pattern withType()
	{
		if (this.left.isWildcard())
		{
			return this.left;
		}
		if (this.right.isWildcard())
		{
			return this.right;
		}
		return this;
	}

	@Override
	public boolean isSwitchable()
	{
		return this.left.isSwitchable() && this.right.isSwitchable();
	}

	@Override
	public int subPatterns()
	{
		return this.left.subPatterns() + this.right.subPatterns();
	}

	@Override
	public boolean switchCheck()
	{
		return this.left.switchCheck() || this.right.switchCheck();
	}

	@Override
	public int minValue()
	{
		return Math.min(this.left.minValue(), this.left.minValue());
	}

	@Override
	public int maxValue()
	{
		return Math.max(this.left.maxValue(), this.right.maxValue());
	}

	@Override
	public Pattern subPattern(int index)
	{
		final int leftCount = this.left.subPatterns();
		if (index < leftCount)
		{
			return this.left.subPattern(index);
		}
		return this.right.subPattern(index - leftCount);
	}

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		final int locals = writer.localCount();

		varIndex = Pattern.ensureVar(writer, varIndex);

		final Label targetLabel = new Label();

		this.left.writeJumpOnMatch(writer, varIndex, targetLabel);

		writer.resetLocals(locals);

		this.right.writeJumpOnMismatch(writer, varIndex, target);

		writer.visitLabel(targetLabel);

		writer.resetLocals(locals);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" | ");
		this.right.toString(prefix, buffer);
	}
}

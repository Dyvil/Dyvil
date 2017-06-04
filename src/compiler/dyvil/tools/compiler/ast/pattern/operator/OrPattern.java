package dyvil.tools.compiler.ast.pattern.operator;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.source.position.SourcePosition;

public class OrPattern extends BinaryPattern
{
	public OrPattern(IPattern left, SourcePosition token, IPattern right)
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
	protected IPattern withType()
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
	public IPattern subPattern(int index)
	{
		final int leftCount = this.left.subPatterns();
		if (index < leftCount)
		{
			return this.left.subPattern(index);
		}
		return this.right.subPattern(index - leftCount);
	}

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
		throws BytecodeException
	{
		final int locals = writer.localCount();

		varIndex = IPattern.ensureVar(writer, varIndex, matchedType);

		final Label targetLabel = new Label();

		this.left.writeJump(writer, varIndex, matchedType, targetLabel);

		writer.resetLocals(locals);

		this.right.writeInvJump(writer, varIndex, matchedType, elseLabel);

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

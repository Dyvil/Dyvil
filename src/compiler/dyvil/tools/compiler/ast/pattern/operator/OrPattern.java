package dyvil.tools.compiler.ast.pattern.operator;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class OrPattern extends BinaryPattern
{
	public OrPattern(IPattern left, ICodePosition token, IPattern right)
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
	public IPattern resolve(MarkerList markers, IContext context)
	{
		super.resolveChildren(markers, context);

		if (this.left.isExhaustive())
		{
			return this.left;
		}
		if (this.right.isExhaustive())
		{
			return this.right;
		}
		return this;
	}

	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		this.left = this.left.withType(type, markers);
		this.right = this.right.withType(type, markers);
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
		varIndex = IPattern.ensureVar(writer, varIndex, matchedType);

		final Label targetLabel = new Label();

		this.left.writeJump(writer, varIndex, matchedType, targetLabel);
		this.right.writeInvJump(writer, varIndex, matchedType, elseLabel);

		writer.writeLabel(targetLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" | ");
		this.right.toString(prefix, buffer);
	}
}

package dyvilx.tools.compiler.ast.pattern.operator;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public class AndPattern extends BinaryPattern implements Pattern
{
	public AndPattern(Pattern left, SourcePosition position, Pattern right)
	{
		super(left, position, right);
	}

	@Override
	public int getPatternType()
	{
		return AND;
	}

	@Override
	public boolean isExhaustive()
	{
		return this.left.isExhaustive() && this.right.isExhaustive();
	}

	@Override
	protected Pattern withType()
	{
		if (this.left.isWildcard())
		{
			return this.right;
		}
		if (this.right.isWildcard())
		{
			return this.left;
		}
		return this;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		final IDataMember field = this.left.resolveField(name);
		if (field != null)
		{
			return field;
		}
		return this.right.resolveField(name);
	}

	@Override
	public boolean isSwitchable()
	{
		return false;
	}

	@Override
	public int subPatterns()
	{
		return -1;
	}

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		varIndex = Pattern.ensureVar(writer, varIndex);

		this.left.writeJumpOnMismatch(writer, varIndex, target);
		this.right.writeJumpOnMismatch(writer, varIndex, target);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" & ");
		this.right.toString(prefix, buffer);
	}
}

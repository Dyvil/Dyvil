package dyvilx.tools.compiler.ast.pattern.operator;

import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.pattern.IPattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;

public class AndPattern extends BinaryPattern implements IPattern
{
	public AndPattern(IPattern left, SourcePosition position, IPattern right)
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
	protected IPattern withType()
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
	public boolean switchCheck()
	{
		return false;
	}

	@Override
	public int switchValue()
	{
		return 0;
	}

	@Override
	public int minValue()
	{
		return 0;
	}

	@Override
	public int maxValue()
	{
		return 0;
	}

	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
		throws BytecodeException
	{
		varIndex = IPattern.ensureVar(writer, varIndex, matchedType);

		this.left.writeInvJump(writer, varIndex, matchedType, elseLabel);
		this.right.writeInvJump(writer, varIndex, matchedType, elseLabel);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" & ");
		this.right.toString(prefix, buffer);
	}
}

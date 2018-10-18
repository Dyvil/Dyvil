package dyvilx.tools.compiler.ast.pattern.operator;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

import java.util.function.Consumer;

public class OrPattern extends BinaryPattern
{
	// =============== Constructors ===============

	public OrPattern(Pattern left, SourcePosition token, Pattern right)
	{
		super(left, token, right);
	}

	// =============== Properties ===============

	// --------------- General ---------------

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

	// --------------- Switch Hash ---------------

	@Override
	public boolean hasSwitchHash()
	{
		return this.left.hasSwitchHash() && this.right.hasSwitchHash();
	}

	@Override
	public boolean isSwitchHashInjective()
	{
		return this.left.isSwitchHashInjective() && this.right.isSwitchHashInjective();
	}

	// =============== Methods ===============

	@Override
	public void forEachAtom(Consumer<Pattern> action)
	{
		this.left.forEachAtom(action);
		this.right.forEachAtom(action);
	}

	// --------------- Type ---------------

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

	// --------------- Compilation ---------------

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

	// --------------- Formatting ---------------

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" | ");
		this.right.toString(prefix, buffer);
	}
}

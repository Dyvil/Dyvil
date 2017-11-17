package dyvilx.tools.compiler.ast.pattern;

import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class TypeCheckPattern implements Pattern
{
	private Pattern pattern;
	private IType   type;

	// Metadata
	private IType          fromType;
	private SourcePosition position;

	public TypeCheckPattern(SourcePosition position, Pattern pattern)
	{
		this.position = position;
		this.pattern = pattern;
	}

	public TypeCheckPattern(Pattern pattern, IType fromType, IType toType)
	{
		this.pattern = pattern;
		this.fromType = fromType;
		this.type = toType;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public int getPatternType()
	{
		return TYPECHECK;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public boolean isType(IType type)
	{
		return !type.isPrimitive();
	}

	@Override
	public Pattern withType(IType type, MarkerList markers)
	{
		if (Types.isSuperType(type, this.type))
		{
			this.fromType = type;
			return this;
		}
		return null;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.pattern == null ? null : this.pattern.resolveField(name);
	}

	@Override
	public Pattern resolve(MarkerList markers, IContext context)
	{
		if (this.pattern != null)
		{
			this.pattern = this.pattern.resolve(markers, context);
		}

		if (this.type != null)
		{
			this.type = this.type.resolveType(markers, context);

			if (this.pattern != null && this.pattern.getPatternType() != WILDCARD)
			{
				this.pattern = this.pattern.withType(this.type, markers);
			}
		}
		else
		{
			markers.add(Markers.semantic(this.position, "pattern.typecheck.invalid"));
		}

		return this;
	}

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		if (this.fromType.isPrimitive())
		{
			if (this.pattern.getPatternType() == WILDCARD)
			{
				return;
			}

			Pattern.loadVar(writer, varIndex);
		}
		else
		{
			varIndex = Pattern.ensureVar(writer, varIndex);

			writer.visitVarInsn(Opcodes.ALOAD, varIndex);
			writer.visitTypeInsn(Opcodes.INSTANCEOF, this.type.getInternalName());
			writer.visitJumpInsn(Opcodes.IFEQ, target);

			if (this.pattern.getPatternType() == WILDCARD)
			{
				return;
			}

			writer.visitVarInsn(Opcodes.ALOAD, varIndex);
		}

		this.fromType.writeCast(writer, this.type, this.lineNumber());
		this.pattern.writeJumpOnMismatch(writer, -1, target);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.pattern != null)
		{
			this.pattern.toString(prefix, buffer);
			int patternType = this.pattern.getPatternType();

			if (patternType == BINDING || patternType != CASE_CLASS && !this.pattern.isType(this.type))
			{
				buffer.append(" as ");
				this.type.toString(prefix, buffer);
			}
		}
	}
}

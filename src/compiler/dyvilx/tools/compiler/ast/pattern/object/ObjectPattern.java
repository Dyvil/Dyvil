package dyvilx.tools.compiler.ast.pattern.object;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.compiler.ast.pattern.AbstractPattern;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.raw.NamedType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class ObjectPattern extends AbstractPattern implements Pattern
{
	protected IType type;

	// Metadata
	private IDataMember instanceField;

	public ObjectPattern(SourcePosition position, IType type)
	{
		this.type = type;
		this.position = position;
	}

	@Override
	public int getPatternType()
	{
		return OBJECT;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public Pattern withType(IType type, MarkerList markers)
	{
		if (this.isType(type))
		{
			return this;
		}
		// Type Check Patterns are not required
		return null;
	}

	@Override
	public Object constantValue()
	{
		return new ObjectSurrogate(this.type.getInternalName());
	}

	@Override
	public Pattern resolve(MarkerList markers, IContext context)
	{
		if (this.type.typeTag() == IType.NAMED)
		{
			NamedType namedType = (NamedType) this.type;

			final Name name = namedType.getName();
			IType parent = namedType.getParent();
			if (parent != null)
			{
				parent = parent.resolveType(markers, context);
				namedType.setParent(parent);

				IDataMember dataMember = parent.resolveField(name);
				if (dataMember != null)
				{
					return new FieldPattern(this.position, dataMember).resolve(markers, context);
				}
			}
			else
			{
				IDataMember dataMember = context.resolveField(name);
				if (dataMember != null)
				{
					return new FieldPattern(this.position, dataMember).resolve(markers, context);
				}
			}
		}

		this.type = this.type.resolveType(markers, context);

		final IClass theClass = this.type.getTheClass();
		if (theClass == null)
		{
			return this;
		}

		if (!theClass.hasModifier(Modifiers.OBJECT_CLASS))
		{
			markers.add(Markers.semanticError(this.position, "pattern.object", theClass.getName()));
			return this;
		}

		this.instanceField = theClass.getMetadata().getInstanceField();
		return this;
	}

	@Override
	public void writeJumpOnMismatch(MethodWriter writer, int varIndex, Label target) throws BytecodeException
	{
		Pattern.loadVar(writer, varIndex);
		// No need to cast - Reference Equality Comparison (ACMP) handles it
		this.instanceField.writeGet(writer, null, this.lineNumber());
		writer.visitJumpInsn(Opcodes.IF_ACMPNE, target);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
	}
}

class ObjectSurrogate
{
	private final String type;

	public ObjectSurrogate(String type)
	{
		this.type = type;
	}

	@Override
	public boolean equals(Object o)
	{
		return this == o || o != null && this.getClass() == o.getClass() //
		                    && this.type.equals(((ObjectSurrogate) o).type);
	}

	@Override
	public int hashCode()
	{
		return this.type.hashCode();
	}

	@Override
	public String toString()
	{
		return "object " + this.type;
	}
}

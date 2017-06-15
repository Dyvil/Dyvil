package dyvil.tools.compiler.ast.expression.constant;

import dyvil.annotation.internal.NonNull;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.access.FieldAccess;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.typevar.CovariantTypeVarType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class EnumValue extends FieldAccess
{
	public EnumValue()
	{
	}

	public EnumValue(SourcePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}

	public EnumValue(IType type, Name name)
	{
		this.type = type;
		this.name = name;
	}

	public EnumValue(SourcePosition position, IType type, Name name)
	{
		this.position = position;
		this.type = type;
		this.name = name;
	}

	@Override
	public int valueTag()
	{
		return ENUM_ACCESS;
	}

	@Override
	public boolean isConstantOrField()
	{
		return true;
	}

	@Override
	public boolean isAnnotationConstant()
	{
		return true;
	}

	@Override
	public IValue toAnnotationConstant(MarkerList markers, IContext context, int depth)
	{
		return this;
	}

	@Override
	public boolean isPolyExpression()
	{
		return true;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		return type.resolveField(this.name) != null || type.canExtract(CovariantTypeVarType.class);
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		return this.isType(type) ? IValue.EXACT_MATCH : IValue.MISMATCH;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IDataMember field = type.resolveField(this.name);
		if (field == null)
		{
			markers.add(Markers.semanticError(this.position, "resolve.field", this.name));
			return null;
		}

		this.field = field;
		return super.withType(type, typeContext, markers, context);
	}

	public String getInternalName()
	{
		return this.name.qualified;
	}

	@Override
	public Object toObject()
	{
		return null;
	}

	@Override
	public int stringSize()
	{
		return this.getInternalName().length();
	}

	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		return false;
	}

	@Override
	public void writeAnnotationValue(AnnotationVisitor visitor, String key)
	{
		visitor.visitEnum(key, this.getType().getExtendedName(), this.getInternalName());
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append('.').append(this.name);
	}
}

package dyvil.tools.compiler.ast.field;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.TupleExpr;
import dyvil.tools.compiler.ast.expression.access.ConstructorCall;
import dyvil.tools.compiler.ast.expression.constant.IntValue;
import dyvil.tools.compiler.ast.expression.constant.StringValue;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.util.Markers;
import dyvil.lang.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

public class EnumConstant extends Field
{
	private int index;

	public EnumConstant(IClass enclosingClass, Name name)
	{
		super(enclosingClass, name);
		this.modifiers.addIntModifier(Modifiers.ENUM_CONST);
	}

	public EnumConstant(@NonNull SourcePosition position, Name name, ModifierSet modifiers, AnnotationList annotations)
	{
		this(null, position, name, modifiers, annotations);
	}

	public EnumConstant(IClass enclosingClass, SourcePosition position, Name name, ModifierSet modifiers,
		                   AnnotationList annotations)
	{
		super(enclosingClass, position, name, Types.UNKNOWN, modifiers, annotations);
		this.modifiers.addIntModifier(Modifiers.ENUM_CONST);
	}

	public int getIndex()
	{
		return this.index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.enclosingClass.getClassType();

		super.resolveTypes(markers, context);
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		final ArgumentList arguments = new ArgumentList(new StringValue(this.name.unqualified), new IntValue(this.index));

		if (this.value != null)
		{
			if (this.value.valueTag() == IValue.TUPLE)
			{
				arguments.addAll(((TupleExpr) this.value).getValues());
			}
			else
			{
				arguments.add(this.value);
			}
		}

		this.value = new ConstructorCall(this.position, this.enclosingClass.getClassType(), arguments);

		super.resolve(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		final IClass enclosingClass = this.getEnclosingClass();
		final IType classType = enclosingClass.getClassType();
		if (!enclosingClass.hasModifier(Modifiers.ENUM_CLASS))
		{
			final Marker marker = Markers.semanticError(this.position, "field.enum.class", this.name);
			marker.addInfo(Markers.getSemantic("method.enclosing_class", classType));
			markers.add(marker);
		}

		super.check(markers, context);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.attributesToString(indent, buffer);
		buffer.append("case ").append(this.name);
		this.valueToString(indent, buffer);
	}
}

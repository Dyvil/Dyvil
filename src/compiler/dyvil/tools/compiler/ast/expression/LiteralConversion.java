package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

public final class LiteralConversion extends MethodCall
{
	private IValue literal;

	public LiteralConversion(IValue literal, IAnnotation annotation)
	{
		this(literal, annotation, new SingleArgument(literal));
	}

	public LiteralConversion(IValue literal, IAnnotation annotation, IArguments arguments)
	{
		super(literal.getPosition(), null, getMethodName(annotation), arguments);
		this.literal = literal;
	}

	public LiteralConversion(IValue literal, IMethod method)
	{
		this(literal, method, new SingleArgument(literal));
	}

	public LiteralConversion(IValue literal, IMethod method, IArguments arguments)
	{
		super(literal.getPosition(), null, method, arguments);
		this.literal = literal;
	}

	public static Name getMethodName(IAnnotation annotation)
	{
		IValue v = annotation.getArguments().getFirstValue();
		if (v != null)
		{
			return Name.get(v.stringValue());
		}
		return Names.apply;
	}

	@Override
	public int valueTag()
	{
		return LITERAL_CONVERSION;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.method == null)
		{
			this.method = IContext.resolveMethod(type, null, this.name, this.arguments);
			if (this.method == null)
			{
				StringBuilder builder = new StringBuilder();
				this.arguments.typesToString(builder);
				markers.add(Markers.semantic(this.literal.getPosition(), "literal.method", this.literal.getType(), type,
				                             builder));
				this.type = type;
				return this;
			}
		}

		this.checkArguments(markers, context);

		final IType thisType = this.getType();
		if (!Types.isSuperType(type, thisType))
		{
			final Marker marker = Markers.semantic(this.literal.getPosition(), "literal.type.incompatible");
			marker.addInfo(Markers.getSemantic("type.expected", type));
			marker.addInfo(Markers.getSemantic("literal.type.conversion", thisType));

			final StringBuilder stringBuilder = new StringBuilder();
			Util.methodSignatureToString(this.method, typeContext, stringBuilder);
			marker.addInfo(Markers.getSemantic("literal.type.method", stringBuilder.toString()));

			markers.add(marker);
		}

		return this;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.literal.toString(prefix, buffer);
	}
}

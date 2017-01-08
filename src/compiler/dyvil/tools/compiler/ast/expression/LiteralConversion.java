package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.access.AbstractCall;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.ImplicitNullableType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class LiteralConversion extends AbstractCall
{
	protected IValue literal;
	protected Name   name;

	public LiteralConversion(ICodePosition position)
	{
		this.position = position;
	}

	public LiteralConversion(IValue literal, IAnnotation annotation)
	{
		this(literal, annotation, new SingleArgument(literal));
	}

	public LiteralConversion(IValue literal, IAnnotation annotation, IArguments arguments)
	{
		this.position = literal.getPosition();
		this.literal = literal;
		this.name = getMethodName(annotation);
		this.arguments = arguments;
	}

	public LiteralConversion(IValue literal, IMethod method)
	{
		this(literal, method, new SingleArgument(literal));
	}

	public LiteralConversion(IValue literal, IMethod method, IArguments arguments)
	{
		this.position = literal.getPosition();
		this.literal = literal;
		this.arguments = arguments;

		if (method != null)
		{
			this.method = method;
			this.name = method.getName();
		}
	}

	public static Name getMethodName(IAnnotation annotation)
	{
		final IValue value = annotation.getArguments().getFirstValue();
		if (value != null)
		{
			return Name.from(value.stringValue());
		}
		return Names.apply;
	}

	@Override
	public int valueTag()
	{
		return LITERAL_CONVERSION;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	protected Name getReferenceName()
	{
		return this.name;
	}

	public IValue getLiteral()
	{
		return this.literal;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.method == null)
		{
			final MatchList<IMethod> candidates = IContext.resolveMethods(type, null, this.name, this.arguments);
			if (candidates.isEmpty())
			{
				this.type = type;
				this.reportResolve(markers, candidates);
				return this;
			}
			else if (candidates.isAmbigous())
			{
				this.type = type;
				super.reportResolve(markers, candidates);
				return this;
			}

			this.method = candidates.getBestMember();
			this.checkArguments(markers, context);
		}

		final IType thisType = this.getType();
		if (Types.isSuperType(type, thisType))
		{
			return this;
		}

		// T! -> T, if necessary
		final IValue value = thisType.convertValueTo(this, type, typeContext, markers, context);
		if (value != null)
		{
			return value;
		}

		final Marker marker = Markers.semanticError(this.position, "literal.type.incompatible");
		marker.addInfo(Markers.getSemantic("type.expected", type));
		marker.addInfo(Markers.getSemantic("literal.type.conversion", thisType));

		marker.addInfo(
			Markers.getSemantic("literal.type.method", Util.methodSignatureToString(this.method, typeContext)));

		markers.add(marker);

		return this;
	}

	@Override
	protected void reportResolve(MarkerList markers, MatchList<IMethod> matches)
	{
		markers.add(Markers.semanticError(this.position, "literal.method", this.literal.getType(), this.type, this.name,
		                                  this.arguments.typesToString()));
	}

	@Override
	public void writeNullCheckedExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.method == ImplicitNullableType.LazyTypes.UNWRAP)
		{
			this.literal.writeExpression(writer, type);
		}
		else
		{
			this.writeExpression(writer, type);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.literal.toString(prefix, buffer);
	}
}

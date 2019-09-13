package dyvilx.tools.compiler.ast.expression;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.access.AbstractCall;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public class LiteralConversion extends AbstractCall
{
	// =============== Fields ===============

	protected IValue literal;
	protected Name   name;

	// =============== Constructors ===============

	// --------------- With Annotation ---------------

	public LiteralConversion(IValue literal, Annotation annotation)
	{
		this(literal, annotation, new ArgumentList(literal));
	}

	public LiteralConversion(IValue literal, Annotation annotation, ArgumentList arguments)
	{
		this(literal, methodNameAndArgumentLabels(annotation), arguments);
	}

	private static Name[] methodNameAndArgumentLabels(Annotation annotation)
	{
		final IValue value = annotation.getArguments().getFirst();
		final String method = value != null ? value.stringValue() : null;
		if (method == null)
		{
			return new Name[] { Names.apply };
		}

		int index = method.indexOf('(');
		if (index < 0)
		{
			return new Name[] { Name.apply(method) };
		}

		final int length = method.length();

		int count = 0;
		for (int i = index; i < length; i++)
		{
			if (method.charAt(i) == ':')
			{
				count++;
			}
		}

		final Name[] result = new Name[count + 1];
		int resultIndex = 1;

		result[0] = Name.apply(method.substring(0, index));
		index++;

		while (index < length && method.charAt(index) != ')')
		{
			final int end = method.indexOf(':', index);
			if (end < 0)
			{
				break;
			}
			result[resultIndex++] = Name.apply(method.substring(index, end));
			index = end + 1;
		}

		return result;
	}

	private LiteralConversion(IValue literal, Name[] methodNameAndArgumentLabels, ArgumentList arguments)
	{
		this(literal, methodNameAndArgumentLabels[0], withArgumentLabels(arguments, methodNameAndArgumentLabels));
	}

	private static ArgumentList withArgumentLabels(ArgumentList arguments, Name[] methodNameAndArgumentLabels)
	{
		for (int i = 1; i < methodNameAndArgumentLabels.length; i++)
		{
			arguments.setLabel(i - 1, methodNameAndArgumentLabels[i]);
		}
		return arguments;
	}

	// --------------- With Name ---------------

	public LiteralConversion(IValue literal, Name name, ArgumentList arguments)
	{
		this.position = literal.getPosition();
		this.literal = literal;
		this.name = name;
		this.arguments = arguments;
	}

	// --------------- With Method ---------------

	public LiteralConversion(IValue literal, IMethod method)
	{
		this(literal, method, new ArgumentList(literal));
	}

	public LiteralConversion(IValue literal, IMethod method, ArgumentList arguments)
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

	// =============== Static Methods ===============

	public static IValue converting(IValue literal, IType type, IMethod method, MarkerList markers, IContext context,
		ITypeContext typeContext)
	{
		return new LiteralConversion(literal, method).checkArguments(markers, context, method)
		                                             .withType(type, typeContext, markers, context);
	}

	// =============== Properties ===============

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
		return null;
	}

	public IValue getLiteral()
	{
		return this.literal;
	}

	public void setLiteral(IValue literal)
	{
		this.literal = literal;
	}

	// =============== Methods ===============

	// --------------- Typing ---------------

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

			return this.checkArguments(markers, context, candidates.getBestMember())
			           .withType(type, typeContext, markers, context); // will probably recurse
		}

		final IType thisType = this.getType();
		if (Types.isSuperType(type, thisType))
		{
			return this;
		}

		// T! -> T, if necessary
		final IValue value = thisType.convertTo(this, type, typeContext, markers, context);
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

	// --------------- Resolution Phase ---------------

	@Override
	protected void reportResolve(MarkerList markers, MatchList<IMethod> matches)
	{
		final StringBuilder name = new StringBuilder(this.name.toString());
		this.arguments.typesToString(name);

		markers.add(Markers.semanticError(this.position, "literal.method", this.literal.getType(), this.type, name));
	}

	// --------------- Formatting ---------------

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.literal.toString(prefix, buffer);
	}
}

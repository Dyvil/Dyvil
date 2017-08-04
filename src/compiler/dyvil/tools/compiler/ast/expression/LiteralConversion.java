package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.access.AbstractCall;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.NamedArgumentList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.lang.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class LiteralConversion extends AbstractCall
{
	protected IValue literal;
	protected Name   name;

	public LiteralConversion(SourcePosition position)
	{
		this.position = position;
	}

	public LiteralConversion(IValue literal, IAnnotation annotation)
	{
		this(literal, annotation, new ArgumentList(literal));
	}

	public LiteralConversion(IValue literal, IAnnotation annotation, ArgumentList arguments)
	{
		this.position = literal.getPosition();
		this.literal = literal;

		Name[] names = parseAnnotation(annotation);
		this.name = names[0];
		if (names.length <= 1)
		{
			this.arguments = arguments;
			return;
		}

		final NamedArgumentList namedArguments = arguments.toNamed();
		for (int i = 1; i < names.length; i++)
		{
			namedArguments.setName(i - 1, names[i]);
		}
		this.arguments = namedArguments;
	}

	public LiteralConversion(IValue literal, Name name, ArgumentList arguments)
	{
		this.literal = literal;
		this.name = name;
		this.arguments = arguments;
	}

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

	public static Name[] parseAnnotation(IAnnotation annotation)
	{
		final String method = getMethod(annotation);
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

	public static String getMethod(IAnnotation annotation)
	{
		final IValue value = annotation.getArguments().getFirst();
		if (value == null)
		{
			return null;
		}
		return value.stringValue();
	}

	public static Name getMethodName(IAnnotation annotation)
	{
		final String method = getMethod(annotation);
		if (method == null)
		{
			return Names.apply;
		}

		final int index = method.indexOf('(');
		final String name = index < 0 ? method : method.substring(0, index);
		return Name.apply(name);
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
		return null;
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

			return this.checkArguments(markers, context, candidates.getBestMember())
			           .withType(type, typeContext, markers, context); // will probably recurse
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
		final StringBuilder name = new StringBuilder(this.name.toString());
		this.arguments.typesToString(name);

		markers.add(Markers.semanticError(this.position, "literal.method", this.literal.getType(), this.type, name));
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.literal.toString(prefix, buffer);
	}
}

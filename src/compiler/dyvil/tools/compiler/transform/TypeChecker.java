package dyvil.tools.compiler.transform;

import dyvil.array.ObjectArray;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralConversion;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class TypeChecker
{
	public interface MarkerSupplier
	{
		Marker createMarker(ICodePosition position, IType expected, IType actual);
	}

	private TypeChecker()
	{
		// no instances
	}

	public static MarkerSupplier markerSupplier(String error)
	{
		return markerSupplier(error, ObjectArray.EMPTY);
	}

	public static MarkerSupplier markerSupplier(String error, String expectedError, String actualError)
	{
		return (position, expectedType, actualType) -> typeError(position, expectedType, actualType, error,
		                                                         expectedError, actualError);
	}

	public static MarkerSupplier markerSupplier(String error, String expectedError, String actualError, Object arg)
	{
		return (position, expectedType, actualType) -> typeError(position, expectedType, actualType, error,
		                                                         expectedError, actualError, arg);
	}

	public static MarkerSupplier markerSupplier(String error, String expectedError, String actualError, Object... args)
	{
		return (position, expectedType, actualType) -> typeError(position, expectedType, actualType, error,
		                                                         expectedError, actualError, args);
	}

	public static MarkerSupplier markerSupplier(String error, Object arg)
	{
		return (position, expected, actual) -> typeError(position, expected, actual, error, arg);
	}

	public static MarkerSupplier markerSupplier(String error, Object... args)
	{
		return (position, expected, actual) -> typeError(position, expected, actual, error, args);
	}

	public static double getTypeMatch(IValue value, IType type, IImplicitContext context)
	{
		final int direct = value.getTypeMatch(type);
		if (direct > 0)
		{
			return direct;
		}

		final MatchList<IMethod> list = new MatchList<>(null);
		context.getImplicitMatches(list, value, type);
		if (list.isEmpty())
		{
			// No implicit conversions available
			return 0;
		}

		return list.getBestCandidate().getValue(0) + IValue.CONVERSION_MATCH;
	}

	private static IValue convertValueDirect(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IValue typedValue = type.convertValue(value, typeContext, markers, context);
		if (typedValue != null)
		{
			return typedValue;
		}

		final IValue convertedValue = value.getType().convertValueTo(value, type, typeContext, markers, context);
		if (convertedValue != null)
		{
			return convertedValue;
		}

		MatchList<IMethod> list = new MatchList<>(null);
		context.getImplicitMatches(list, value, type);
		if (list.isEmpty())
		{
			return null;
		}

		// TODO Ambiguity check
		return new LiteralConversion(value, list.getBestMember());
	}

	public static IValue convertValue(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type.hasTypeVariables())
		{
			type = type.getConcreteType(typeContext);
		}

		return convertValueDirect(value, type, typeContext, markers, context);
	}

	public static IValue convertValue(IValue value, IType type, ITypeContext typeContext, MarkerList markers, IContext context, MarkerSupplier markerSupplier)
	{
		final IType concreteType = type.getConcreteType(typeContext);
		final IValue newValue = convertValueDirect(value, concreteType, typeContext, markers, context);
		if (newValue != null)
		{
			if (typeContext != null && !typeContext.isReadonly())
			{
				type.inferTypes(newValue.getType(), typeContext);
			}
			return newValue;
		}

		if (value.isResolved())
		{
			markers.add(markerSupplier.createMarker(value.getPosition(), concreteType, value.getType()));
		}
		return value;
	}

	public static Marker typeError(IValue value, IType type, ITypeContext typeContext, String key, Object... args)
	{
		return typeError(value.getPosition(), type.getConcreteType(typeContext), value.getType(), key, args);
	}

	public static Marker typeError(ICodePosition position, IType expected, IType actual, String key, Object... args)
	{
		return typeError(position, expected, actual, key, "type.expected", "value.type", args);
	}

	public static Marker typeError(ICodePosition position, IType expected, IType actual, String key, String expectedError, String actualError, Object... args)
	{
		final Marker marker = Markers.semanticError(position, key, args);
		marker.addInfo(Markers.getSemantic(expectedError, expected));
		marker.addInfo(Markers.getSemantic(actualError, actual));
		return marker;
	}

	public static Marker typeError(ICodePosition position, IType expectedType, IType actualType, String error, String expectedError, String actualError)
	{
		final Marker marker = Markers.semanticError(position, error);
		marker.addInfo(Markers.getSemantic(expectedError, expectedType));
		marker.addInfo(Markers.getSemantic(actualError, actualType));
		return marker;
	}
}

package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class LiteralConversion implements IValue
{
	private IValue     literal;
	private IArguments arguments;
	private IType      type;
	
	private IMethod method;
	
	private Name methodName = Names.apply;
	
	public LiteralConversion(IValue literal, IAnnotation annotation)
	{
		this.literal = literal;
		this.arguments = new SingleArgument(literal);
		this.methodName = getMethodName(annotation);
	}
	
	public LiteralConversion(IValue literal, IAnnotation annotation, IArguments arguments)
	{
		this.literal = literal;
		this.arguments = arguments;
		this.methodName = getMethodName(annotation);
	}
	
	public LiteralConversion(IValue literal, IMethod method)
	{
		this.literal = literal;
		this.arguments = new SingleArgument(literal);
		this.method = method;
	}
	
	public LiteralConversion(IValue literal, IMethod method, IArguments arguments)
	{
		this.literal = literal;
		this.arguments = arguments;
		this.method = method;
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
	public ICodePosition getPosition()
	{
		return this.literal.getPosition();
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public int valueTag()
	{
		return LITERAL_CONVERSION;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public boolean isResolved()
	{
		return this.method != null;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.method == null)
		{
			this.method = IContext.resolveMethod(type, null, this.methodName, this.arguments);
			if (this.method == null)
			{
				StringBuilder builder = new StringBuilder();
				this.arguments.typesToString(builder);
				markers.add(Markers.semantic(this.literal.getPosition(), "literal.method", this.literal.getType(), type,
				                             builder));
				this.type = type;
				return null;
			}
		}
		
		final GenericData genericData = this.method.getGenericData(null, null, this.arguments);

		this.method.checkArguments(markers, this.literal.getPosition(), context, null, this.arguments, genericData);
		this.type = this.method.getType().getConcreteType(genericData);
		
		final IType concrete = type.getConcreteType(typeContext).asParameterType();
		if (!Types.isSuperType(concrete, this.type))
		{
			final Marker marker = Markers.semantic(this.literal.getPosition(), "literal.type.incompatible");
			marker.addInfo(Markers.getSemantic("type.expected", concrete));
			marker.addInfo(Markers.getSemantic("literal.type.conversion", this.type));

			final StringBuilder stringBuilder = new StringBuilder();
			Util.methodSignatureToString(this.method, typeContext, stringBuilder);
			marker.addInfo(Markers.getSemantic("literal.type.method", stringBuilder.toString()));
			
			markers.add(marker);
		}
		
		return this;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.arguments.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.arguments.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.arguments.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (type == null)
		{
			type = this.type;
		}

		this.method.writeCall(writer, null, this.arguments, this.type, type, this.literal.getLineNumber());
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.literal.toString(prefix, buffer);
	}
}

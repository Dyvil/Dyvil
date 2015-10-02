package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class LiteralExpression implements IValue
{
	private IValue		literal;
	private IArguments	arguments;
	private IType		type;
	
	private IMethod method;
	
	private Name methodName = Name.apply;
	
	public LiteralExpression(IValue literal, IAnnotation annotation)
	{
		this.literal = literal;
		this.arguments = new SingleArgument(literal);
	}
	
	public LiteralExpression(IValue literal, IAnnotation annotation, IArguments arguments)
	{
		this.literal = literal;
		this.arguments = arguments;
	}
	
	public LiteralExpression(IValue literal, IMethod method)
	{
		this.literal = literal;
		this.arguments = new SingleArgument(literal);
		this.method = method;
	}
	
	public LiteralExpression(IValue literal, IMethod method, IArguments arguments)
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
		return Name.apply;
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
		return BOXED;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		IMethod method = this.method;
		if (method == null)
		{
			method = IContext.resolveMethod(type, null, this.methodName, this.arguments);
			if (method == null)
			{
				StringBuilder builder = new StringBuilder();
				this.arguments.typesToString(builder);
				markers.add(this.literal.getPosition(), "literal.method", this.literal.getType(), type, builder);
				this.type = type;
				return null;
			}
			
			this.method = method;
		}
		
		GenericData data = method.getGenericData(null, null, this.arguments);
		method.checkArguments(markers, this.literal.getPosition(), context, null, this.arguments, data);
		this.type = method.getType().getConcreteType(data);
		
		if (!type.isSuperTypeOf(this.type))
		{
			Marker m = markers.create(this.literal.getPosition(), "literal.type");
			m.addInfo("Required Type: " + type.getConcreteType(typeContext));
			m.addInfo("Conversion Type: " + this.type);
			
			StringBuilder sb = new StringBuilder("Conversion Method: \n\t\t");
			Util.methodSignatureToString(method, sb);
			m.addInfo(sb.toString());
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
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.method.writeCall(writer, null, this.arguments, null, this.literal.getLineNumber());
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.literal.writeStatement(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.literal.toString(prefix, buffer);
	}
}

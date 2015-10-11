package dyvil.tools.compiler.ast.annotation;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.consumer.IAnnotationConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class AnnotationValue implements IValue, IAnnotationConsumer
{
	protected IAnnotation annotation;
	
	private boolean isAnnotationParameter;
	
	public AnnotationValue()
	{
	}
	
	public AnnotationValue(IAnnotation annotation)
	{
		this.annotation = annotation;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.annotation.getPosition();
	}
	
	@Override
	public void setAnnotation(IAnnotation annotation)
	{
		this.annotation = annotation;
	}
	
	public IAnnotation getAnnotation()
	{
		return this.annotation;
	}
	
	@Override
	public int valueTag()
	{
		return ANNOTATION;
	}
	
	@Override
	public IType getType()
	{
		return this.annotation.getType();
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(this.annotation.getType());
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.isType(type))
		{
			return this;
		}
		return null;
	}
	
	@Override
	public IValue toConstant(MarkerList markers)
	{
		this.isAnnotationParameter = true;
		return this;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.annotation.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.annotation.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.annotation.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.annotation.check(markers, context, null);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.annotation.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.annotation.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		// TODO Bytecode Generation
		writer.writeInsn(Opcodes.ACONST_NULL);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.annotation.toString(prefix, buffer);
	}
}

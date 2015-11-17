package dyvil.tools.compiler.ast.annotation;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Handle;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.IAnnotationConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class AnnotationValue implements IValue, IAnnotationConsumer
{
	private static final Handle ANNOTATION_METAFACTORY = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/runtime/AnnotationMetafactory", "metafactory",
			"(Ljava/lang/invoke/MethodHandles$Lookup;" + "Ljava/lang/String;" + "Ljava/lang/invoke/MethodType;" + "[Ljava/lang/Object;"
					+ ")Ljava/lang/invoke/CallSite;");
					
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
	public boolean isResolved()
	{
		return this.annotation.getType().isResolved();
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
		StringBuilder descBuilder = new StringBuilder().append('(');
		
		IArguments arguments = this.annotation.getArguments();
		IClass iclass = this.annotation.getType().getTheClass();
		
		int len = iclass.parameterCount();
		String[] parameterNames = new String[len];
		for (int i = 0; i < len; i++)
		{
			IParameter parameter = iclass.getParameter(i);
			IType type = parameter.getType();
			parameterNames[i] = parameter.getName().qualified;
			type.appendExtendedName(descBuilder);
			
			IValue value = arguments.getValue(i, parameter);
			if (value == null)
			{
				value = parameter.getValue().withType(type, type, null, null);
			}
			
			arguments.writeValue(i, parameter, writer);
		}
		
		descBuilder.append(')');
		descBuilder.append('L').append(iclass.getInternalName()).append(';');
		
		writer.writeInvokeDynamic("_", descBuilder.toString(), ANNOTATION_METAFACTORY, (Object[]) parameterNames);
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

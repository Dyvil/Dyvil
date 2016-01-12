package dyvil.tools.compiler.ast.annotation;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.MethodVisitor;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassMetadata;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;
import java.util.Set;

public final class AnnotationMetadata implements IClassMetadata
{
	private IClass           theClass;
	public  RetentionPolicy  retention;
	public  Set<ElementType> targets;
	
	public AnnotationMetadata(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public RetentionPolicy getRetention()
	{
		return this.retention;
	}
	
	@Override
	public Set<ElementType> getTargets()
	{
		return this.targets;
	}
	
	@Override
	public boolean isTarget(ElementType target)
	{
		if (this.targets == null || this.targets.isEmpty())
		{
			return true;
		}
		return this.targets.contains(target);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		// Add the java.lang.Annotation interface
		if (!this.theClass.isSubTypeOf(Annotation.Types.ANNOTATION))
		{
			this.theClass.addInterface(Annotation.Types.ANNOTATION);
		}
		
		if (this.retention == null)
		{
			IAnnotation retention = this.theClass.getAnnotation(Annotation.Types.RETENTION_CLASS);
			if (retention != null)
			{
				INamed value = (INamed) retention.getArguments().getValue(0, Annotation.VALUE);
				try
				{
					this.retention = RetentionPolicy.valueOf(value.getName().qualified);
				}
				catch (IllegalArgumentException ex)
				{
					// Problematic RentionPolicy annotation - do not handle this
				}
			}
		}
		if (this.targets != null)
		{
			return;
		}
		
		IAnnotation target = this.theClass.getAnnotation(Annotation.Types.TARGET_CLASS);
		if (target == null)
		{
			return;
		}
		
		this.targets = EnumSet.noneOf(ElementType.class);
		IValueList values = (IValueList) target.getArguments().getValue(0, Annotation.VALUE);
		if (values == null)
		{
			return;
		}
		
		int count = values.valueCount();
		for (int i = 0; i < count; i++)
		{
			INamed value = (INamed) values.getValue(i);
			ElementType elementType;
			try
			{
				elementType = ElementType.valueOf(value.getName().qualified);
			}
			catch (IllegalArgumentException ex)
			{
				// Problematic Target annotation - do not handle this
				continue;
			}
			this.targets.add(elementType);
		}
	}
	
	@Override
	public void resolveTypesBody(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		int params = this.theClass.parameterCount();
		for (int i = 0; i < params; i++)
		{
			IParameter param = this.theClass.getParameter(i);
			
			StringBuilder desc = new StringBuilder("()");
			param.getType().appendExtendedName(desc);
			MethodVisitor mw = writer
					.visitMethod(Modifiers.PUBLIC | Modifiers.ABSTRACT, param.getName().qualified, desc.toString(),
					             null, null);
			
			IValue value = param.getValue();
			if (value != null)
			{
				AnnotationVisitor av = mw.visitAnnotationDefault();
				Annotation.visitValue(av, null, value);
			}
		}
	}
}

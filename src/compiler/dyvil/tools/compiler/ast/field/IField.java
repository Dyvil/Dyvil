package dyvil.tools.compiler.ast.field;

import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.type.IType;

public interface IField extends IClassMember, IDataMember
{
	@Override
	public IClass getTheClass();
	
	@Override
	public default boolean isField()
	{
		return true;
	}
	
	@Override
	public default boolean isVariable()
	{
		return false;
	}
	
	public static void writeAnnotations(FieldVisitor fv, AnnotationList annotations, IType type)
	{
		if (annotations != null)
		{
			int count = annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				annotations.getAnnotation(i).write(fv);
			}
		}
		
		type.writeAnnotations(fv, TypeReference.FIELD, "");
	}
}

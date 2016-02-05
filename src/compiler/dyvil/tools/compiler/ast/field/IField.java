package dyvil.tools.compiler.ast.field;

import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.annotation.AnnotationUtils;

public interface IField extends IClassMember, IDataMember
{
	@Override
	IClass getTheClass();
	
	@Override
	default boolean isField()
	{
		return true;
	}
	
	@Override
	default boolean isVariable()
	{
		return false;
	}
	
	static void writeAnnotations(FieldVisitor fv, ModifierSet modifierSet, AnnotationList annotations, IType type)
	{
		AnnotationUtils.writeModifiers(fv, modifierSet);

		if (annotations != null)
		{
			int count = annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				annotations.getAnnotation(i).write(fv);
			}
		}
		
		type.writeAnnotations(fv, TypeReference.newTypeReference(TypeReference.FIELD), "");
	}
}

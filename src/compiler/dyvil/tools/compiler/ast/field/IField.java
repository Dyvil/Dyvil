package dyvil.tools.compiler.ast.field;

import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;

public interface IField extends IClassMember, IDataMember
{
	@Override
	IClass getEnclosingClass();

	@Override
	default MemberKind getKind()
	{
		return MemberKind.FIELD;
	}

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
		ModifierUtil.writeModifiers(fv, modifierSet);

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

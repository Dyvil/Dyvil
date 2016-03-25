package dyvil.tools.compiler.ast.consumer;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public interface IDataMemberConsumer<T extends IDataMember>
{
	void addDataMember(T dataMember);

	T createDataMember(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations);
}

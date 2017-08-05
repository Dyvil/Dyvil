package dyvilx.tools.compiler.ast.consumer;

import dyvilx.tools.compiler.ast.annotation.AnnotationList;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.type.IType;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;

public interface IDataMemberConsumer<T extends IDataMember>
{
	void addDataMember(T dataMember);

	T createDataMember(SourcePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations);
}

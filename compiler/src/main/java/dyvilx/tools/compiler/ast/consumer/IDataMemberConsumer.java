package dyvilx.tools.compiler.ast.consumer;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.type.IType;

public interface IDataMemberConsumer<T extends IDataMember>
{
	void addDataMember(T dataMember);

	T createDataMember(SourcePosition position, Name name, IType type, AttributeList attributes);
}

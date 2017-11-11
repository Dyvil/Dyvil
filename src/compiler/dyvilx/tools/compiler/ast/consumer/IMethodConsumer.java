package dyvilx.tools.compiler.ast.consumer;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.method.CodeMethod;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.type.IType;

public interface IMethodConsumer
{
	void addMethod(IMethod method);

	default IMethod createMethod(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		return new CodeMethod(position, name, type, attributes);
	}
}

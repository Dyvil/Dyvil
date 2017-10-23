package dyvilx.tools.compiler.ast.consumer;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.CodeClass;
import dyvilx.tools.compiler.ast.classes.IClass;

public interface IClassConsumer
{
	void addClass(IClass theClass);

	default IClass createClass(SourcePosition position, Name name, AttributeList annotations)
	{
		return new CodeClass(position, name, annotations);
	}
}

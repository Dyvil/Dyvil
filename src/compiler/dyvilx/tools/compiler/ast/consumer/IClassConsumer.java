package dyvilx.tools.compiler.ast.consumer;

import dyvilx.tools.compiler.ast.annotation.AnnotationList;
import dyvilx.tools.compiler.ast.classes.CodeClass;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;

public interface IClassConsumer
{
	void addClass(IClass theClass);

	default IClass createClass(SourcePosition position, Name name, ModifierSet modifiers, AnnotationList annotations)
	{
		return new CodeClass(position, name, modifiers, annotations);
	}
}

package dyvil.tools.compiler.ast.consumer;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public interface IClassConsumer
{
	void addClass(IClass theClass);

	default IClass createClass(ICodePosition position, Name name, ModifierSet modifiers, AnnotationList annotations)
	{
		return new CodeClass(position, name, modifiers, annotations);
	}
}

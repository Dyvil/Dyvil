package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.ast.consumer.IClassConsumer;
import dyvil.tools.parsing.Name;

public interface IClassList extends IClassConsumer
{
	int classCount();
	
	void addClass(IClass iclass);
	
	IClass getClass(int index);
	
	IClass getClass(Name name);
}

package dyvilx.tools.compiler.ast.classes;

import dyvilx.tools.compiler.ast.consumer.IClassConsumer;
import dyvil.lang.Name;

public interface IClassList extends IClassConsumer
{
	int classCount();
	
	void addClass(IClass iclass);
	
	IClass getClass(int index);
	
	IClass getClass(Name name);
}

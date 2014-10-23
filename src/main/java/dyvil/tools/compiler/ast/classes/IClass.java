package dyvil.tools.compiler.ast.classes;

import java.util.List;

import dyvil.tools.compiler.ast.api.IASTObject;
import dyvil.tools.compiler.ast.api.IAnnotatable;
import dyvil.tools.compiler.ast.api.IModified;
import dyvil.tools.compiler.ast.api.INamed;
import dyvil.tools.compiler.ast.structure.IContext;

public interface IClass extends IASTObject, INamed, IModified, IAnnotatable, IContext
{
	public default void addSuperClass(IClass superClass)
	{
		this.getSuperClasses().add(superClass);
	}
	
	public String getGenericName();
	
	public List<IClass> getSuperClasses();
	
	public void setBody(ClassBody body);
	
	public ClassBody getBody();
}

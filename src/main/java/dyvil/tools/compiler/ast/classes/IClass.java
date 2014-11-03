package dyvil.tools.compiler.ast.classes;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public interface IClass extends IASTObject, INamed, IModified, ITypeList, IAnnotatable, IContext
{
	public default void addSuperClass(Type superClass)
	{
		if (superClass != null)
		{
			this.getSuperClasses().add(superClass);
		}
	}
	
	public List<Type> getSuperClasses();
	
	public void setBody(ClassBody body);
	
	public ClassBody getBody();
	
	// Compilation
	
	public void write(ClassWriter writer);
	
	public String getInternalName();
}

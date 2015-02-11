package dyvil.tools.compiler.ast.dynamic;

import jdk.internal.org.objectweb.asm.Handle;
import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;

public class DynamicMethod extends ASTNode
{
	public static final Handle	BOOTSTRAP	= new Handle(Opcodes.H_INVOKEVIRTUAL, "dyvil/dyn/DynamicLinker", "linkMethod",
													"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType)Ljava/lang/invoke/CallSite;");
	
	public String				name;

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
	}
}

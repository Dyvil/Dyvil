package dyvil.tools.compiler.ast.api;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface IMethod extends IASTNode, IMember, IGeneric, IValued, IThrower, IVariableList, IParameterized, IContext
{
	public void checkArguments(List<Marker> markers, IValue instance, List<IValue> arguments);
	
	public int getSignatureMatch(String name, IType type, IType... argumentTypes);
	
	// @Bytecode
	
	public default void writePrefixBytecode(MethodWriter writer)
	{
	}
	
	public default void writeInfixBytecode(MethodWriter writer)
	{
	}
	
	public default boolean writePostfixBytecode(MethodWriter writer)
	{
		return false;
	}
	
	// Compilation
	
	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
	
	public void write(ClassWriter writer);
}

package dyvil.tools.compiler.ast.method;

import java.util.List;

import jdk.internal.org.objectweb.asm.Label;
import dyvil.tools.compiler.ast.generic.IGeneric;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameterized;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface IMethod extends IBaseMethod, IMember, IParameterized, IGeneric, IThrower
{
	public int getSignatureMatch(String name, IValue instance, IArguments arguments);
	
	public void checkArguments(List<Marker> markers, IValue instance, IArguments arguments, ITypeContext typeContext);
	
	// Generics
	
	public boolean hasTypeVariables();
	
	public IType resolveType(String name, IValue instance, IArguments arguments, List<IType> generics);
	
	// Compilation
	
	public boolean isIntrinsic();
	
	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
	
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments);
	
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments);
	
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments);
}

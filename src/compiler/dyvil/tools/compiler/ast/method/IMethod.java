package dyvil.tools.compiler.ast.method;

import org.objectweb.asm.Label;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IMethod extends IASTNode, IMember, IBaseMethod, IMethodSignature, IContext, IClassCompilable
{
	public int getSignatureMatch(Name name, IValue instance, IArguments arguments);
	
	public IValue checkArguments(MarkerList markers, IValue instance, IArguments arguments, ITypeContext typeContext);
	
	// Misc
	
	public void setParameters(IParameter[] parameters, int parameterCount);
	
	@Override
	public default void addType(IType type)
	{
		int index = this.parameterCount();
		this.addParameter(new MethodParameter(Name.getQualified("par" + index), type));
	}
	
	// Generics
	
	@Override
	public default boolean isMethod()
	{
		return true;
	}
	
	public GenericData getGenericData(GenericData data, IValue instance, IArguments arguments);
	
	public boolean hasTypeVariables();
	
	// Compilation
	
	public boolean isIntrinsic();
	
	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
	
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type);
	
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments);
	
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments);
}

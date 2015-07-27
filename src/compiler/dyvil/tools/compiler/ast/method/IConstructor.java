package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IConstructor extends IClassMember, ICallableMember, ITypeList, IContext
{
	public float getSignatureMatch(IArguments arguments);
	
	public void checkArguments(MarkerList markers, ICodePosition position, IContext context, IArguments arguments);
	
	public void checkCall(MarkerList markers, ICodePosition position, IContext context, IArguments arguments);
	
	// Misc
	
	public void setParameters(IParameter[] parameters, int parameterCount);
	
	@Override
	public default int typeCount()
	{
		return 0;
	}
	
	@Override
	public default void setType(int index, IType type)
	{
	}
	
	@Override
	public default void addType(IType type)
	{
		int index = this.parameterCount();
		this.addParameter(new MethodParameter(Name.getQualified("par" + index), type));
	}
	
	@Override
	public default IType getType(int index)
	{
		return null;
	}
	
	@Override
	public void setType(IType type);
	
	// Compilation
	
	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
	
	public void writeCall(MethodWriter writer, IArguments arguments, IType type, int lineNumber) throws BytecodeException;
	
	public void writeInvoke(MethodWriter writer, int lineNumber) throws BytecodeException;
	
	public void writeArguments(MethodWriter writer, IArguments arguments) throws BytecodeException;
	
	public void write(ClassWriter writer, IValue instanceFields) throws BytecodeException;
}

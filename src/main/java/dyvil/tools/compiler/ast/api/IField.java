package dyvil.tools.compiler.ast.api;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.bytecode.MethodWriter;

public interface IField extends IASTNode, IMember, IValued
{
	@Override
	public IField applyState(CompilerState state, IContext context);
	
	@Override
	public IClass getTheClass();
	
	// Compilation
	
	public void write(ClassWriter writer);
	
	public void writeGet(MethodWriter writer);
	
	public void writeSet(MethodWriter writer);
	
	public String getDescription();
	
	public String getSignature();
}

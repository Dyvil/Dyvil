package dyvil.tools.compiler.ast.api;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.structure.IContext;

public interface IField extends IASTObject, INamed, ITyped, IModified, IAnnotatable, IValued
{
	@Override
	public IField applyState(CompilerState state, IContext context);
	
	public IClass getTheClass();
	
	// Compilation
	
	public void write(ClassWriter writer);
	
	public void writeGet(MethodVisitor visitor);
	
	public void writeSet(MethodVisitor visitor);
	
	public String getDescription();
	
	public String getSignature();
}

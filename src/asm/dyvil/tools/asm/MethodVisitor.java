/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package dyvil.tools.asm;

public interface MethodVisitor extends AnnotatableVisitor, TypeAnnotatableVisitor
{
	public void visitParameter(String name, int access);
	
	public AnnotationVisitor visitAnnotationDefault();
	
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible);
	
	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);
	
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible);
	
	public void visitAttribute(Attribute attr);
	
	public void visitCode();
	
	public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack);
	
	public void visitInsn(int opcode);
	
	public void visitIntInsn(int opcode, int operand);
	
	public void visitVarInsn(int opcode, int var);
	
	public void visitTypeInsn(int opcode, String type);
	
	public void visitFieldInsn(int opcode, String owner, String name, String desc);
	
	@Deprecated
	public default void visitMethodInsn(int opcode, String owner, String name, String desc)
	{
		this.visitMethodInsn(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE);
	};
	
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf);
	
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs);
	
	public void visitJumpInsn(int opcode, Label label);
	
	public void visitLabel(Label label);
	
	public void visitLdcInsn(Object cst);
	
	public void visitIincInsn(int var, int increment);
	
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels);
	
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels);
	
	public void visitMultiANewArrayInsn(String desc, int dims);
	
	public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);
	
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type);
	
	public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);
	
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index);
	
	public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc,
			boolean visible);
			
	public void visitLineNumber(int line, Label start);
	
	public void visitMaxs(int maxStack, int maxLocals);
	
	public void visitEnd();
}

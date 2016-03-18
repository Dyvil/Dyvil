/***
 * ASM: a very small and fast Java bytecode manipulation framework Copyright (c) 2000-2011 INRIA, France Telecom All
 * rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution. 3. Neither the name of the copyright holders nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dyvil.tools.asm;

public interface MethodVisitor extends AnnotatableVisitor, TypeAnnotatableVisitor
{
	void visitParameter(String name, int access);
	
	AnnotationVisitor visitAnnotationDefault();
	
	@Override
	AnnotationVisitor visitAnnotation(String desc, boolean visible);
	
	@Override
	AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);
	
	AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible);
	
	void visitAttribute(Attribute attr);
	
	boolean visitCode();
	
	void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack);
	
	void visitInsn(int opcode);
	
	void visitIntInsn(int opcode, int operand);
	
	void visitVarInsn(int opcode, int var);
	
	void visitTypeInsn(int opcode, String type);
	
	void visitFieldInsn(int opcode, String owner, String name, String desc);
	
	@Deprecated
	default void visitMethodInsn(int opcode, String owner, String name, String desc)
	{
		this.visitMethodInsn(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE);
	}
	
	void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf);
	
	void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs);
	
	void visitJumpInsn(int opcode, Label label);
	
	void visitLabel(Label label);
	
	void visitLdcInsn(Object cst);
	
	void visitIincInsn(int var, int increment);
	
	void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels);
	
	void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels);
	
	void visitMultiANewArrayInsn(String desc, int dims);
	
	AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);
	
	void visitTryCatchBlock(Label start, Label end, Label handler, String type);
	
	AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible);
	
	void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index);
	
	AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible);

	void visitLineNumber(int line, Label start);
	
	void visitMaxs(int maxStack, int maxLocals);
	
	void visitEnd();
}

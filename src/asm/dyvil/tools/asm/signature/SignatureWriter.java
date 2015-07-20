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
package dyvil.tools.asm.signature;

import dyvil.tools.asm.SignatureVisitor;

public class SignatureWriter implements SignatureVisitor
{
	private final StringBuffer	buf	= new StringBuffer();
	private boolean				hasFormals;
	private boolean				hasParameters;
	private int					argumentStack;
	
	@Override
	public void visitFormalTypeParameter(final String name)
	{
		if (!this.hasFormals)
		{
			this.hasFormals = true;
			this.buf.append('<');
		}
		this.buf.append(name);
		this.buf.append(':');
	}
	
	@Override
	public SignatureVisitor visitClassBound()
	{
		return this;
	}
	
	@Override
	public SignatureVisitor visitInterfaceBound()
	{
		this.buf.append(':');
		return this;
	}
	
	@Override
	public SignatureVisitor visitSuperclass()
	{
		this.endFormals();
		return this;
	}
	
	@Override
	public SignatureVisitor visitInterface()
	{
		return this;
	}
	
	@Override
	public SignatureVisitor visitParameterType()
	{
		this.endFormals();
		if (!this.hasParameters)
		{
			this.hasParameters = true;
			this.buf.append('(');
		}
		return this;
	}
	
	@Override
	public SignatureVisitor visitReturnType()
	{
		this.endFormals();
		if (!this.hasParameters)
		{
			this.buf.append('(');
		}
		this.buf.append(')');
		return this;
	}
	
	@Override
	public SignatureVisitor visitExceptionType()
	{
		this.buf.append('^');
		return this;
	}
	
	@Override
	public void visitBaseType(final char descriptor)
	{
		this.buf.append(descriptor);
	}
	
	@Override
	public void visitTypeVariable(final String name)
	{
		this.buf.append('T');
		this.buf.append(name);
		this.buf.append(';');
	}
	
	@Override
	public SignatureVisitor visitArrayType()
	{
		this.buf.append('[');
		return this;
	}
	
	@Override
	public void visitClassType(final String name)
	{
		this.buf.append('L');
		this.buf.append(name);
		this.argumentStack *= 2;
	}
	
	@Override
	public void visitInnerClassType(final String name)
	{
		this.endArguments();
		this.buf.append('.');
		this.buf.append(name);
		this.argumentStack *= 2;
	}
	
	@Override
	public void visitTypeArgument()
	{
		if (this.argumentStack % 2 == 0)
		{
			++this.argumentStack;
			this.buf.append('<');
		}
		this.buf.append('*');
	}
	
	@Override
	public SignatureVisitor visitTypeArgument(final char wildcard)
	{
		if (this.argumentStack % 2 == 0)
		{
			++this.argumentStack;
			this.buf.append('<');
		}
		if (wildcard != '=')
		{
			this.buf.append(wildcard);
		}
		return this;
	}
	
	@Override
	public void visitEnd()
	{
		this.endArguments();
		this.buf.append(';');
	}
	
	@Override
	public String toString()
	{
		return this.buf.toString();
	}
	
	private void endFormals()
	{
		if (this.hasFormals)
		{
			this.hasFormals = false;
			this.buf.append('>');
		}
	}
	
	private void endArguments()
	{
		if (this.argumentStack % 2 != 0)
		{
			this.buf.append('>');
		}
		this.argumentStack /= 2;
	}
}

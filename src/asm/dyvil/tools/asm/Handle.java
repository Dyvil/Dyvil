/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
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

public final class Handle
{
	final int    tag;
	final String owner;
	final String name;
	final String desc;
	
	public Handle(int tag, String owner, String name, String desc)
	{
		this.tag = tag;
		this.owner = owner;
		this.name = name;
		this.desc = desc;
	}
	
	public int getTag()
	{
		return this.tag;
	}
	
	public String getOwner()
	{
		return this.owner;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getDesc()
	{
		return this.desc;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof Handle))
		{
			return false;
		}
		Handle h = (Handle) obj;
		return this.tag == h.tag && this.owner.equals(h.owner) && this.name.equals(h.name) && this.desc.equals(h.desc);
	}
	
	@Override
	public int hashCode()
	{
		return this.tag + this.owner.hashCode() * this.name.hashCode() * this.desc.hashCode();
	}
	
	@Override
	public String toString()
	{
		return this.owner + '.' + this.name + this.desc + " (" + this.tag + ')';
	}
}

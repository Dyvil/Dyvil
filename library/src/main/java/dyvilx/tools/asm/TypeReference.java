/***
 * ASM: a very small and fast Java bytecode manipulation framework Copyright (c) 2000-2013 INRIA, France Telecom All
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
package dyvilx.tools.asm;

public class TypeReference
{
	public final static int CLASS_TYPE_PARAMETER        = 0x00;
	public final static int METHOD_TYPE_PARAMETER       = 0x01;
	public final static int CLASS_EXTENDS               = 0x10;
	public final static int CLASS_TYPE_PARAMETER_BOUND  = 0x11;
	public final static int METHOD_TYPE_PARAMETER_BOUND = 0x12;
	
	public final static int FIELD = 0x13;
	
	public final static int METHOD_RETURN           = 0x14;
	public final static int METHOD_RECEIVER         = 0x15;
	public final static int METHOD_FORMAL_PARAMETER = 0x16;
	public final static int THROWS                  = 0x17;
	
	public final static int LOCAL_VARIABLE      = 0x40;
	public final static int RESOURCE_VARIABLE   = 0x41;
	public final static int EXCEPTION_PARAMETER = 0x42;
	
	public final static int INSTANCEOF                           = 0x43;
	public final static int NEW                                  = 0x44;
	public final static int CONSTRUCTOR_REFERENCE                = 0x45;
	public final static int METHOD_REFERENCE                     = 0x46;
	public final static int CAST                                 = 0x47;
	public final static int CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT = 0x48;
	public final static int METHOD_INVOCATION_TYPE_ARGUMENT      = 0x49;
	public final static int CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT  = 0x4A;
	public final static int METHOD_REFERENCE_TYPE_ARGUMENT       = 0x4B;
	
	public static int newTypeReference(int sort)
	{
		return sort << 24;
	}
	
	public static int newTypeParameterReference(int sort, int paramIndex)
	{
		return sort << 24 | paramIndex << 16;
	}
	
	public static int newTypeParameterBoundReference(int sort, int paramIndex, int boundIndex)
	{
		return sort << 24 | paramIndex << 16 | boundIndex << 8;
	}
	
	public static int newSuperTypeReference(int itfIndex)
	{
		itfIndex &= 0xFFFF;
		return CLASS_EXTENDS << 24 | itfIndex << 8;
	}
	
	public static int newFormalParameterReference(int paramIndex)
	{
		return METHOD_FORMAL_PARAMETER << 24 | paramIndex << 16;
	}
	
	public static int newExceptionReference(int exceptionIndex)
	{
		return THROWS << 24 | exceptionIndex << 8;
	}
	
	public static int newTryCatchReference(int tryCatchBlockIndex)
	{
		return EXCEPTION_PARAMETER << 24 | tryCatchBlockIndex << 8;
	}
	
	public static int newTypeArgumentReference(int sort, int argIndex)
	{
		return sort << 24 | argIndex;
	}
	
	public static int getSort(int value)
	{
		return value >>> 24;
	}
	
	public static int getTypeParameterIndex(int value)
	{
		return (value & 0x00FF0000) >> 16;
	}
	
	public static int getTypeParameterBoundIndex(int value)
	{
		return (value & 0x0000FF00) >> 8;
	}
	
	public static int getSuperTypeIndex(int value)
	{
		return (short) ((value & 0x00FFFF00) >> 8);
	}
	
	public static int getFormalParameterIndex(int value)
	{
		return (value & 0x00FF0000) >> 16;
	}
	
	public static int getExceptionIndex(int value)
	{
		return (value & 0x00FFFF00) >> 8;
	}
	
	public static int getTryCatchBlockIndex(int value)
	{
		return (value & 0x00FFFF00) >> 8;
	}
	
	public static int getTypeArgumentIndex(int value)
	{
		return value & 0xFF;
	}
}

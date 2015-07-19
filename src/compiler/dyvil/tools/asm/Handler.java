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

class Handler
{
	Label	start;
	Label	end;
	Label	handler;
	String	desc;
	int		type;
	Handler	next;
	
	static Handler remove(Handler h, Label start, Label end)
	{
		if (h == null)
		{
			return null;
		}
		
		h.next = remove(h.next, start, end);
		int hstart = h.start.position;
		int hend = h.end.position;
		int s = start.position;
		int e = end == null ? Integer.MAX_VALUE : end.position;
		// if [hstart,hend[ and [s,e[ intervals intersect...
		if (s < hend && e > hstart)
		{
			if (s <= hstart)
			{
				if (e >= hend)
				{
					// [hstart,hend[ fully included in [s,e[, h removed
					h = h.next;
				}
				else
				{
					// [hstart,hend[ minus [s,e[ = [e,hend[
					h.start = end;
				}
			}
			else if (e >= hend)
			{
				// [hstart,hend[ minus [s,e[ = [hstart,s[
				h.end = start;
			}
			else
			{
				// [hstart,hend[ minus [s,e[ = [hstart,s[ + [e,hend[
				Handler g = new Handler();
				g.start = end;
				g.end = h.end;
				g.handler = h.handler;
				g.desc = h.desc;
				g.type = h.type;
				g.next = h.next;
				h.end = start;
				h.next = g;
			}
		}
		return h;
	}
}

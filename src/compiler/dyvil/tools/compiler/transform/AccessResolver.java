package dyvil.tools.compiler.transform;

import java.util.LinkedList;
import java.util.ListIterator;

import dyvil.tools.compiler.ast.access.IAccess;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class AccessResolver
{
	public static IValue resolve(MarkerList markers, IContext context, IAccess access)
	{
		LinkedList<IAccess> chain = getCallChain(markers, context, access);
		
		ListIterator<IAccess> iterator = chain.listIterator();
		IAccess prev = null;
		IAccess curr = null;
		IValue alternate = null;
		
		boolean backwards = false;
		
		while (iterator.hasNext())
		{
			prev = curr;
			curr = iterator.next();
			
			if (alternate != null)
			{
				curr.setValue(alternate);
				alternate = null;
			}
			
			if (!curr.resolve(context, markers))
			{
				alternate = curr.resolve2(context);
				if (alternate instanceof IAccess)
				{
					curr = (IAccess) alternate;
					iterator.set(curr);
				}
				else if (alternate != null)
				{
					if (!iterator.hasNext())
					{
						return alternate;
					}
					iterator.remove();
					iterator.next().setValue(alternate);
					iterator.previous();
				}
				else
				{
					backwards = true;
					break;
				}
			}
		}
		
		if (!backwards)
		{
			return chain.getLast();
		}
		else if (chain.size() == 1)
		{
			access.addResolveError(markers);
			return access;
		}
		
		IAccess next = null;
		curr = access;
		prev = null;
		
		while (curr != null)
		{
			IValue value = curr.getValue();
			if (value instanceof IAccess)
			{
				prev = (IAccess) value;
			}
			else
			{
				prev = null;
			}
			
			if (next != null && (!curr.isResolved() || !next.isResolved()))
			{
				next.setValue(null);
				if (next.resolve(context, markers))
				{
					alternate = curr.resolve3(context, next);
					if (alternate instanceof IAccess)
					{
						if (next.getValue() == curr)
						{
							next.setValue(alternate);
						}
						next = (IAccess) alternate;
						curr = prev;
						
						if (iterator.hasNext())
						{
							iterator.next();
							iterator.remove();
							iterator.previous();
							iterator.set(next);
						}
						else
						{
							iterator.set(next);
						}
					}
					else
					{
						next.setValue(curr);
						curr.addResolveError(markers);
					}
				}
				else
				{
					curr.addResolveError(markers);
				}
			}
			next = curr;
			curr = prev;
		}
		
		return chain.getLast();
	}
	
	public static LinkedList<IAccess> getCallChain(MarkerList markers, IContext context, IAccess iaccess)
	{
		LinkedList<IAccess> list = new LinkedList();
		while (true)
		{
			list.addFirst(iaccess);
			
			IValue v = iaccess.getValue();
			if (v instanceof IAccess)
			{
				iaccess = (IAccess) v;
				continue;
			}
			else if (v != null)
			{
				IValue v2 = v.resolve(markers, context);
				if (v != v2)
				{
					iaccess.setValue(v2);
				}
			}
			break;
		}
		
		return list;
	}
}

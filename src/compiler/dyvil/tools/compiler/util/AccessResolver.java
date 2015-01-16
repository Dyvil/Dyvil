package dyvil.tools.compiler.util;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.lexer.marker.Marker;

public class AccessResolver
{
	public static IAccess resolve(List<Marker> markers, IContext context, IAccess access)
	{
		LinkedList<IAccess> chain = getCallChain(markers, context, access);
		
		ListIterator<IAccess> iterator = chain.listIterator();
		IAccess prev = null;
		IAccess curr = null;
		IAccess alternate = null;
		
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
			
			if (!curr.resolve(context))
			{
				alternate = curr.resolve2(context);
				if (alternate == null)
				{
					backwards = true;
				}
				else
				{
					curr = alternate;
					iterator.set(alternate);
				}
			}
		}
		
		if (!backwards)
		{
			return chain.getLast();
		}
		else if (chain.size() == 1)
		{
			markers.add(access.getResolveError());
			return access;
		}
		
		IAccess next = null;
		curr = access;
		prev = null;
		
		while (true)
		{
			IValue value = curr.getValue();
			if (value instanceof IAccess)
			{
				prev = (IAccess) value;
			}
			else
			{
				break;
			}
			
			if (!curr.resolve(context))
			{
				curr.setValue(null);
				if (curr.resolve(context))
				{
					prev.addValue(curr);
					curr = null;
					iterator.remove();
				}
				else
				{
					curr.setValue(value);
					markers.add(curr.getResolveError());
				}
			}
			
			next = curr;
			curr = prev;
		}
		
		return chain.getLast();
	}
	
	public static LinkedList<IAccess> getCallChain(List<Marker> markers, IContext context, IAccess iaccess)
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

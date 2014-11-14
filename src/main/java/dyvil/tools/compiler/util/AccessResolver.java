package dyvil.tools.compiler.util;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.Marker;

public class AccessResolver
{
	public static IAccess resolve(IContext context, IAccess access)
	{
		List<Marker> markers = CompilerState.RESOLVE.file.markers;
		LinkedList<IAccess> chain = getCallChain(access);
		
		ListIterator<IAccess> iterator = chain.listIterator();
		IAccess prev = null;
		IAccess curr = null;
		IAccess next = iterator.next();
		
		while (iterator.hasNext())
		{
			IContext context1 = context;
			
			prev = curr;
			curr = next;
			next = iterator.next();
			
			if (prev != null)
			{
				context1 = prev.getType();
				curr.setValue(prev);
			}
			else
			{
				IValue value = curr.getValue();
				if (value != null)
				{
					context1 = value.getType();
				}
			}
			
			if (!curr.resolve(context1, context))
			{
				IAccess alternate = curr.resolve2(context1, context);
				if (alternate == null)
				{
					if (next.resolve(context, context1))
					{
						alternate = curr.resolve3(context1, next);
						if (alternate != null)
						{
							next.setValue(null);
							curr = alternate;
							iterator.set(alternate);
							continue;
						}
					}
					
					markers.add(curr.getResolveError());
					return access;
				}
				
				curr = alternate;
				continue;
			}
		}
		
		return chain.getLast();
	}
	
	public static LinkedList<IAccess> getCallChain(IAccess iaccess)
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
			break;
		}
		
		return list;
	}
}

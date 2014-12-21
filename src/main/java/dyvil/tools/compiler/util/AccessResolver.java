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
		IContext context1 = context;
		IAccess prev = null;
		IAccess curr = null;
		
		boolean backwards = false;
		
		while (iterator.hasNext())
		{
			prev = curr;
			curr = iterator.next();
			
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
			
			if (context1 == null)
			{
				backwards = true;
				break;
			}
			
			if (!curr.resolve(context1, context))
			{
				IAccess alternate = curr.resolve2(context1, context);
				if (alternate == null)
				{
					// TODO
					markers.add(curr.getResolveError());
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
		
		IAccess next = curr;
		prev = iterator.previous();
		
		while (iterator.hasPrevious())
		{
			next = curr;
			curr = prev;
			prev = iterator.previous();
			
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
			
			if (context1 == null)
			{
				context1 = context;
			}
			
			IAccess alternate = curr.resolve3(context1, next);
			if (alternate != null)
			{
				curr = alternate;
				iterator.set(alternate);
			}
			
			markers.add(curr.getResolveError());
			
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

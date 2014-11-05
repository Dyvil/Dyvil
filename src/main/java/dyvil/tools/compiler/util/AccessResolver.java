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
		
		IAccess a = null;
		ListIterator<IAccess> iterator = chain.listIterator();
		while (iterator.hasNext())
		{
			IContext context1 = context;
			IAccess iaccess = iterator.next();
			if (a != null)
			{
				context1 = a.getType();
			}
			else
			{
				IValue value = iaccess.getValue();
				if (value != null)
				{
					context1 = value.getType();
				}
			}
			
			if (!iaccess.resolve(context1, context))
			{
				IAccess iaccess2 = iaccess.resolve2(context1, context);
				if (iaccess2 == iaccess)
				{
					markers.add(iaccess.getResolveError());
					return access;
				}
			}
			a = iaccess;
		}
		
		return access;
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

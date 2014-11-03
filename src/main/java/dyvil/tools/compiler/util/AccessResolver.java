package dyvil.tools.compiler.util;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IAccess;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.Marker;

import dyvil.tools.compiler.ast.structure.Package;

public class AccessResolver
{
	public static IAccess resolve(IContext context, IAccess access)
	{
		List<Marker> markers = CompilerState.RESOLVE.file.markers;
		
		LinkedList<IAccess> list = new LinkedList();
		
		IAccess a = access;
		while (true)
		{
			list.addFirst(a);
			
			IValue v = a.getValue();
			if (v instanceof IAccess)
			{
				a = (IAccess) v;
				continue;
			}
			break;
		}
		
		a = access;
		while (true)
		{
			List<IValue> params = a.getValues();
			int size = params.size();
			if (size == 1)
			{
				IValue v = params.get(0);
				if (v instanceof IAccess)
				{
					a = (IAccess) v;
					continue;
				}
			}
			break;
		}
		
		a = null;
		ListIterator<IAccess> iterator = list.listIterator();
		while (iterator.hasNext())
		{
			IContext context1 = context;
			IAccess iaccess = iterator.next();
			if (a != null)
			{
				context1 = a.getType().resolve(Package.rootPackage);
			}
			else
			{
				IValue value = iaccess.getValue();
				if (value != null)
				{
					context1 = value.getType().resolve(Package.rootPackage);
				}
			}
			
			if (!iaccess.resolve(context1))
			{
				IAccess iaccess2 = iaccess.resolve2(context1);
				if (iaccess2 == iaccess)
				{
					markers.add(iaccess.getResolveError());
				}
			}
			a = iaccess;
		}
		
		return access;
	}
}

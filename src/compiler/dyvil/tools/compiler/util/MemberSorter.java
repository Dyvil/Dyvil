package dyvil.tools.compiler.util;

import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.Name;

import java.util.Comparator;

public class MemberSorter
{
	public static final Comparator<? super IMember> MEMBER_COMPARATOR = MemberSorter::compareMembers;
	public static final Comparator<? super IMethod> METHOD_COMPARATOR = MemberSorter::compareMethods;

	public static int compareNames(Name name1, Name name2)
	{
		return name1.qualified.compareToIgnoreCase(name2.qualified);
	}

	public static int compareMembers(IMember member1, IMember member2)
	{
		return compareNames(member1.getName(), member2.getName());
	}

	public static int compareMethods(IMethod method1, IMethod method2)
	{
		final int n = compareMembers(method1, method2);
		if (n != 0)
		{
			return n;
		}
		if (method1.checkOverride(method2, null))
		{
			return compareTypes(method1.getType(), method2.getType());
		}

		return method1.getDescriptor().compareTo(method2.getDescriptor());
	}

	public static int compareTypes(IType type1, IType type2)
	{
		if (Types.isSuperType(type1, type2))
		{
			if (Types.isSuperType(type2, type1))
			{
				return 0; // same type
			}

			return 1; // type2 is more specific than type1
		}
		if (Types.isSuperType(type2, type1))
		{
			return -1; // type2 is less specific than type1
		}
		return 0;
	}
}

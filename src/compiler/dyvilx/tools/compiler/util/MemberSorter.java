package dyvilx.tools.compiler.util;

import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.member.IMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvil.lang.Name;

import java.util.Comparator;

public class MemberSorter
{
	public static final Comparator<? super IMember> MEMBER_COMPARATOR = MemberSorter::compareMembers;
	public static final Comparator<? super IMethod> METHOD_COMPARATOR = MemberSorter::compareMethods;

	public static final Comparator<? super IClass> CLASS_COMPARATOR = (a, b) ->
	{
		final int i = Boolean.compare(a.isInterface(), b.isInterface());
		// sort interfaces last
		return i != 0 ? i : compareClasses(a, b);
	};

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
		final int n = compareMembers(method1, method2); // compare names
		if (n != 0)
		{
			return n;
		}
		if (!method1.overrides(method2, null))
		{
			return method1.getSignature().compareTo(method2.getSignature());
		}

		final IType type1 = method1.getType().asParameterType();
		final IType type2 = method2.getType().asParameterType();

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
		return compareNames(type1.getName(), type2.getName());
	}

	public static int compareTypes(IType a, IType b)
	{
		// TODO implement a faster alternative to avoid useless type copying
		a = a.asParameterType();
		b = b.asParameterType();

		if (Types.isSuperType(a, b))
		{
			if (Types.isSuperType(b, a))
			{
				return 0; // same type
			}

			return 1; // type2 is more specific than type1
		}
		if (Types.isSuperType(b, a))
		{
			return -1; // type2 is less specific than type1
		}
		return 0;
	}

	public static int compareClasses(IClass a, IClass b)
	{
		if (a == b)
		{
			return 0;
		}
		if (Types.isSuperClass(a, b))
		{
			return 1;
		}
		if (Types.isSuperClass(b, a))
		{
			return -1;
		}
		return 0;
	}
}

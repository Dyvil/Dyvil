package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.type.IType;

public enum Variance
{
	INVARIANT
	{
		@Override
		public boolean checkCompatible(IType a, IType b)
		{
			return a.isSameType(b);
		}
	},
	COVARIANT
	{
		@Override
		public boolean checkCompatible(IType a, IType b)
		{
			return a.isSuperTypeOf(b);
		}
		
		@Override
		public void appendPrefix(StringBuilder builder)
		{
			builder.append('+');
		}
		
		@Override
		public void appendInfix(StringBuilder builder)
		{
			builder.append(" <: ");
		}
	},
	CONTRAVARIANT
	{
		@Override
		public boolean checkCompatible(IType a, IType b)
		{
			return b.isSuperTypeOf(a);
		}
		
		@Override
		public void appendPrefix(StringBuilder builder)
		{
			builder.append('-');
		}
		
		@Override
		public void appendInfix(StringBuilder builder)
		{
			builder.append(" >: ");
		}
	};
	
	public abstract boolean checkCompatible(IType type1, IType type2);
	
	public void appendPrefix(StringBuilder builder)
	{
	}
	
	public void appendInfix(StringBuilder builder)
	{
	}
}

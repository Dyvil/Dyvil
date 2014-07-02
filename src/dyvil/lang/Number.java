package dyvil.lang;

import dyvil.lang.primitive.IntConst;

public abstract class Number
{
	protected static int constantTableSize = 128;
	
	public abstract int intValue();
	
	public Int toInt()
	{
		return IntConst.get(this.intValue());
	}
	
	public abstract Number $inc$();
	
	public abstract Number $dec$();
	
	public abstract Number $sqr$();
	
	public abstract Number $rec$();
	
	public abstract Number $add$(int i);
	
	public abstract Number $sub$(int i);
	
	public abstract Number $mul$(int i);
	
	public abstract Number $div$(int i);
	
	public abstract Number $mod$(int i);
	
	public abstract Number $bsl$(int i);
	
	public abstract Number $bsr$(int i);
	
	public abstract Number $usr$(int i);
	
	public Number $add$(Int i)
	{
		return this.$add$(i.intValue());
	}
	
	public Number $sub$(Int i)
	{
		return this.$sub$(i.intValue());
	}
	
	public Number $mul$(Int i)
	{
		return this.$mul$(i.intValue());
	}
	
	public Number $div$(Int i)
	{
		return this.$div$(i.intValue());
	}
	
	public Number $mod$(Int i)
	{
		return this.$mod$(i.intValue());
	}
	
	public Number $bsl$(Int i)
	{
		return this.$bsl$(i.intValue());
	}
	
	public Number $bsr$(Int i)
	{
		return this.$bsr$(i.intValue());
	}
	
	public Number $usr$(Int i)
	{
		return this.$usr$(i.intValue());
	}
}

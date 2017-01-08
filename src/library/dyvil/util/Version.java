package dyvil.util;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.LiteralConvertible;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The <b>Version</b> class represents a {@link Comparable comparable} model for the <a
 * href="http://semver.org">Semantic Versioning</a> format.
 */
@LiteralConvertible.FromString
@LiteralConvertible.FromTuple
@Immutable
public final class Version implements Comparable<Version>, Serializable
{
	public enum Element
	{
		MAJOR, MINOR, PATCH, PRERELEASE, BUILD
	}

	private static final long serialVersionUID = 2514051844985966173L;

	protected static final String  INFO_FORMAT  = "[\\dA-Za-z\\-]+(?:\\.[\\dA-Za-z\\-]+)*";
	protected static final Pattern INFO_PATTERN = Pattern.compile(INFO_FORMAT);

	private static final String  FORMAT  =
		"^v?(\\d+)\\.(\\d+)\\.(\\d+)(?:-(" + INFO_FORMAT + "))?(?:\\+(" + INFO_FORMAT + "))?$";
	private static final Pattern PATTERN = Pattern.compile(Version.FORMAT);

	private final int    major;
	private final int    minor;
	private final int    patch;
	@Nullable
	private final String prerelease;
	@Nullable
	private final String build;

	@NonNull
	public static Version apply(@NonNull String version)
	{
		return new Version(version);
	}

	@Nullable
	public static Version apply(int major, int minor, int patch)
	{
		return new Version(major, minor, patch, null, null);
	}

	@Nullable
	public static Version apply(int major, int minor, int patch, String prerelease)
	{
		return new Version(major, minor, patch, prerelease, null);
	}

	@NonNull
	public static Version apply(int major, int minor, int patch, String prerelease, String build)
	{
		return new Version(major, minor, patch, prerelease, build);
	}

	public Version(@NonNull String version)
	{
		final Matcher matcher = PATTERN.matcher(version);
		if (!matcher.matches())
		{
			throw new IllegalArgumentException("'" + version + "' does not match format '" + FORMAT + "'");
		}

		this.major = Integer.parseInt(matcher.group(1));
		this.minor = Integer.parseInt(matcher.group(2));
		this.patch = Integer.parseInt(matcher.group(3));
		this.prerelease = matcher.group(4);
		this.build = matcher.group(5);
	}

	public Version(int major, int minor, int patch)
	{
		this(major, minor, patch, null, null);
	}

	public Version(int major, int minor, int patch, String prerelease)
	{
		this(major, minor, patch, prerelease, null);
	}

	public Version(int major, int minor, int patch, @Nullable String prerelease, @Nullable String build)
	{
		if (major < 0)
		{
			throw new IllegalArgumentException(Element.MAJOR + " must be positive");
		}
		if (minor < 0)
		{
			throw new IllegalArgumentException(Element.MINOR + " must be positive");
		}
		if (patch < 0)
		{
			throw new IllegalArgumentException(Element.PATCH + " must be positive");
		}
		if (prerelease != null && !INFO_PATTERN.matcher(prerelease).find())
		{
			throw new IllegalArgumentException(Element.PRERELEASE + " '" + prerelease + "'does not match format "
				                                   + INFO_FORMAT + "'");
		}
		if (build != null && !INFO_PATTERN.matcher(build).find())
		{
			throw new IllegalArgumentException(Element.BUILD + " '" + build + "' does not match format " + INFO_FORMAT
				                                   + "'");
		}

		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.prerelease = prerelease;
		this.build = build;
	}

	public int major()
	{
		return this.major;
	}

	public int minor()
	{
		return this.minor;
	}

	public int patch()
	{
		return this.patch;
	}

	@Nullable
	public String releaseInfo()
	{
		return this.prerelease;
	}

	@Nullable
	public String buildInfo()
	{
		return this.build;
	}

	public boolean isInDevelopment()
	{
		return this.major == 0;
	}

	public boolean isStable()
	{
		return this.major > 0;
	}

	@NonNull
	public Version next(Version.@Nullable Element element)
	{
		if (element == null)
		{
			throw new IllegalArgumentException("Invalid Element");
		}

		switch (element)
		{
		case MAJOR:
			return new Version(this.major + 1, 0, 0);
		case MINOR:
			return new Version(this.major, this.minor + 1, 0);
		case PATCH:
			return new Version(this.major, this.minor, this.patch + 1);
		case PRERELEASE:
			throw new IllegalArgumentException("Cannot increment prerelease information");
		case BUILD:
			throw new IllegalArgumentException("Cannot increment build information");
		default:
			return this;
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder result = new StringBuilder();
		result.append(this.major).append(".").append(this.minor).append(".").append(this.patch);
		if (this.prerelease != null && !this.prerelease.isEmpty())
		{
			result.append('-').append(this.prerelease);
		}
		if (this.build != null && !this.build.isEmpty())
		{
			result.append('+').append(this.build);
		}
		return result.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + this.major;
		result = prime * result + this.minor;
		result = prime * result + this.patch;
		result = prime * result + (this.prerelease == null ? 0 : this.prerelease.hashCode());
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}

		final Version that = (Version) obj;
		if (this.major != that.major || this.minor != that.minor || this.patch != that.patch)
		{
			return false;
		}
		if (this.prerelease == null)
		{
			if (that.prerelease != null)
			{
				return false;
			}
		}
		else if (!this.prerelease.equals(that.prerelease))
		{
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Version o)
	{
		int cmp = this.major - o.major;
		if (cmp != 0)
		{
			return cmp;
		}

		cmp = this.minor - o.minor;
		if (cmp != 0)
		{
			return cmp;
		}

		cmp = this.patch - o.patch;
		if (cmp != 0)
		{
			return cmp;
		}

		if (this.prerelease == null)
		{
			return o.prerelease != null ? -1 : 0;
		}
		else if (o.prerelease == null)
		{
			return 1;
		}
		return this.prerelease.compareTo(o.prerelease);
	}
}

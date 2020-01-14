package bdv.util;

/**
 * A bounded range with {@code minBound <= min <= max <= maxBound}.
 * <p>
 * {@code BoundedRang} is immutable.
 * <p>
 * {@link #withMin(double)}, {@link #withMinBound(double)} etc derive a new
 * {@code BoundedRange} with the given {@code min}, {@code minBound} etc, while
 * maintaining the {@code minBound <= min <= max <= maxBound} property. For
 * example, {@code this.withMin(x)}, will also update {@code max}, if
 * {@code x > this.getMax()}.
 * <p>
 * {@link #join(BoundedRange) join(other)} will derive a new
 * {@code BoundedRange}, with {@code min} the minimum of {@code this.getMin()}
 * and {@code other.getMin()} and so on.
 *
 * @author Tobias Pietzsch
 */
public final class BoundedRange
{
	private final double minBound;
	private final double maxBound;
	private final double min;
	private final double max;

	public BoundedRange( final double minBound, final double maxBound, final double min, final double max )
	{
		if ( ( minBound > min ) || ( maxBound < max ) || ( min > max ) )
			throw new IllegalArgumentException();

		this.minBound = minBound;
		this.maxBound = maxBound;
		this.min = min;
		this.max = max;
	}

	public double getMinBound()
	{
		return minBound;
	}

	public double getMaxBound()
	{
		return maxBound;
	}

	public Bounds getBounds()
	{
		return new Bounds( minBound, maxBound );
	}

	public double getMin()
	{
		return min;
	}

	public double getMax()
	{
		return max;
	}

	public BoundedRange withMax( final double newMax )
	{
		final double newMin = Math.min( min, newMax );
		final double newMinBound = Math.min( minBound, newMin );
		final double newMaxBound = Math.max( maxBound, newMax );
		return new BoundedRange( newMinBound, newMaxBound, newMin, newMax );
	}

	public BoundedRange withMin( final double newMin )
	{
		final double newMax = Math.max( max, newMin );
		final double newMinBound = Math.min( minBound, newMin );
		final double newMaxBound = Math.max( maxBound, newMax );
		return new BoundedRange( newMinBound, newMaxBound, newMin, newMax );
	}

	public BoundedRange withMaxBound( final double newMaxBound )
	{
		final double newMinBound = Math.min( minBound, newMaxBound );
		final double newMin = Math.min( Math.max( min, newMinBound ), newMaxBound );
		final double newMax = Math.min( Math.max( max, newMinBound ), newMaxBound );
		return new BoundedRange( newMinBound, newMaxBound, newMin, newMax );
	}

	public BoundedRange withMinBound( final double newMinBound )
	{
		final double newMaxBound = Math.max( maxBound, newMinBound );
		final double newMin = Math.min( Math.max( min, newMinBound ), newMaxBound );
		final double newMax = Math.min( Math.max( max, newMinBound ), newMaxBound );
		return new BoundedRange( newMinBound, newMaxBound, newMin, newMax );
	}

	public BoundedRange join( final BoundedRange other )
	{
		final double newMinBound = Math.min( minBound, other.minBound );
		final double newMaxBound = Math.max( maxBound, other.maxBound );
		final double newMin = Math.min( min, other.min );
		final double newMax = Math.max( max, other.max );
		return new BoundedRange( newMinBound, newMaxBound, newMin, newMax );
	}

	@Override
	public String toString()
	{
		return "BoundedRange[ (" + minBound + ") " + min + ", " + max + " (" + maxBound + ") ]";
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( o == null || getClass() != o.getClass() )
			return false;

		final BoundedRange that = ( BoundedRange ) o;

		if ( Double.compare( that.minBound, minBound ) != 0 )
			return false;
		if ( Double.compare( that.maxBound, maxBound ) != 0 )
			return false;
		if ( Double.compare( that.min, min ) != 0 )
			return false;
		return Double.compare( that.max, max ) == 0;
	}

	@Override
	public int hashCode()
	{
		int result;
		long temp;
		temp = Double.doubleToLongBits( minBound );
		result = ( int ) ( temp ^ ( temp >>> 32 ) );
		temp = Double.doubleToLongBits( maxBound );
		result = 31 * result + ( int ) ( temp ^ ( temp >>> 32 ) );
		temp = Double.doubleToLongBits( min );
		result = 31 * result + ( int ) ( temp ^ ( temp >>> 32 ) );
		temp = Double.doubleToLongBits( max );
		result = 31 * result + ( int ) ( temp ^ ( temp >>> 32 ) );
		return result;
	}
}


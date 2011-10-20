/*
 * Copyright (c) 2010 Marlon Hendred
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.mhendred.face4j.model;

import org.json.JSONException;
import org.json.JSONObject;

public final class Point
{
	public float x;

	public float y;

	public Point()
	{
		this.x = 0;
		this.y = 0;
	}

	public Point(Point p)
	{
		this.x = p.x;
		this.y = p.y;
	}

	public Point(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public void negate ()
	{
		x = -x;
		y = -y;
	}

	public void offset (float dx, float dy)
	{
		x += dx;
		y += dy;
	}

	public final String toString ()
	{
		return "(" + x + ", " + y + ")";
	}

	@Override
	public int hashCode ()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals (Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}

	public boolean equals (float x, float y)
	{
		return this.x == x && this.y == y;
	}

	static Point fromJson (JSONObject jObj) throws JSONException
	{
		if (jObj != null)
		{
			final Point p = new Point();

			p.x = (float) jObj.getDouble("x");
			p.y = (float) jObj.getDouble("y");

			return p;
		}

		return null;
	}
}
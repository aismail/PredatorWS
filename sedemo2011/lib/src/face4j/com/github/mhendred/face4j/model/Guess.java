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

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Guess extends Pair<String, Integer> implements Comparable<Guess>
{
	public Guess(final JSONObject jObj) throws JSONException
	{
		super();
		
		this.first  = jObj.getString("uid");
		this.second = jObj.getInt("confidence");
	}

	@Override
	public String toString ()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Guess [confidence=").append(second).append(", uid=").append(first).append("]");
		return builder.toString();
	}
	
	static List<Guess> fromJsonArray (JSONArray jArr) throws JSONException
	{
		final List<Guess> guesses = new LinkedList<Guess>();
		
		if (jArr != null)
		{
			for (int i = 0; i < jArr.length(); i++)
			{
				guesses.add(new Guess(jArr.getJSONObject(i)));
			}
		}
		
		return guesses;
	}

	/**
	 * Used for natural ordering
	 */
	@Override
	public int compareTo (Guess that)
	{
		if (this.second > that.second)
		{
			return 1; 
		}
		
		if (this.second < that.second)
		{
			return -1;
		}
		
		return 0;
	}
}
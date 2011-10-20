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

package com.github.mhendred.face4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
 
/**
 * Class to hold parameters for post requests
 * 
 * @author Marlon Hendred
 *
 */
class Parameters
{
	protected final Map<String, String> params;
	
	public Parameters()
	{
		this.params = new HashMap<String, String>();
	}
	
	/**
	 * Convenience constructor for initialize single name value pairs
	 * 
	 * @param name
	 * @param value
	 */
	public Parameters(String key, String value)
	{
		this();
		params.put(key, value);
	}

	public void put(String key, float value)
	{
		put(key, String.valueOf(value));
	}
	
	public void put(String key, boolean value)
	{
		put(key, String.valueOf(value));
	}
	
	public void put(String key, int value)
	{
		put(key, String.valueOf(value));
	}
	
	public void put (String key, String value)
	{
		if (value != null)
		{
			params.put(key, value);
		}
	}
	
	public void putAll(Map<String, String> params) 
	{
		for(String key : params.keySet())
		{
			if (params.get(key) != null)
			{
				put (key, params.get(key));
			}
		}
	}
	
	public Map<String, String> getMap()
	{
		return Collections.unmodifiableMap(params);
	}
	
	public void remove (String key)
	{
		params.remove(key);
	}
	
	public boolean isEmpty()
	{
		return params.isEmpty();
	}

	public List<NameValuePair> toPostParams()
	{
		final List<NameValuePair> list = new LinkedList<NameValuePair>();
		
		for(String key : params.keySet())
		{
			list.add(new BasicNameValuePair(key, params.get(key)));
		}
		
		return list;
	}
	
	@Override
	public String toString()
	{
		return params.toString();
	}
}
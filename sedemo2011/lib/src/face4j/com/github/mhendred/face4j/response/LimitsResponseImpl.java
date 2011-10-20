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

package com.github.mhendred.face4j.response;

import static com.github.mhendred.face4j.response.ResponseHelper.optInt;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.mhendred.face4j.exception.FaceClientException;

public class LimitsResponseImpl extends AbstractResponse implements LimitsResponse
{
	private final Date resetDate;

	private final int remaining;

	private final int limit;

	private final int used;
	
	private int namespaceLimit;
	
	private int namespaceUsed;
	
	private int namespaceRemaining;
	
	private String restTimeString;
	
	public LimitsResponseImpl(final String json) throws FaceClientException
	{
		super(json);
		
		try
		{		
			final JSONObject usage = response.getJSONObject("usage");
			namespaceRemaining = optInt(usage, "namespace_remaining");
			namespaceUsed      = optInt(usage, "namespace_used");
			namespaceLimit	   = optInt(usage, "namespace_limit");
			restTimeString = usage.getString("reset_time_text");
			resetDate	   = new Date(usage.getLong("reset_time"));
			remaining	   = usage.getInt("remaining");
			used           = usage.getInt("used");
			limit          = usage.getInt("limit");
		}
		
		catch (JSONException jex)
		{
			logger.error("Error: ", jex);
			throw new FaceClientException(jex);
		}
	}

	/* (non-Javadoc)
	 * @see face4j.response.BaseResponse#getUsed()
	 */
	@Override
	public int getUsed ()
	{
		return used;
	}

	/* (non-Javadoc)
	 * @see face4j.response.BaseResponse#getRemaining()
	 */
	@Override
	public int getRemaining ()
	{
		return remaining;
	}

	/* (non-Javadoc)
	 * @see face4j.response.BaseResponse#getLimit()
	 */
	@Override
	public int getLimit ()
	{
		return limit;
	}

	/* (non-Javadoc)
	 * @see face4j.response.BaseResponse#getRestTimeString()
	 */
	@Override
	public String getRestTimeString ()
	{
		return restTimeString;
	}

	/* (non-Javadoc)
	 * @see face4j.response.BaseResponse#getResetDate()
	 */
	@Override
	public Date getResetDate ()
	{
		return resetDate;
	}

	@Override
	public int getNamespaceLimit ()
	{
		return namespaceLimit;
	}

	@Override
	public int getNamespaceUsed ()
	{
		return namespaceUsed;
	}

	@Override
	public int getNamespaceRemaining ()
	{
		return namespaceRemaining;
	}

	@Override
	public String toString ()
	{
		return super.toString();
	}
}
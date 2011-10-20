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

import static com.github.mhendred.face4j.response.ResponseHelper.toRemovedTagList;

import java.util.List;

import org.json.JSONException;

import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.model.RemovedTag;


public final class RemoveTagResponseImpl extends AbstractResponse implements RemoveTagResponse
{	
	private final List<RemovedTag> removedTags;

	public RemoveTagResponseImpl(String json) throws FaceClientException
	{
		super(json);
		
		try
		{	
			removedTags = toRemovedTagList(response.getJSONArray("removed_tags"));
		}
		
		catch (JSONException jex)
		{
			logger.error("Error geting removed_tags: " + jex.getMessage(), jex);
			
			throw new FaceClientException(jex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.response.RemoveTagResponse#getRemovedTags()
	 */
	public List<RemovedTag> getRemovedTags ()
	{
		return removedTags;
	}
	
	public String toString()
	{
		return super.toString();
	}
}
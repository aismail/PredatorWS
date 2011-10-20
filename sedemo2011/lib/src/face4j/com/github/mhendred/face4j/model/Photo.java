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

import static com.github.mhendred.face4j.model.Face.fromJsonArray;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Photo class
 * 
 * @author Marlon Hendred
 * 
 */
public class Photo
{
	private final Logger logger;
	
	private final String url;

	private final String pid;

	private final int width;

	private final int height;

	private List<Face> tags;

	public Photo(final JSONObject jObj) throws JSONException
	{
		logger = LoggerFactory.getLogger(Photo.class);
		
		url = jObj.getString("url");
		pid = jObj.getString("pid");

		width = jObj.getInt("width");
		height = jObj.getInt("height"); 

		tags = fromJsonArray(jObj.getJSONArray("tags"));
	}
	
	

	@Override
	public String toString ()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Photo [height=").append(height)
		       .append(", pid=").append(pid)
		       .append(", url=").append(url)
		       .append(", width=")
		       .append(width).append("]")
		       .append("\ntags=").append(tags);
		
		return builder.toString();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Photo#getFaceCount()
	 */
	public int getFaceCount ()
	{
		return getFaces().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Photo#getURL()
	 */
	public String getURL ()
	{
		return this.url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Photo#getPID()
	 */
	public String getPID ()
	{
		return this.pid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Photo#getWidth()
	 */
	public float getWidth ()
	{
		return this.width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Photo#getHeight()
	 */
	public float getHeight ()
	{
		return this.height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Photo#getFaces()
	 */
	public List<Face> getFaces ()
	{
		return this.tags;
	}

	public void scaleFaceRects(float width, float height)
	{
		for (Face f : getFaces())
		{
			final Rect r = f.getRectangle();
			
			r.left   *= (width/100);
			r.right  *= (width/100);
			r.top    *= (height/100);
			r.bottom *= (height/100);
		}
	}
	public Face getFace()
	{
		try
		{
			return getFaces().get(0);
		}
		
		catch (IndexOutOfBoundsException ioob)
		{
			if (logger.isInfoEnabled())
			{
				logger.info("No faces found...");
			}
			
			return null;
		}
	}
}
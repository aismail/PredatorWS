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


public class SavedTag
{	
	private final String tid;
	
	private String detected_tid;
	
	public SavedTag (final JSONObject jObj) throws JSONException
	{
		tid = jObj.getString("tid");
		detected_tid = jObj.optString("detected_tid");
	}
	
	/* (non-Javadoc)
	 * @see com.face.api.client.model.SavedTag#getTID()
	 */
	public String getTID()
	{
		return this.tid;
	}
	
	/* (non-Javadoc)
	 * @see com.face.api.client.model.SavedTag#getDetectedTID()
	 */
	public String getDetectedTID()
	{
		return this.detected_tid;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("tid : ").append(tid)
		  .append("\n")
		  .append("detected_tid: ").append(detected_tid)
		  .append("\n")
		  .trimToSize();
		
		return sb.toString();
	}
}
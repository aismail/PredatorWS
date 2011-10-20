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

/**
 * Holds the path to the resources on api.face.com
 * 
 * @author Marlon Hendred
 *
 */
enum Api 
{
	RECOGNIZE("/faces/recognize.json", true),
	DETECT("/faces/detect.json", false),
	GROUP("/faces/group.json", true),
	TRAIN("/faces/train.json", true),
	STATUS("/faces/status.json",false),
	REMOVE_TAGS("/tags/remove.json", true),
	SAVE_TAGS("/tags/save.json", true),
	GET_TAGS("/tags/get.json", true),
	ADD_TAG("/tags/add.json", true),
	LIMITS("/account/limits.json", false),
	NAMESPACES("/account/namespaces.json", false),
	USERS("/account/users.json", false),
	FACEBOOK("/facebook/get.json", true);
	
	private final String path;
	
	private final boolean takesAuth;
	
	private Api (String path, boolean takesAuth) 
	{
		this.path = path;
		this.takesAuth = takesAuth;
	}
	
	public String getPath ()
	{
		return path;
	}
	
	public boolean takesAuth ()
	{
		return takesAuth;
	}
}
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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.exception.FaceServerException;


/**
 * Default implementation of {@link Responder} interface. It seems as though
 * face.com always returns an HTTP status code of 200 even for 404 not founds.
 * This is why there is no need to check the status line.
 * 
 * @author Marlon Hendred
 *
 */
class ResponderImpl implements Responder
{
	/**
	 * This is a slf4j logger. You can change the framework and runtime by adding a jar to
	 * your classpath
	 * 
	 *  @see http://www.slf4j.org
	 */
	private static final Logger logger = LoggerFactory.getLogger(Responder.class);
	
	/**
	 * "failure" string constant
	 */
	private static final String FAILURE = "failure";
	
	/**
	 * {@link HttpClient} for executing requests
	 */
	private final HttpClient httpClient;
	
	/**
	 * {@link HttpPost} method for {@code POST}s
	 */
	private final HttpPost postMethod;
	
	/**
	 * {@link HttpGet} method for {@code GET}s
	 */
	private final HttpGet getMethod;
	
	public ResponderImpl()
	{
		this.httpClient = new DefaultHttpClient();	
		this.postMethod = new HttpPost();
		this.getMethod  = new HttpGet();
	}

	/** 
	 * @see {@link Responder#doPost(URI, List)}
	 */
	public String doPost(final URI uri, final List<NameValuePair> params) throws FaceClientException, FaceServerException
	{		
		try
		{
			final HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			
			postMethod.setURI(uri);
			postMethod.setEntity(entity);
		
			final HttpResponse response = httpClient.execute(postMethod);	
			
			return checkResponse(response);
		}
		
		catch (IOException ioe)
		{
			logger.error("Error while POSTing to {} ", uri, ioe);
			throw new FaceClientException(ioe);
		}
	}
	
	/**
	 * @see {@link Responder#doPost(File, URI, List)}
	 */
	public String doPost(final File file, final URI uri, final List<NameValuePair> params) throws  FaceClientException, FaceServerException
	{		
		try
		{
			final MultipartEntity entity = new MultipartEntity();	
			
			if (logger.isInfoEnabled())
			{
				logger.info("Adding image entity, size: [{}] bytes", file.length());
			}
			
			entity.addPart("image", new FileBody(file));

			try 
			{
				for (NameValuePair nvp : params)
				{
					entity.addPart(nvp.getName(), new StringBody(nvp.getValue()));
				}
			}
			
			catch (UnsupportedEncodingException uee)
			{
				logger.error("Error adding entity", uee);
				throw new FaceClientException(uee);
			}
		
			postMethod.setURI(uri);
			postMethod.setEntity(entity);
			
			final long start = System.currentTimeMillis();
			final HttpResponse response = httpClient.execute(postMethod);
		
			if (logger.isDebugEnabled())
			{
				logger.debug("POST took {} (ms)", (System.currentTimeMillis() - start));
			}
			
			return checkResponse(response);
		}
		
		catch (IOException ioe)
		{
			logger.error("Error while POSTing to {} ", uri, ioe);
			throw new FaceClientException(ioe);
		}
	}
	
	/**
	 * @see {@code Responder#doGet(URI)}
	 */
	public String doGet (final URI uri) throws  FaceClientException, FaceServerException
	{
		getMethod.setURI(uri);
		
		try 
		{
			final HttpResponse response = httpClient.execute(getMethod);
			return checkResponse(response);
		}
		
		catch (IOException ioe)
		{
			logger.error("Error while POSTing to {} ", uri, ioe);
			throw new FaceClientException(ioe);
		}
	}
	
	private String checkResponse(HttpResponse httpResponse) throws FaceServerException, FaceClientException
	{
		try 
		{
			final String json = EntityUtils.toString(httpResponse.getEntity());
		
			if (json.contains(FAILURE))
			{
				final JSONObject obj = new JSONObject(json);
				final String message = obj.getString("error_message");
				final int errorCode  = obj.getInt("error_code");
				
				final FaceServerException fse = new FaceServerException(message, errorCode);
				
				if (logger.isDebugEnabled())
				{
					logger.debug("Error: ", fse);
				}
				
				throw fse;	
			}
			
			else 
			{
				if (logger.isInfoEnabled())
				{
					logger.info("SUCCESS:{} ", httpResponse.getStatusLine());
				}
				
				return json;
			}
		
		}
		
		catch (JSONException jse)
		{
			logger.error("Error parsing response", jse);
			throw new FaceClientException(jse);
		}
		
		catch (IOException ioe)
		{
			logger.error("Error parsing response", ioe);
			throw new FaceClientException(ioe);
		}
	}
}
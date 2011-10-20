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
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.exception.FaceServerException;
import com.github.mhendred.face4j.model.Namespace;
import com.github.mhendred.face4j.model.Photo;
import com.github.mhendred.face4j.model.RemovedTag;
import com.github.mhendred.face4j.model.SavedTag;
import com.github.mhendred.face4j.model.UserStatus;
import com.github.mhendred.face4j.response.GetTagsResponse;
import com.github.mhendred.face4j.response.GetTagsResponseImpl;
import com.github.mhendred.face4j.response.GroupResponse;
import com.github.mhendred.face4j.response.GroupResponseImpl;
import com.github.mhendred.face4j.response.LimitsResponse;
import com.github.mhendred.face4j.response.LimitsResponseImpl;
import com.github.mhendred.face4j.response.NamespaceResponse;
import com.github.mhendred.face4j.response.NamespaceResponseImpl;
import com.github.mhendred.face4j.response.PhotoResponse;
import com.github.mhendred.face4j.response.PhotoResponseImpl;
import com.github.mhendred.face4j.response.RemoveTagResponse;
import com.github.mhendred.face4j.response.RemoveTagResponseImpl;
import com.github.mhendred.face4j.response.SaveTagResponse;
import com.github.mhendred.face4j.response.SaveTagResponseImpl;
import com.github.mhendred.face4j.response.StatusResponse;
import com.github.mhendred.face4j.response.StatusResponseImpl;
import com.github.mhendred.face4j.response.TrainResponse;
import com.github.mhendred.face4j.response.TrainResponseImpl;
import com.github.mhendred.face4j.response.UsersResponse;
import com.github.mhendred.face4j.response.UsersResponseImpl;

/**
 * Default implementation of {@link FaceClient} interface
 * 
 * @author Marlon Hendred 
 * 
 * @see {@link FaceClient}  
 * @see <a href="http://developers.face.com/docs/">Developer's page</a>
 */
public class DefaultFaceClient implements FaceClient
{	
	/**
	 * Default API end point @TODO: set from properties
	 */
	private static final String API_ENDPOINT = "http://api.face.com";
	
	/**
	 * Handles {@code POST}s to the face.com endpoint
	 */
	private final Responder http;
	
	/**
	 * Facebook and twitter credentials
	 * 
	 * @TODO: Implement facebook OAuth dance
	 */
	private final Credentials creds;
	
	/**
	 * Parameters that are required for every call
	 */
	private final Parameters reqd;
	
	/**
	 * Base {@link URI} endpoint
	 */
	private final URI baseURI;

	/**
	 * Detector mode
	 */
	private boolean isAggressive;
	/**
	 * Convenience constructor with default {@link Responder} implementation
	 * 
	 * @see {@link DefaultResponder}
	 * @see {@link #DefaultFaceClient(String, String, Responder)}
	 */
	public DefaultFaceClient (final String apiKey, final String apiSecret)
	{
		this (apiKey, apiSecret, new ResponderImpl());
	}
	
	/**
	 * Constructs a Face.com API client pointing to {@code host}. You need to obtain an API key/secret pair.
	 * You can get an API key/secret from face.com 
	 * 
	 * @param apiKey Your aplication's API key
	 * @param apiSecret Your applications API secret
	 *  
	 * @see {@link Responder}
	 * @see <a href="http://developers.face.com/docs/">Developer's page</a> for information on obtaining an
	 * 		API key/secret
	 */
	public DefaultFaceClient (final String apiKey, final String apiSecret, final Responder responder)
	{
		this.baseURI = URI.create(API_ENDPOINT);
		this.http    = responder;
		this.creds   = new Credentials();
		this.reqd	 = new Parameters();
		
		reqd.put("api_key", apiKey);
		reqd.put("api_secret", apiSecret);
		
		setAggressive(false);
	}	
	
	/**
	 * @see {@link FaceClient#removeTags(String)}
	 */
	@Override
	public List<RemovedTag> removeTags (final String tids) throws FaceClientException, FaceServerException
	{
		Validate.notEmpty(tids, "Tag ids cannot be empty");
		
		final Parameters params = new Parameters("tids", tids);
		final String json = executePost(Api.REMOVE_TAGS, params);
		final RemoveTagResponse response = new RemoveTagResponseImpl(json);
		
		return response.getRemovedTags();	
	}
	
	/**
	 * @see {@link FaceClient#train(String)}
	 */
	@Override
	public TrainResponse train (final String uids) throws FaceClientException, FaceServerException
	{
		final Parameters params = new Parameters("uids", uids);		
		final String json = executePost(Api.TRAIN, params);
		final TrainResponse response = new TrainResponseImpl(json);
		
		return response;
	}
	
	/**
	 * @see {@link FaceClient#addTag(String, float, float, int, int, String, String, String)}
	 */
	@Override
	public void addTag (
			final String url, 
			final float x, 
			final float y,
			final int width, 
			final int height, 
			final String uid, 
			final String label, 
			final String taggerId) 
		throws FaceClientException, FaceServerException
	{
		Validate.notNull(uid, "UID cannot be null");
		
		final Parameters params = new Parameters();
		
		params.put("x", x);
		params.put("y", y);
		params.put("width", width);
		params.put("height", height);
		params.put("tagger_id", taggerId);
		params.put("url", url);
		params.put("uid", uid);
		params.put("label", label);

		// No response
		executePost(Api.ADD_TAG, params);
	}
	
	/**
	 * @see {@link FaceClient#getTags(String, String, String, String, boolean, int)}
	 */
	@Override
	public List<Photo> getTags (
			final String urls,
			final String uids,
			final String order,
			final String filter,
			final boolean together,
			final int limit)
	throws FaceClientException, FaceServerException
	{
		return getTags(null, urls, uids, order, filter, together, limit);
	}
	
	/**
	 * @see {@link FaceClient#getTags(String, String, String, String, String, boolean, int)}
	 */
	@Override
	public List<Photo> getTags (
			final String pids, 
			final String urls, 
			final String uids, 
			final String order,
			final String filter,
			final boolean together,
			final int limit) 
		throws FaceClientException, FaceServerException
	{
		final Parameters params = new Parameters();
		
		params.put("pids", pids);
		params.put("urls", urls);
		params.put("uids", uids);
		params.put("order", order);
		params.put("filter", filter);
		params.put("together", together);
		params.put("limit", limit);

		final String json = executePost(Api.GET_TAGS, params);
		final GetTagsResponse response = new GetTagsResponseImpl(json);
		
		return response.getPhotos();
	}

	/**
	 * @see {@list FaceClient#saveTags(String, String, String)}
	 */
	@Override
	public List<SavedTag> saveTags (final String tids, final String uid, final String label) 
		throws FaceClientException, FaceServerException
	{
		Validate.notEmpty(uid, "User IDs cannot be null");
		Validate.notEmpty(tids, "Tag IDs cannot be null");
		
		final Parameters params = new Parameters("tids", tids);
		
		params.put("uid", uid);
		params.put("label", label);
		
		final String json = executePost(Api.SAVE_TAGS, params);
		final SaveTagResponse response = new SaveTagResponseImpl(json);

		return response.getSavedTags();
	}
	
	/**
	 * @see {@link FaceClient#recognize(URL, String)}
	 */
	@Override
	public Photo recognize (final File imageFile, final String uids) throws FaceClientException, FaceServerException
	{	
		Validate.notNull(imageFile, "File is null");
		Validate.isTrue(imageFile.exists(), "File does not exist!");
		Validate.notEmpty(uids, "User IDs cannot be null");
			
		final Parameters params = new Parameters("uids", uids);
		final String json =  executePost(imageFile, Api.RECOGNIZE, params);
		final PhotoResponse response = new PhotoResponseImpl(json);		
		
		return response.getPhoto();
	}
	
	/**
	 * @see {@link FaceClient#recognize(String, String)}
	 */
	@Override
	public List<Photo> recognize (final String urls, final String uids) throws FaceClientException, FaceServerException
	{
		Validate.notEmpty(urls, "URLs cant be empty");
		Validate.notEmpty(uids, "User IDs can't be empty");
				
		final Parameters params = new Parameters("uids", uids);
		
		params.put("urls", urls);

		final String json = executePost(Api.RECOGNIZE, params);
		final PhotoResponse response = new PhotoResponseImpl(json);
				
		return response.getPhotos();
	}
	
	/**
h	 * @see {@link FaceClient#detect(URL)}
	 */
	@Override
	public Photo detect (final File imageFile) throws FaceClientException, FaceServerException
	{	
		Validate.notNull(imageFile, "File is null");
		Validate.isTrue(imageFile.exists(), "File doesn't exist!");
		
		final String json = executePost(imageFile, Api.DETECT, new Parameters());
		final PhotoResponse response = new PhotoResponseImpl(json);
		
		return response.getPhoto();
	}

	/**
	 * @see {@link FaceClient#detect(String)}
	 */
	@Override
	public List<Photo> detect (final String urls) throws FaceClientException, FaceServerException
	{
		Validate.notNull(urls, "URLs cannot be null");
		
		final Parameters params = new Parameters();
		
		params.put("urls", urls);
		
		final String json = executePost(Api.DETECT, params);
		final PhotoResponse response = new PhotoResponseImpl(json);
		
		return response.getPhotos();
	}

	/**
	 * @see {@link FaceClient#status(String)}
	 */
	@Override
	public List<UserStatus> status (final String uids) throws FaceClientException, FaceServerException
	{
		Validate.notEmpty(uids, "UIDs cant be empty");
				
		final Parameters params = new Parameters();

		params.put("uids", uids);
		
		final String json = executePost(Api.STATUS, params);
		final StatusResponse response = new StatusResponseImpl(json);
			
		return response.getTrainingStatus();
	}
	
	/**
	 * @see {@link FaceClient#facebookGet(String)}
	 */
	@Override
	public List<Photo> facebookGet (final String uids) throws FaceClientException, FaceServerException
	{
		Validate.notEmpty(uids, "User IDs cannot be empty");
		
		final Parameters params = new Parameters();

		params.put("uids", uids);
		
		final String json = executePost(Api.FACEBOOK, params);
		final PhotoResponse response = new PhotoResponseImpl(json);
						
		return response.getPhotos();	
	}

	/**
	 * @see {@link FaceClient#group(String, String)}
	 */
	@Override
	public GroupResponse group(String urls, String uids) throws FaceClientException, FaceServerException
	{
		Validate.notEmpty(urls, "URLs cannot be empty");
		Validate.notEmpty(uids, "UIDs cannot be empty");
		
		final Parameters params = new Parameters();
		
		params.put("uids", uids);
		params.put("urls", urls);

		final String json = executePost(Api.GROUP, params);
		final GroupResponse response = new GroupResponseImpl(json);
						
		return response;
	}

	/**
	 * @see {@link FaceClient#group(File, String)}
	 */
	@Override
	public GroupResponse group (File imageFile, String uids) throws FaceClientException, FaceServerException 
	{
		Validate.isTrue(imageFile.exists(), "File does not exist");
		Validate.notEmpty(uids, "UIDs cannot be empty");
		
		final Parameters params = new Parameters();
		
		params.put("uids", uids);
		
		final String json = executePost(imageFile, Api.GROUP, params);
		final GroupResponse response = new GroupResponseImpl(json);
			
		return response;
	}

	/** 
	 * @see {@link FaceClient#users(String)}
	 */
	@Override
	public UsersResponse users (String namespaces) throws FaceClientException, FaceServerException
	{
		Validate.notEmpty(namespaces, "Must supply namespace(s)");
		
		final Parameters params = new Parameters();
		params.put("namespaces", namespaces);
		
		final String json = executePost(Api.USERS, params);
		final UsersResponse response = new UsersResponseImpl(json, namespaces);
		
		return response;
	}
	
	/**
	 * @see {@link FaceClient#usage()}
	 */
	@Override
	public LimitsResponse getLimits () throws FaceClientException, FaceServerException
	{
		final String json = executePost(Api.LIMITS, new Parameters());
		final LimitsResponse response = new LimitsResponseImpl(json);
		
		return response;
	}
	
	/**
	 * @see {@link FaceClient#namespaces()}
	 */
	@Override
	public List<Namespace> namespaces() throws FaceClientException, FaceServerException
	{
		final String json = executePost(Api.NAMESPACES, new Parameters());
		final NamespaceResponse response = new NamespaceResponseImpl(json);
		
		return response.getNamespaces();
	}
	
	/**
	 * @see {@link FaceClient#getNamespace(String)
	 */
	@Override
	public Namespace getNamespace(String namespace) throws FaceClientException, FaceServerException
	{
		for (Namespace ns : namespaces())
		{
			if (ns.getName().equals(namespace))
			{
				return ns;
			}
		}
		
		return null;
	}
	
	/**
	 * @see {@link FaceClient#setFacebookOauth2(String, String)}
	 */
	@Override
	public void setFacebookOauth2(final String fbUserId, final String oauthToken)
	{
		creds.put("fb_user", fbUserId);
		creds.put("fb_oauth_token", oauthToken);
	}
	
	/**
	 * @see {@link FaceClient#setTwitterOauth(String, String, String)}
	 */
	@Override
	public void setTwitterOauth(final String oauthUser, final String oauthSecret, final String oauthToken)
	{
		creds.put("twitter_oauth_user", oauthUser);
		creds.put("twitter_oauth_secret", oauthSecret);
		creds.put("twitter_oauth_token", oauthToken);
	}
	
	/**
	 * @see {@link FaceClient#clearFacebookCreds()}
	 */
	@Override
	public void clearFacebookCreds()
	{
		creds.remove("fb_oauth_token");
		creds.remove("fb_user");
	}
	
	/**
	 * @see {@link FaceClient#clearTwitterCreds()}
	 */
	@Override
	public void clearTwitterCreds()
	{
		creds.remove("twitter_oauth_user");
		creds.remove("twitter_oauth_secret");
		creds.remove("twitter_oauth_token");
	}
	
	/**
	 * @see {@link FaceClient#setAggressive(boolean)} 
	 */
	@Override
	public void setAggressive(final boolean isAggressive)
	{
		this.isAggressive = isAggressive;
		
		reqd.put("detector", isAggressive ? "Aggressive" : "Normal");
	}
	
	/**
	 * @see {@link FaceClient#isAggressive()}
	 */
	@Override
	public boolean isAggressive()
	{
		return isAggressive;	
	}
	
	private String executePost(Api api, Parameters params) throws FaceClientException, FaceServerException
	{
		return executePost(null, api, params);
	}
	
	private String executePost(File file, Api api, Parameters params) throws FaceClientException, FaceServerException
	{
		final URI uri = baseURI.resolve(api.getPath());
		
		params.putAll(reqd.getMap());

		if (api.takesAuth())
		{
			params.put("user_auth", creds.getAuthString());
		}
		
		if (file != null)
		{
			return http.doPost(file, uri, params.toPostParams());
		}
		
		else
		{
			return http.doPost(uri, params.toPostParams());
		}
	}
}
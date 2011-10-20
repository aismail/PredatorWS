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
import java.net.URL;
import java.util.List;

import com.github.mhendred.face4j.exception.FaceClientException;
import com.github.mhendred.face4j.exception.FaceServerException;
import com.github.mhendred.face4j.model.Face;
import com.github.mhendred.face4j.model.Namespace;
import com.github.mhendred.face4j.model.Photo;
import com.github.mhendred.face4j.model.RemovedTag;
import com.github.mhendred.face4j.model.SavedTag;
import com.github.mhendred.face4j.model.UserStatus;
import com.github.mhendred.face4j.response.GroupResponse;
import com.github.mhendred.face4j.response.LimitsResponse;
import com.github.mhendred.face4j.response.TrainResponse;
import com.github.mhendred.face4j.response.UsersResponse;

/**
 * Interface which describes how a class interacting with the face.com API would behave. This interface
 * currently does not support callbacks or tagging passwords. If the {@link AndroidFaceClient} implementation 
 * does not meet your needs, simply extend and re-implement
 *  
 * @author Marlon Hendred 
 * 
 * @see <a href="http://developers.face.com/docs/">Developer's page</a>
 */
public interface FaceClient 
{	
	/**
	 * Returns tags for detected faces in a single photo, with geometric information of the tag, 
	 * eyes, nose and mouth, as well as the gender, glasses, and smiling attributes.
	 * 
	 * @param imageUrl {@link File} of the image to be uploaded to face.com for detection
	 * 
	 * @return {@link Photo} object encapsulating response
	 * 
	 * @throws FaceServerException if there was a server side error
	 * @throws FaceClientException if there was a client side error
	 * 
	 */
	public Photo detect (final File imageFile) throws FaceClientException, FaceServerException;
	
	/**
	 * Convenience method for batch face detection
	 * 
	 * @param urls Comma delimited {@code String} of urls
	 * 
	 * @return {@code List}<{@link Photo}>
	 * 
	 * @throws FaceClientException
	 * @throws FaceServerException
	 * 
	 * 
	 * @see {@link #detect(String, String, URL)
	 */
	public List<Photo> detect (final String urls) throws FaceClientException, FaceServerException;
	
	
	/**
	 * Convenience method for detecting faces in an image file
	 * 
	 * @param imageUrl {@link File} of the image to be uploaded to face.com for detection
	 * @param uids comma delimited {@code String} of user IDs to search for in the photos passed in the request
	 * 
	 * @return {@link Photo} 
	 * 
	 * @throws FaceServerException
	 * @throws FaceClientException
	 * 
	 * 
	 * @see {@link #recognize(String, String)}
	 */
	public Photo recognize (final File imageFile, final String uids) throws FaceClientException, FaceServerException;
	
	/**
	 * Convenience method for recognizing UIDs in {@code URL}s
	 * 
	 * @param urls Comma delimited {@code String} of image URLs
	 * @param uids Comma delimited {@code String} of UIDs to search for in the photos passed in the request
	 *
	 * @return {@code List}<{@link Photo}>
	 *
	 * @throws FaceClientException
	 * @throws FaceServerException
	 * 
	 * 
	 * @see {@link #recognize(String, String, String, String)}
	 */
	public List<Photo> recognize (final String urls, final String uids) throws FaceClientException, FaceServerException;
	
	/**
	 * Attempts to detect, group, and optionally recognize one or more user IDs' faces in a image. Useful when dealing 
	 * with files.
	 * 
	 * @param imageUrl {@link File} of the image to be uploaded to face.com for detection
	 * @param uids comma delimited {@code String} of user IDs to search for in the photos passed in the request
	 * 
	 * @return {@link Photo} 
	 * 
	 * @throws FaceServerException
	 * @throws FaceClientException
	 * 
	 * 
	 * @see {@link #group(String, String)}
	 */
	public GroupResponse group (final File imageFile, final String uids) throws FaceClientException, FaceServerException;
	
	/**
	 * Attempts to detect, group, and optionally recognize one or more user IDs' faces in a image url.
	 * 
	 * @param urls Comma delimited {@code String} of image URLs
	 * @param uids Comma delimited {@code String} of UIDs to search for in the photos passed in the request
	 *
	 * @return {@code List}<{@link Photo}>
	 *
	 * @throws FaceClientException
	 * @throws FaceServerException
	 * 
	 * 
	 * @see {@link #recognize(String, String, String, String)}
	 */
	public GroupResponse group (final String urls, final String uids) throws FaceClientException, FaceServerException;
	
	/**
 	 * Calls the training procedure for the specified UIDs, and reports back changes.
	 * 
	 * @param uids Comma separated list of UIDs to train the recognizer on
	 * 
	 * @return a {@link TrainReponse} with access to {@link UserStatus} objects
	 * 
	 * @throws FaceServerException 
	 * @throws FaceClientException
	 * 
	 * 
	 * @see {@link #train(String, String, String, String)}
	 */
	public TrainResponse train (final String uid) throws FaceClientException, FaceServerException;
	
	/**
	 * Reports training set status for the specified UIDs.
	 * 
	 * @param uids The comma separated user IDs to get the status for.
	 * 
	 * @return {@code List} of {@link UserStatus} object with status information
	 * 
	 * @throws FaceServerException if there was a server side error
	 * @throws FaceClientException if there was a client side error
	 * 
	 */
	public List<UserStatus> status (final String uids) throws FaceClientException, FaceServerException;
	
	/**
	 * Returns saved tags in one or more photos, or for the specified User ID(s). 
	 * This method also accepts multiple filters for finding tags corresponding to a more 
	 * specific criteria such as front-facing, recent, or where two or more users 
	 * appear together in same photos.
	 * 
	 * @param pids Photo IDs to fetch {@link Face} tags from (as returned by detect(...) and recognize(...)).
	 * @param urls Comma separated list of urls
	 * @param uids A comma separated list of user IDs to fetch.
	 * @param order Specify 'recent' for latest tags, and 'random' to randomly select tags. Default: 'recent'
	 * @param filter Filter results based on attributes.
	 * @param limit Set maximum limit for number of tags to return. Default: 5
	 * @param together when providing multiple uids, return only photos where ALL uids appear together in the photo(s). Default: false
	 * 
	 * @return A {@code List} of {@link Photo} objects in which the uid has {@link Face} tags.
	 * 
	 * @throws FaceServerException
	 * @throws FaceClientException
	 * 
	 * 
	 * @see <a href="http://developers.face.com/docs/api/tags-save/">tags.get</a> page for more information on this call
	 **/
	public List<Photo> getTags (
			final String pids, 
			final String urls, 
			final String uids, 
			final String order,
			final String filter,
			final boolean together,
			final int limit) 
		throws FaceClientException, FaceServerException;
	
	
	/**
	 * Convenience method for getting tags in files
	 * 
	 * 
	 *@see {@link #getTags(String, String, String, String, String, boolean, int)
	 *
	 **/
	public List<Photo> getTags (
			final String urls, 
			final String uids, 
			final String order,
			final String filter,
			final boolean together,
			final int limit) 
		throws FaceClientException, FaceServerException;
	
	/**
	 * Add a manual face tag to a photo. Use this method to add face tags where those were not detected
	 * for completeness of your service. Manual tags are treated like automatic tags, except they are not 
	 * used to train the system how a user looks like. 
	 * 
	 * @param url The {@code String} of the image to add the tag to. Protocol must be HTTP
	 * @param x The horizontal center position of the {@link Face} tag, as a percentage from 0 to 100, from the left of the photo
	 * @param y The vertical center position of the {@link Face} tag, as a percentage from 0 to 100, from the top of the photo
	 * @param width Width of the {@link Face} tag, as a percentage from 0 to 100 (height is currently the same as the width)
	 * @param uid The ID of the user being tagged
	 * @param label The label of the tag (usually first and last name)
	 * @param taggerId The ID of the user making the tag
	 * 
	 * @throws FaceServerException
	 * @throws FaceClientException
	 * 
	 * 
	 * @see this <a href="http://developers.face.com/docs/api/tags-add/">tags.add</a> page for more information
	 */
	public void addTag (
			final String url, 
			final float x, 
			final float y,
			final int width, 
			final int height, 
			final String uid, 
			final String label, 
			final String taggerId) 
		throws FaceClientException, FaceServerException;
	
	/**
	 * Convenience method for saving tags for a given user without a callback or password
	 * 
	 * @param tids One or more tag ids to associate with the passed uid. 
	 * 		  The tag id is a reference field in the response of faces.detect and faces.recognize methods	
	 * @param uid The user ID of the user being tagged.
	 * @param label Optional display name of the user. (usually First and Last name)
	 * 
	 * @return A {@code List}<{@link SavedTag}>
	 * 
	 * @throws FaceServerException if there was a server side error
	 * @throws FaceClientException if there was a client side error
	 * 
	 * @throws FaceClientParseException 
	 * 
	 * @see {@link #saveTags(String, String, String, String, String)
	 */
	public List<SavedTag> saveTags(final String tids, final String uid, final String label) throws FaceClientException, FaceServerException;
	
	/**
	 * Remove a previously saved {@link Face} tags from a photo.
	 * 
	 * @param tids Tag ids to remove, comma delimited
	 * 
	 * @return a {@code List} of {@link RemoveTag} responses
	 * 
	 * @throws FaceServerException if there was a server side error
	 * @throws FaceClientException if there was a client side error
	 * 
	 * 
	 * @see this <a href="http://developers.face.com/docs/api/tags-remove/">tags.remove</a> page for more information
	 * 
	 */
	public List<RemovedTag> removeTags (final String tids) throws FaceClientException, FaceServerException;

	/**
	 * Returns facebook tags for one or more specified User IDs
	 * 
	 * @param uids facebook uids to get tags for
	 * 
	 * @return {@code List}<{@code Photo}>
	 * 
	 * @throws FaceServerException
	 * @throws FaceClientException
	 * 
	 */
	public List<Photo> facebookGet (final String uids) throws FaceClientException, FaceServerException;
	
	/**
	 * Returns a list of users for specified namespaces
	 * 
	 * @param namespaces Comma delimited {@link String} of namespaces to get users for
	 * 
	 * @throws FaceServerException
	 * @throws FaceClientException
	 */
	public UsersResponse users (String namespaces) throws FaceClientException, FaceServerException;
	
	/**
	 * Returns usage statistics
	 * 
	 * @throws FaceServerException
	 * @throws FaceClientException
	 */
	public LimitsResponse getLimits () throws FaceClientException, FaceServerException;
	
	/**
	 * Get a {@code List<{@link Namespace}>} associated with your API key
	 * 
	 * @throws FaceServerException
	 * @throws FaceClientException
	 */
	public List<Namespace> namespaces() throws FaceClientException, FaceServerException;
	
	/**
	 * Get a stats for a particular {@Link Namespace}
	 * 
	 * @throws FaceServerException
	 * @throws FaceClientException
	 */
	public Namespace getNamespace(String namespace) throws FaceClientException, FaceServerException;
	
	/**
	 * Set up facebook credentials
	 * 
	 * @param fbUserId the current facebook user
	 * @param oauthToken facebook OAuth2 token
	 */
	public void setFacebookOauth2(final String fbUserId, final String oauth2Token);
	
	/**
	 * Sets up Twitter OAuth credentials. This is only required when making api calls
	 * on user ids in the twitter namespace.
	 * 
	 * @param oauthUser
	 * @param oauthSecret
	 * @param oauthToken
	 * 
	 * @see <a href="http://developers.face.com/docs/auth/">Auth</a> page for more information.
	 */
	public void setTwitterOauth(final String oauthUser, final String oauthSecret, final String oauthToken);
	
	/**
	 * Clears out the facebook credentials. Calls on uids in this namespace will
	 * no longer work
	 */
	public void clearFacebookCreds();
	
	/**
	 * Clears out the twitter credentials. Calls on uids in this namespace will
	 * no longer work
	 */
	public void clearTwitterCreds();
	
	/**
	 * Set the face detector work mode: Normal (default) or Aggressive. Aggressive mode may 
	 * find a bit more faces, and is also slower. Usage of an "Aggressive" detector counts as two 
	 * Normal detections.
	 * 
	 * @param isAggressive 
	 * */
	public void setAggressive(final boolean isAggressive);
	
	/**
	 * Return the state of the detector
	 */
	public boolean isAggressive();
}
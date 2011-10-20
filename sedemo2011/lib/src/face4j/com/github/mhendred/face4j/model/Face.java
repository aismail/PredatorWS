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

import static com.github.mhendred.face4j.model.Point.fromJson;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Holds "Tag" JSON object
 * 
 * @author mhendred
 * 
 */
public class Face
{		
	private final String tid;

	private String label;

	private final boolean confirmed;

	private final boolean manual;

	private final float width;

	private final float height;

	private final int faceConfidence;

	private boolean smiling;

	private boolean glasses;

	private Gender gender;

	private final List<Guess> guesses;

	private final Point center;

	private final Point leftEye;

	private final Point rightEye;

	private final Point mouthLeft;

	private final Point mouthRight;

	private final Point mouthCenter;

	private final Point leftEar;
	
	private final Point rightEar;
	
	private final Point chin;
	
	private final Point nose;

	private final Rect faceRect;

	private final int threshold;
	
	private final float yaw;
	
	private final float roll;
	
	private final float pitch;

	public Face(JSONObject jObj) throws JSONException
	{
		tid = jObj.getString("tid");
		label = jObj.optString("label");

		confirmed = jObj.getBoolean("confirmed");
		manual = jObj.getBoolean("manual");
		
		width  = (float) jObj.getDouble("width");
		height = (float) jObj.getDouble("height");
		
		yaw   = (float) jObj.getDouble("yaw");
		roll  = (float) jObj.getDouble("roll");
		pitch = (float) jObj.getDouble("pitch");
		
		threshold = jObj.optInt("threshold");
		
		center = fromJson(jObj.optJSONObject("center"));
		
		leftEye  = fromJson(jObj.optJSONObject("eye_left"));
		rightEye = fromJson(jObj.optJSONObject("eye_right"));
		
		leftEar  = fromJson(jObj.optJSONObject("ear_left"));
		rightEar = fromJson(jObj.optJSONObject("ear_right"));
		
		chin = fromJson(jObj.optJSONObject("chin"));
		
		mouthCenter = fromJson(jObj.optJSONObject("mouth_center"));
		mouthRight  = fromJson(jObj.optJSONObject("mouth_right"));
		mouthLeft   = fromJson(jObj.optJSONObject("mouth_left"));
		
		nose = fromJson(jObj.optJSONObject("nose"));
		
		guesses = Guess.fromJsonArray(jObj.optJSONArray("uids"));
		
		// Attributes
		jObj = jObj.getJSONObject("attributes");

		if (jObj.has("smiling"))
			smiling = jObj.getJSONObject("smiling").getBoolean("value");

		if (jObj.has("glasses"))
			glasses = jObj.getJSONObject("glasses").getBoolean("value");

		if (jObj.has("gender"))
			gender = Gender.valueOf(jObj.getJSONObject("gender").getString("value"));

		faceConfidence = jObj.getJSONObject("face").getInt("confidence");
		
		faceRect = new Rect(center, width, height);		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getUids()
	 */
	public List<Guess> getGuesses ()
	{
		return guesses;
	}

	/**
	 * @return the {@link Guess} with the highest confidence for this face
	 */
	public Guess getGuess ()
	{
		try 
		{
			return Collections.max(guesses);
		}
		
		catch (NoSuchElementException nsee)
		{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getWidth()
	 */
	public double getWidth ()
	{
		return width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getHeight()
	 */
	public double getHeight ()
	{
		return height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getLabel()
	 */
	public String getLabel ()
	{
		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getTID()
	 */
	public String getTID ()
	{
		return tid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getThreshHold()
	 */
	public int getThreshHold ()
	{
		return threshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#isConfirmed()
	 */
	public boolean isConfirmed ()
	{
		return confirmed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#isManual()
	 */
	public boolean isManual ()
	{
		return manual;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getCenter()
	 */
	public Point getCenter ()
	{
		return center;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getLeftEye()
	 */
	public Point getLeftEye ()
	{
		return leftEye;
	}

	public Point getLeftEar ()
	{
		return leftEar;
	}
	
	public Point getRightEar ()
	{
		return rightEar;
	}
	
	public Point getChin ()
	{
		return chin;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getRightEye()
	 */
	public Point getRightEye ()
	{
		return rightEye;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getMouthCenter()
	 */
	public Point getMouthCenter ()
	{
		return mouthCenter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getMouthRight()
	 */
	public Point getMouthRight ()
	{
		return mouthRight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getMouthLeft()
	 */
	public Point getMouthLeft ()
	{
		return mouthLeft;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#isFace()
	 */
	public boolean isFace ()
	{
		return faceConfidence > 50;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#isWearingGlasses()
	 */
	public boolean isWearingGlasses ()
	{
		return glasses;
	}

	public float getYaw ()
	{
		return yaw;
	}

	public float getRoll ()
	{
		return roll;
	}

	public float getPitch ()
	{
		return pitch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#isSmiling()
	 */
	public boolean isSmiling ()
	{
		return smiling;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getGender()
	 */
	public Gender getGender ()
	{
		return gender;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.face.api.client.model.Face#getNose()
	 */
	public Point getNose ()
	{
		return nose;
	}

	public Rect getRectangle ()
	{
		return faceRect;
	}
	
	public void setLabel (final String label)
	{
		this.label = label;
	}
	
	static List<Face> fromJsonArray (JSONArray jArr) throws JSONException
	{
		final List<Face> faces = new LinkedList<Face>();
		
		for (int i = 0; i < jArr.length(); i++)
		{
			faces.add(new Face(jArr.getJSONObject(i)));
		}
		
		return faces;
	}

	@Override
	public String toString ()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Face [center=").append(center)
			   .append(", chin=").append(chin)
			   .append(", confirmed=").append(confirmed)
			   .append(", faceConfidence=").append(faceConfidence)
			   .append(", faceRect=").append(faceRect)
			   .append(", gender=").append(gender)
			   .append(", glasses=").append(glasses)
			   .append(", guesses=").append(guesses)
			   .append(", height=").append(height)
			   .append(", label=").append(label)
			   .append(", leftEar=").append(leftEar)
			   .append(", leftEye=").append(leftEye)
			   .append(", manual=").append(manual)
			   .append(", mouthCenter=").append(mouthCenter)
			   .append(", mouthLeft=").append(mouthLeft)
			   .append(", mouthRight=").append(mouthRight)
			   .append(", nose=").append(nose)
			   .append(", pitch=").append(pitch)
			   .append(", rightEar=").append(rightEar)
			   .append(", rightEye=").append(rightEye)
			   .append(", roll=").append(roll)
			   .append(", smiling=").append(smiling)
			   .append(", threshold=").append(threshold)
			   .append(", tid=").append(tid)
			   .append(", width=").append(width)
			   .append(", yaw=").append(yaw)
			   .append("]");
		
		return builder.toString();
	}
}
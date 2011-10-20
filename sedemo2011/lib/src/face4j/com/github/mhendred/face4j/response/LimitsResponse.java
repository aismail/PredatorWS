package com.github.mhendred.face4j.response;

import java.util.Date;

public interface LimitsResponse
{

	public int getUsed ();

	public int getRemaining ();

	public int getLimit ();

	public int getNamespaceLimit();
	
	public int getNamespaceUsed();
	
	public int getNamespaceRemaining();
	
	public String getRestTimeString ();

	public Date getResetDate ();

}
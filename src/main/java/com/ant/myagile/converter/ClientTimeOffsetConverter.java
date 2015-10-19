package com.ant.myagile.converter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.ant.myagile.model.History;
import com.ant.myagile.model.RetrospectiveResult;

public class ClientTimeOffsetConverter {
	/**
	 * get TimeOffset of Client from Cookie
	 * @param request
	 * @return timeOffset (minute)
	 */
	public static int getTimeOffset() {
		HttpServletRequest request = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();
		Cookie[] cookies = request.getCookies();
		String timeOffset = null;
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("TIMEZONE_COOKIE")) {
					timeOffset = cookie.getValue();
					break;
				}
			}
		}
		return Integer.parseInt(timeOffset);
	}
	/**
	 * calculate different time between server and client
	 * @return time (milliseconds)
	 */
	public static long calculateDifferentTimeBetweenServerAndClient() {
		Calendar cal = Calendar.getInstance();
		/* timeOffsetServer GTM-7 => -7*3600*1000 GMT+7 => +7*3600*1000*/
		long timeOffsetServer = (cal.get(Calendar.ZONE_OFFSET) + cal
				.get(Calendar.DST_OFFSET));
		/* change timeOffsetClient from minutes to miliseconds */
		/* timeOffsetClient GTM-7 => +7*3600*1000 GMT+7 => -7*3600*1000*/
		long timeOffsetClient = (getTimeOffset()*60*1000);
		long result =timeOffsetClient + timeOffsetServer; 
		return result;
	}

	/**
	 * change time of list has History type to Client time
	 * @param historyServerTime (list History)
	 */
	public static void transformServerTimeToClientTime(List<History> historyServerTime){
		for(int i=0; i<historyServerTime.size();i++){
			historyServerTime.get(i).setCreatedOn(convertToClientDate(historyServerTime.get(i).getCreatedOn()));
		}
	}
	
	/**
	 * change time of list has RetrospectiveResult to Client time
	 * @param retroResult (list RetrospectiveResult)
	 */
	public static void transformServerTimeToClientTimeretroResult(List<RetrospectiveResult> retroResult){
		for(int i=0; i<retroResult.size();i++){
			retroResult.get(i).setDate(convertToClientDate(retroResult.get(i).getDate()));
		}
	}
	
	/**
	 * change time of Issue History to Client time
	 * @param historyOfIssue (Object)
	 * @param size of Object
	 */
	public static void setOffsetTimeForIssueHistory(List<Object> historyOfIssue, int size){
		for(int i = 0; i<size;i++){
			Object[] historyObject = (Object[]) historyOfIssue.get(i);
			History history = (History) historyObject[0];
			history.setCreatedOn(convertToClientDate(history.getCreatedOn()));
		}
	}
	/**
	 * convert from Date server to Date client (both Date type)
	 * @param dateServer
	 * @return dateClient
	 */
	public static Date convertToClientDate(Date dateServer) {
		long secondsInClient = dateServer.getTime() - calculateDifferentTimeBetweenServerAndClient();
		return new Date(secondsInClient);
	}
}

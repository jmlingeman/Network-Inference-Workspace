// SBEAMSClient.java
//-----------------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

package org.systemsbiology.gaggle.boss.plugins.sbeams;
//-----------------------------------------------------------------------------------------------
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

//-----------------------------------------------------------------------------------------------
public class SBEAMSClient {

  private String cookie = null;
  static private String userName; 
  static private String password;
  private boolean useGui = false;
  private int passwordAttempts = 3;
  private static boolean DEBUG = false;
  private static String COOKIE_URL = "https://db.systemsbiology.net/sbeams/cgi/main.cgi";
  private static String DEFAULT_COOKIE_FILE = "./.sbeamsCookie";
  private static String COOKIE_ERROR = "badCookie";

//-----------------------------------------------------------------------------------------------
 protected static class Response {
   String contentType = null;
   String cookie = null;
   String content = null;
 } //Response class
//-----------------------------------------------------------------------------------------------
  public SBEAMSClient () throws Exception {
  }//constructor
//-----------------------------------------------------------------------------------------------
  public SBEAMSClient (boolean useGui) throws Exception {
	this.useGui = useGui;
  }//constructor
//-----------------------------------------------------------------------------------------------
  public SBEAMSClient( String userName, String password ) throws Exception {
	this.userName = userName;
	this.password = password;
  }//constructor
//-----------------------------------------------------------------------------------------------
  protected void destroyCookie() {
	cookie = null;
  }//destroyCookie
//-----------------------------------------------------------------------------------------------
  protected boolean findCookie(String cookiePath) {
	boolean cookieFound = false;
	try{
	  BufferedReader bufferedReader = new BufferedReader (new FileReader(cookiePath));
	  StringBuffer strbuf = new StringBuffer();
	  String newLineOfText;
	  while ((newLineOfText = bufferedReader.readLine()) != null) {
		Pattern cookieSeek = Pattern.compile("(SBEAMSName\\=(.+)\\;)");
		Matcher match = cookieSeek.matcher(newLineOfText);
		if (match.matches()) {
		  cookie = match.group(1);
		  cookieFound = true;
		  continue;
		}
	  }
	} catch (IOException e) {
	  System.err.println("Cookie File Error or Not Found");
	  cookieFound = false;
	}
	return cookieFound;
  }//findCookie
//-----------------------------------------------------------------------------------------------
  protected boolean findCookie() {
	return findCookie (DEFAULT_COOKIE_FILE);
  }//findCookie
//-----------------------------------------------------------------------------------------------
  protected void saveCookie(String cookieFile) {
	try {
	  BufferedWriter bufferedWriter = new BufferedWriter (new FileWriter (cookieFile));
	  bufferedWriter.write(cookie);
	  bufferedWriter.newLine();
	  bufferedWriter.flush();
	} catch(IOException e){
	  System.err.println("Can't write to cookie file");
	  e.printStackTrace();
	}
  }//saveCookie
//-----------------------------------------------------------------------------------------------
  // Saves whatever is stored as "cookie" in a cookie file .sbeamsCookie
  protected void saveCookie() {
	saveCookie(DEFAULT_COOKIE_FILE);
  }//saveCookie
//-----------------------------------------------------------------------------------------------
  public String getCookie() {
	return cookie;
  }//getCookie
//-----------------------------------------------------------------------------------------------
  public void setCookie(String cookie) {
	this.cookie = cookie;
  }//setCookie
//-----------------------------------------------------------------------------------------------
  public boolean goodCookie() {
	if (cookie == null)
	  return false;
	// Hash for translating month to number
	Hashtable monthHash = new Hashtable();
	monthHash.put("Jan", new Integer(1));
	monthHash.put("Feb", new Integer(2));
	monthHash.put("Mar", new Integer(3));
	monthHash.put("Apr", new Integer(4));
	monthHash.put("May", new Integer(5));
	monthHash.put("Jun", new Integer(6));
	monthHash.put("Jly", new Integer(7));
	monthHash.put("Aug", new Integer(8));
	monthHash.put("Sep", new Integer(9));
	monthHash.put("Oct", new Integer(10));
	monthHash.put("Nov", new Integer(11));
	monthHash.put("Dec", new Integer(12));
	// Extract cookie expiration information
	int cookieYear = 0;
	int cookieMonth = 0;
	int cookieDate = 0;
	int cookieHour = 0;
	int cookieMinute = 0;
	int cookieSecond = 0;
	Pattern expiration = Pattern.compile("expires\\=(\\w+)\\,\\s?(\\d+)\\-(\\w+)\\-(\\d+)\\s+(\\d+)\\:(\\d+)\\:(\\d+)\\;?");
	Matcher match = expiration.matcher(cookie);
	if (match.matches()) {
	  cookieDate = (new Integer(match.group(2))).intValue();
	  cookieMonth = ((Integer)monthHash.get(match.group(3))).intValue();
	  cookieYear = (new Integer(match.group(4))).intValue();
	  cookieHour = (new Integer(match.group(5))).intValue();
	  cookieMinute = (new Integer(match.group(6))).intValue();
	  cookieSecond = (new Integer(match.group(7))).intValue();
	}else { return false;}
	// Get current timestamp and compare it to cookie expiration information
	Calendar calendar = new GregorianCalendar();
	Date trialTime = new Date();
	calendar.setTime(trialTime);

	if (calendar.get(Calendar.YEAR) < cookieYear)
	  return true;
	else if (calendar.get(Calendar.YEAR) > cookieYear)
	  return false;

	if ((calendar.get(Calendar.MONTH)+1) < cookieMonth)
	  return true;
	else if ((calendar.get(Calendar.MONTH)+1) > cookieMonth)
	  return false;

	if (calendar.get(Calendar.DATE) < cookieDate)
	  return true;
	else if (calendar.get(Calendar.DATE) > cookieDate)
	  return false;

    if (calendar.get(Calendar.HOUR_OF_DAY) < cookieHour)
	  return true;
	else if (calendar.get(Calendar.HOUR_OF_DAY) > cookieHour)
	  return false;

	if (calendar.get(Calendar.MINUTE) < cookieMinute)
	  return true;
	else if (calendar.get(Calendar.MINUTE) > cookieMinute)
	  return false;
	
	if (calendar.get(Calendar.SECOND) < cookieSecond)
	  return true;
	else if (calendar.get(Calendar.SECOND) > cookieSecond)
	  return false;

	return false;
	
  }//goodCookie
//-----------------------------------------------------------------------------------------------
  private Response postRequest (String urlString, String params)
    throws Exception {
	URL url = new URL(urlString);
	HttpURLConnection uc = (HttpURLConnection)url.openConnection();
	uc.setDoInput(true);
	uc.setDoOutput(true);
	uc.setUseCaches(false);
	uc.setAllowUserInteraction(false);
	uc.setRequestMethod("POST");
	uc.setRequestProperty("ContentType", "application/x-www-form-urlencoded");
	uc.setRequestProperty("User-Agent", "CytoLinkFromMJ");

	if (cookie != null)
	  uc.setRequestProperty("Cookie", cookie);

	PrintStream out = new PrintStream(uc.getOutputStream());
	out.print(params);
	out.flush();
	out.close();
	uc.connect();
	StringBuffer sb = new StringBuffer();
	String inputLine;
	BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
	while ((inputLine = in.readLine()) != null) {
	  sb.append(inputLine + "\n");
	}
	in.close();

	Response res = new Response();
	res.content = sb.toString();
	res.contentType = uc.getHeaderField("Content-Type");
	res.cookie = uc.getHeaderField("Set-Cookie");
	return res;

  }//postRequest
//-----------------------------------------------------------------------------------------------
  public String fetchSbeamsPage (String urlString, String params) 
	throws Exception{
	if (cookie == null)
	  fetchCookie();
	String paramsInUrl = new String();
	String unparameterizedUrl = urlString;
	Pattern potentialParams = Pattern.compile("(.*)\\?(.*)");
	Matcher match = potentialParams.matcher(urlString);
	if (match.matches()){
	  unparameterizedUrl = match.group(1);
	  paramsInUrl = match.group(2);
	  if (params == null)
		params = paramsInUrl;
	  else
		params +=paramsInUrl;
	}

	Response res = new Response();
	res = postRequest(unparameterizedUrl, params);
	return res.content;

  }//fetchSbeamsPage
//-----------------------------------------------------------------------------------------------
  public String fetchSbeamsPage (String url) 
	throws Exception{
	if (cookie == null)
	  fetchCookie();
   	return fetchSbeamsPage (url, "");

  }//fetchSbeamsPage
//-----------------------------------------------------------------------------------------------
  protected boolean promptForUsernamePassword() {
	return promptForUsernamePassword(useGui);
  }//promptForUsernamePassword
//-----------------------------------------------------------------------------------------------
  // Not yet implemented in a good way (does not hide password on terminal)
  protected boolean promptForUsernamePassword( boolean useGui) {
	boolean success = true;
	if (useGui == true) {
	  PasswordDialog prompt = new PasswordDialog ("Cytoscape SBEAMS Login");

	  if (userName != null ) {
		prompt.setName(userName);
		prompt.setPasswordFocus();
	  }

	  if(prompt.showDialog()){
		userName = prompt.getName();
		password = prompt.getPass();
	  } else {
		System.err.println("User selected cancel");
		success = false;
	  }
	  prompt.dispose();

	} else {
	  System.out.print( "Enter SBEAMS Username: " );
	  try {
		String line;
		BufferedReader stdin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( ( line = stdin.readLine() ) == null ) { };
		userName = line;
	  } catch( Exception e ) { 
		success = false;
	  }

	  System.out.print( "Enter Password: " );
	  try {
		String line;
		BufferedReader stdin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( ( line = stdin.readLine() ) == null ) { };
		password = line;
	  } catch( Exception e ) { 
		success = false;
	  }
	  System.out.println("Thanks for the Username/Password Information, "+userName+"!");
	}

	return success;
  }//promptForUsernamePassword
//-----------------------------------------------------------------------------------------------
  private void fetchCookie() throws Exception{
	while (passwordAttempts > 0) {
	  if (userName == null || password== null)
		promptForUsernamePassword();

	  StringBuffer params = new StringBuffer();
	  //setting the trusted connection			
	  params.append("username");
	  params.append("=");
	  params.append(URLEncoder.encode(userName, "UTF8"));
	  params.append("&");
	  params.append("password");
	  params.append("=");
	  params.append(URLEncoder.encode(password, "UTF8"));
	  params.append("&");
	  params.append("login");
	  params.append("=");
	  params.append(URLEncoder.encode(" Login ", "UTF8"));
	  Response res = postRequest(COOKIE_URL, params.toString());
	  this.cookie = res.cookie;
	  if (res.cookie == null) {
		password = null;
		passwordAttempts--;
	  }else {
		break;
	  }
	}
  }
//-----------------------------------------------------------------------------------------------
  public static void main (String [] args) {
	System.out.println ("test SBEAMS Table Retrieval");
	try {
	  SBEAMSClient client = new SBEAMSClient(true);
	  System.out.println(client.fetchSbeamsPage("http://db/sbeams/cgi/Microarray/ViewFile.cgi?action=read&FILE_NAME=matrix_output&project_id=328&SUBDIR=20050104_154519"));
	}catch (IOException e) {
	  System.err.println("Page Not Found");
	}catch (Exception t) {
	  t.printStackTrace();
	}
	System.exit(0);
  }
}//SBEAMSClient

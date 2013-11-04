/*
 * Created on Aug 12, 2004
 * 
 * This file is part of Banjo (Bayesian Network Inference with Java Objects)
 * edu.duke.cs.banjo
 * Banjo is licensed from Duke University.
 * Copyright (c) 2005-2008 by Alexander J. Hartemink.
 * All rights reserved.
 * 
 * License Info:
 * 
 * For non-commercial use, may be licensed under a Non-Commercial Use License.
 * For commercial use, please contact Alexander J. Hartemink or the Office of
 *   Science and Technology at Duke University. More information available at
 *   http://www.cs.duke.edu/~amink/software/banjo
 * 
 */
package edu.duke.cs.banjo.utility;

import edu.duke.cs.banjo.data.settings.*;

/**
 * Defines the exceptions that the application generates.
 *
 * <p><strong>Details:</strong> <br>
 * - To provide as much meaningful feedback to the user or developer, we use special
 * values for the different exceptions that the application may throw. <br>
 * - (Developer) To add a new BanjoException, simply add a new constant corresponding 
 * to the new exception to the BANJO class' Exception section. <br>
 * - The BanjoErrorHandler class provides a generic high-level handling of the generated
 * exceptions, available to any GUI class that wants to use it. 
 * 
 * <p><strong>Change History:</strong> <br>
 * Created on Aug 12, 2004
 * 
* @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class BanjoException extends Exception {
	
	private String message = new String();

   /*
   * Determines if a de-serialized file is compatible with this class.
   *
   * Maintainers must change this value if and only if the new version
   * of this class is not compatible with old versions. See Sun docs
   * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
   * /serialization/spec/version.doc.html> details. </a>
   *
   * Not necessary to include in first version of the class, but
   * included here as a reminder of its importance.
   */
    private static final long serialVersionUID = 7526472295622776147L;
	
	final int exceptionType;

    /**
     * Basic Constructor: creates a BanjoException of one of our defined types. <br>
     * 
     * @param exceptionType The type of the BanjoException to set.
     */
	public BanjoException( final int exceptionType ) {
		    
	    // This is the minimal constructor for throwing your own BanjoException
	    // Make sure that the type value is "registered" in the BANJO class
		this.exceptionType = exceptionType;
	}

    /**
     * Contructor that handles an exception within the code, for which
	 * we didn't attach an additional message. <br>
	 * 
     * @param e The Exception that was originally encountered.
     * @param exceptionType The type of the BanjoException to set.
     */
	public BanjoException( final Exception e, final int exceptionType ) {
	    
	    // Contructor that handles an exception within the code, for which
	    // we didn't attach an additional message (e.g., a more generic "issue"
	    // that is handled at the final exception handling place)
	    
	    this( exceptionType );

        if ( e.getMessage() != null ) {
            
            this.message = new String( e.getMessage() );
        }
        else {
            
            this.message = new String( "No info available about this exception." );
        }
	}

    /**
     * For trapping an existing exception, and attaching both a type and a 
     * message to it. <br>
     * 
     * @param e The Exception that was encountered, and that triggered the
     * creation of this BanjoException. <br>
     * Note: when DEV_PRINTSTACKTRACE is turned on,
     * the original exception will be echo-printed to the standard i/o.
     * @param exceptionType The type of the BanjoException to set.
     * @param customMessage The message to attach to the BanjoException.
     */
	public BanjoException( 
            final Exception e, 
	        final int exceptionType, 
            final String customMessage ) {
		
	    // Basic constructor for trapping an existing exception, and
	    // attaching both a type and a message to it.
	    
	    this( e, exceptionType );
        StringBuffer extractedMessage = new StringBuffer();
        String spacer = "";
        
        if ( customMessage != null ) {
	    
            extractedMessage.append( customMessage );
            spacer = "\n --------------------- \n";
        }
        
        if ( e.getMessage() != null ) {

            extractedMessage.append( e.getMessage() );
        }
        
        if ( customMessage == null && e.getMessage() == null ) {
            

            extractedMessage.append( "No info available about this exception." );
        }
        
        this.message = new String( extractedMessage.toString() );
	}

    /**
     * Constructor for propagating an existing BanjoException.
     * 
     * @param e The BanjoException to propagate.
     */
	public BanjoException( final BanjoException e ) {

	    // This constructor is mainly used to propagate an existing exception
	    // of BanjoException "type" 
	    
		this.exceptionType = e.exceptionType;
		if ( e.message != null )
		    this.message = new String( e.getMessage() );
	}

    /**
     * Is used when we didn't encounter an exception in the
     * code, but rather want to flag an "issue" (e.g., unusable input value, etc)
     * 
     * @param exceptionType The type of the BanjoException to set.
     * @param customMessage The message to attach to the BanjoException.
     */
	public BanjoException( final int exceptionType, final String customMessage ) {
		
	    // This constructor is used when we didn't encounter an exception in the
	    // code, but rather want to flag an "issue" (e.g., unusable input value, etc)
	    
	    this( exceptionType );
	    this.message = new String( customMessage );
	}
	

	public BanjoException( 
            final int exceptionType, 
	        final Object exceptionInfo, 
	        final Object exceptionLocation ) {
		
	    // This constructor is used when we didn't encounter an exception in the
	    // code, but rather want to flag an "issue" (e.g., unusable input value, etc)
	    
	    this( exceptionType );

	    System.out.println( exceptionInfo.getClass().toString() );
	    
	    
	    if ( exceptionInfo instanceof String ) {
	        
	        this.message = new String( (String) exceptionInfo ); 
	    }
	    else if ( exceptionInfo instanceof SettingItem ) {
	        
	        this.message = "Code section for validating '" +
	        	((SettingItem) exceptionInfo).getItemNameCanonical() + "'"; 
	    }
	    
		if ( exceptionLocation != null )
		    this.message += ", located in class " + 
		    			StringUtil.getClassName( exceptionLocation ) + ".";
	}

	/**
	 * @return Returns the exceptionType.
	 */
	public int getExceptionType() {
		return exceptionType;
	}
    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}

/*
 * Created on Oct 25, 2005
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

/**
 * Used for tracking multiple internal "errors", e.g., while trying to validate
 * the user input.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Oct 25, 2005  (v2.0)
 * 
 * <p>
 * hjs (v2.1)               Add constructor based on existing BanjoError.
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class BanjoError {

    private String errorMessageText;
    private final int errorType;
    private String settingName;
    private Object errorInfo;
 
    //  
    public BanjoError( String _errorMessageText, 
            int _errorType,
            String _settingName,
            Object _errorInfo ) {

        errorType = _errorType;
        
        if ( _errorMessageText != null ) {
            
            errorMessageText = new String( _errorMessageText );
        }
        
        if ( _settingName != null ) {
            
            settingName = new String( _settingName );
        }
        
        if ( _errorInfo != null ) {
        
            if ( _errorInfo instanceof String ) {
                
                errorInfo = new String( (String) _errorInfo );
            }
            else {
            
                errorInfo = _errorInfo;
        	}
        }
    }
    
    public BanjoError( BanjoError _banjoError ) {

        this ( _banjoError.errorMessageText, 
                _banjoError.errorType,
                _banjoError.settingName, 
                (Object) _banjoError.errorInfo );
    }
    
    /**
     * @return Returns the errorMessageText.
     */
    public String getErrorMessageText() {
        return errorMessageText;
    }
    /**
     * @return Returns the errorType.
     */
    public int getErrorType() {
        return errorType;
    }
    /**
     * @return Returns the errorInfo.
     */
    public Object getErrorInfo() {
        return errorInfo;
    }
    /**
     * @return Returns the settingName.
     */
    public String getSettingName() {
        return settingName;
    }
}

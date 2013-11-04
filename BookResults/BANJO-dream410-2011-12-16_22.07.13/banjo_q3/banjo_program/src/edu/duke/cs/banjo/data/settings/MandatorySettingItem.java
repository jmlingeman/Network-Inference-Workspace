
/*
 * Created on Oct 19, 2005
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
package edu.duke.cs.banjo.data.settings;


import edu.duke.cs.banjo.utility.BANJO;
import edu.duke.cs.banjo.utility.BanjoException;
import edu.duke.cs.banjo.utility.BanjoError;

/**
 * Holds an individual setting that is loaded into Banjo.
 * 
 * <p><strong>Details:</strong> <br>
 *  
 * <p><strong>Change History:</strong> <br>
 * Created on Oct 19, 2005 <br>
 * Part of a large-scale refactoring of the settings processing.
 * 
 * <p>
 * hjs (v2.1)   Add constructor based on existing settingItem
 * 
 * @author Jurgen Sladeczek (hjs) <br>
 * For the latest info, please visit www.cs.duke.edu.
 */
public class MandatorySettingItem extends SettingItem {

    /**
     * Constructor for creating a new "mandatory" settingItem.
     * 
     * @param _itemNameCanonical The unique name that we use to refer to the settingItem.
     * @param _itemNameDescriptive The descriptive name of the settingItem.
     * @param _settingNameForDisplay The special name of the settingItem used in user feedback.
     * 
     */
    public MandatorySettingItem( 
            final String _itemNameCanonical,
            final String _itemNameDescriptive, 
            final String _settingNameForDisplay ) throws Exception {

        super( _itemNameCanonical, _itemNameDescriptive, _settingNameForDisplay );
        
        
        if ( _itemNameCanonical == null || _itemNameCanonical.equalsIgnoreCase("") ) {
            
            // throw new BanjoException: this is a developer issue, so the end user
            // should never encounter this message            
            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                    "(SettingItem.constructor) " +
            		"The value of property 'itemNameCanonical' for item '" +
                    getItemNameForComparison() + "' needs to be supplied." );
            
        }
        if ( _itemNameDescriptive == null || _itemNameDescriptive.equalsIgnoreCase("") ) {
            
            // throw new BanjoException: this is a developer issue, so the end user
            // should never encounter this message            
            throw new BanjoException( BANJO.ERROR_BANJO_DEV,
                    "(SettingItem.constructor) " +
            		"The value of property 'itemNameDescriptive' for item '" +
                    getItemNameForComparison() + "' needs to be supplied." );
            
        }
    }

    /**
     * Constructor based on an existing settingItem. Note that the invoked super class
     * constructor does a "deep" copy.
     *
     * @param _settingItem The settingItem that we want to use as basis for the new one.
     * 
     */    
    public MandatorySettingItem ( SettingItem _settingItem ) {
        
        super( _settingItem );
    }


    /**
     * Validation of a "mandatory" setting item.
     * 
     */
    // Note: Depending on whether this method returns 'true' or 'false', we will
    // add the settingItem to a collection. This lets us present all input related
    // errors to the user at one time.
    public boolean validate( 
            final String _itemValueAsLoaded,
            final String _dataTypeToValidateAgainst,
            final int _validationType,
            final Object _additionalInfo, 
            final Object _defaultValue ) throws Exception {

        boolean isValidDataType = true;
        
        String strItemValueToValidate;

	    setItemValueAsLoaded( _itemValueAsLoaded );
        strItemValueToValidate = _itemValueAsLoaded;
	    
        // The supplied parameter is a mandatory one: 
        if ( ( strItemValueToValidate == null || strItemValueToValidate.equalsIgnoreCase("") )
                && _defaultValue.equals( BANJO.BANJO_NOVALUESUPPLIED_STRING ) ) {

            isValidDataType = false;
            
            // In this case we immediately flag the issue, because a mandatory value is
            // missing, and there is no default value we can use
            BanjoError errorItem = new BanjoError( 
                    errorMessageMissingValue( 
                            getItemNameDescriptive(), 
                            getItemNameCanonical(), 
                            getItemValueAsLoaded(), 
                            _dataTypeToValidateAgainst ),
                    BANJO.ERRORTYPE_MISSINGVALUE,
                    getItemNameCanonical(),
                    "(MandatorySettingitem.validate) " +
                    "The value for '" + getItemNameDescriptive() +
                    "' is missing." );

            collectedErrors.add( errorItem );
        }
        else if ( strItemValueToValidate == null || strItemValueToValidate.equalsIgnoreCase("") ) {
            
            // In this case we validate using (potentially) the default value
            isValidDataType = super.validate( 
                    strItemValueToValidate,
                    _dataTypeToValidateAgainst,
                    _validationType,
                    _additionalInfo, 
                    _defaultValue );
        }
        else {

	        // When the user supplied a value, then we check that value WITHOUT ever
	        // using the default value
		    isValidDataType = super.validate( 
                    strItemValueToValidate,
		            _dataTypeToValidateAgainst,
		            _validationType,
		            _additionalInfo,
		            null );
	    }
	    
        this.setValidSetting( isValidDataType );
        return isValidDataType;
    }
}

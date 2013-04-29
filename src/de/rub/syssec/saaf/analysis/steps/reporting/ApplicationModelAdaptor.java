/**
 * 
 */
package de.rub.syssec.saaf.analysis.steps.reporting;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

import de.rub.syssec.saaf.model.application.ApplicationInterface;
import de.rub.syssec.saaf.model.application.Digest;

/**
 * Provides StringTemplate with access to propertys that do not follow java-bean specification.
 * 
 * This class was introduced since there is only one getter for the Hashes getHash(Digest digest).
 * Since this does not comply with the javabean spec it would cause problems when you used
 * the an expresssion like analysis.HashMD5 in a stringtemplate.
 * 
 * For more information see: http://www.antlr.org/wiki/display/ST4/Model+adaptors
 * 
 * @author Tilman Bender <tilman.bender@rub.de>
 *
 */
public class ApplicationModelAdaptor extends ObjectModelAdaptor {

	/* (non-Javadoc)
	 * @see org.stringtemplate.v4.ModelAdaptor#getProperty(org.stringtemplate.v4.Interpreter, org.stringtemplate.v4.ST, java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object getProperty(Interpreter interpreter, ST self, Object o,
			Object property, String propertyName) throws STNoSuchPropertyException {
        if ( propertyName.equals("HashMD5") ) return ((ApplicationInterface)o).getMessageDigest(Digest.MD5);
        if ( propertyName.equals("HashSHA1") ) return ((ApplicationInterface)o).getMessageDigest(Digest.SHA1);
        if ( propertyName.equals("HashSHA256") ) return ((ApplicationInterface)o).getMessageDigest(Digest.SHA256);
        return super.getProperty(interpreter,self,o,property,propertyName);

	}

}

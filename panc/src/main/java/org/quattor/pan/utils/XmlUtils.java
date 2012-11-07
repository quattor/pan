package org.quattor.pan.utils;

import static org.quattor.pan.utils.MessageUtils.MSG_MISSING_SAX_TRANSFORMER;
import static org.quattor.pan.utils.MessageUtils.MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT;

import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.quattor.pan.exceptions.CompilerError;

public class XmlUtils {

    private XmlUtils() {

    }

    public static TransformerHandler getSaxTransformerHandler() {

        try {

            // Generate the transformer factory. Need to guarantee that we get a
            // SAXTransformerFactory.
            TransformerFactory factory = TransformerFactory.newInstance();
            if (!factory.getFeature(SAXTransformerFactory.FEATURE)) {
                throw CompilerError.create(MSG_MISSING_SAX_TRANSFORMER);
            }

            // Only set the indentation if the returned TransformerFactory
            // supports it.
            try {
                factory.setAttribute("indent-number", Integer.valueOf(4));
            } catch (IllegalArgumentException consumed) {
            }

            // Can safely cast the factory to a SAX-specific one. Get the
            // handler to feed with SAX events.
            SAXTransformerFactory saxfactory = (SAXTransformerFactory) factory;

            TransformerHandler handler = saxfactory.newTransformerHandler();

            // Set parameters of the embedded transformer.
            Transformer transformer = handler.getTransformer();
            Properties properties = new Properties();
            properties.setProperty(OutputKeys.INDENT, "yes");
            properties.setProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperties(properties);

            return handler;

        } catch (TransformerConfigurationException tce) {
            Error error = CompilerError
                    .create(MSG_UNEXPECTED_EXCEPTION_WHILE_WRITING_OUTPUT);
            error.initCause(tce);
            throw error;
        }

    }

}

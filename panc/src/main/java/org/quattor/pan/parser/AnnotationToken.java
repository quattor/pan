package org.quattor.pan.parser;

import org.quattor.pan.parser.annotation.AnnotationProcessor;

public class AnnotationToken extends Token {

    private Object value;

    public AnnotationToken() {
        super();
    }

    public AnnotationToken(int kind) {
        this(kind, null);
    }

    public AnnotationToken(int kind, String image) {
        super(kind, image);

        if (image != null) {
            // Would throw a NullPointerException
            // Also, recent javacc generated code doesn't call the init wit the image
            //    but sets the attribute in the newToken call
            try {
                value = AnnotationProcessor.process(image);
            } catch (org.quattor.pan.parser.annotation.ParseException e) {
                value = e;
            }
        }
    }

    @Override
    public Object getValue() {
        if (value == null && this.image != null) {
            // See remark on recet javacc in the init
            try {
                value = AnnotationProcessor.process(this.image);
            } catch (org.quattor.pan.parser.annotation.ParseException e) {
                value = e;
            }
        }
        return value;
    }

}

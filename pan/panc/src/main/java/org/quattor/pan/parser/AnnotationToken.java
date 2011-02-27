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

        try {
            value = AnnotationProcessor.process(image);
        } catch (org.quattor.pan.parser.annotation.ParseException e) {
            value = e;
        }
    }

    @Override
    public Object getValue() {
        return value;
    }

}

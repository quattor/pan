package org.quattor.pan.type;

import org.junit.Test;
import org.quattor.pan.dml.data.Element;
import org.quattor.pan.dml.data.LongProperty;
import org.quattor.pan.dml.data.StringProperty;
import org.quattor.pan.exceptions.SyntaxException;
import org.quattor.pan.exceptions.ValidationException;
import org.quattor.pan.template.BuildContext;
import org.quattor.pan.template.Context;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by iliclaey.
 */
public class ChoiceTypeTest {

    @Test(expected = SyntaxException.class)
    public void testInvalidNumberArgs() throws SyntaxException {
        List<Element> args = new ArrayList<Element>();
        args.add(StringProperty.getInstance("Exception"));

        ChoiceType.getInstance(null, null, args);
    }

    @Test(expected = ValidationException.class)
    public void testInvalidSelfValidation() throws SyntaxException {
        Context context = new BuildContext();

        List<Element> args = new ArrayList<Element>();
        args.add(StringProperty.getInstance("A"));
        args.add(StringProperty.getInstance("B"));

        BaseType type = ChoiceType.getInstance(null, null, args);

        type.validate(context, LongProperty.getInstance(1));
    }

    @Test(expected = ValidationException.class)
    public void testInvalidChoice() throws SyntaxException {
        Context context = new BuildContext();

        List<Element> args = new ArrayList<Element>();
        args.add(StringProperty.getInstance("A"));
        args.add(StringProperty.getInstance("B"));

        BaseType type = ChoiceType.getInstance(null, null, args);

        type.validate(context, StringProperty.getInstance("C"));
    }

    @Test
    public void testValidChoice() throws SyntaxException {
        Context context = new BuildContext();

        List<Element> args = new ArrayList<Element>();
        args.add(StringProperty.getInstance("A"));
        args.add(StringProperty.getInstance("B"));

        BaseType type = ChoiceType.getInstance(null, null, args);

        Object o = type.validate(context, StringProperty.getInstance("A"));

        assertEquals(o, null);
    }


}

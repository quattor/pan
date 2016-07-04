package org.quattor.pan.dml.functions;

import org.junit.Test;
import org.quattor.pan.dml.data.*;
import org.quattor.pan.exceptions.EvaluationException;
import org.quattor.pan.exceptions.SyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by iliclaey.
 */
public class JoinTest extends BuiltInFunctionTestUtils {

    @Test
    public void checkGetInstance() {
        checkClassRequirements(Join.class);
    }

    @Test(expected = SyntaxException.class)
    public void testTooFewArguments() throws SyntaxException {
        Join.getInstance(null);
    }

    @Test(expected = SyntaxException.class)
    public void testNullValueFirstArg() throws SyntaxException {
        Join.getInstance(null, Null.getInstance(), new ListResource());
    }

    @Test(expected = SyntaxException.class)
    public void testNullValueSecondArg() throws SyntaxException {
        Join.getInstance(null, StringProperty.getInstance(","), Null.getInstance());
    }

    @Test(expected = SyntaxException.class)
    public void testNullValueArgs() throws SyntaxException {
        Join.getInstance(null, Null.getInstance(), Null.getInstance());
    }

    @Test(expected = SyntaxException.class)
    public void testInvalidFirstArg() throws SyntaxException {
        runDml(Join.getInstance(null, LongProperty.getInstance(0L),
                new ListResource()));
    }

    @Test(expected = SyntaxException.class)
    public void testInvalidSecondArg() throws SyntaxException {
        runDml(Join.getInstance(null, StringProperty.getInstance("OK"),
                new HashResource()));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidArgs() throws SyntaxException {
        runDml(Join.getInstance(null, StringProperty.getInstance(","),
                StringProperty.getInstance("First"), LongProperty.getInstance(0L)));
    }

    @Test(expected = EvaluationException.class)
    public void testInvalidTypeSecondArg() throws SyntaxException {
        Element[] args = {
                LongProperty.getInstance(1),
                LongProperty.getInstance(2),
                LongProperty.getInstance(3)
        };

        runDml(Join.getInstance(null, StringProperty.getInstance(","),
                new ListResource(args)));
    }

    @Test(expected = EvaluationException.class)
    public void testNestedElements() throws SyntaxException {
        Element[] args = { new ListResource() };

        runDml(Join.getInstance(null, StringProperty.getInstance("OK"),
                new ListResource(args)));
    }

    @Test
    public void testJoinWithList() throws SyntaxException {
        String expected = new String("a-b-c");

        // Execute operation
        Element[] args = {
                StringProperty.getInstance("a"),
                StringProperty.getInstance("b"),
                StringProperty.getInstance("c")
        };

        Element e = runDml(Join.getInstance(null, StringProperty.getInstance("-"),
                new ListResource(args)));

        // Check result
        assertTrue(e instanceof StringProperty);
        String result = ((StringProperty) e).getValue();
        assertEquals(result, expected);
    }

    @Test
    public void testJoinWithoutList() throws SyntaxException {
        String expected = new String("a-b-c");

        // Execute operation
        Element e = runDml(Join.getInstance(null,
                StringProperty.getInstance("-"),
                StringProperty.getInstance("a"),
                StringProperty.getInstance("b"),
                StringProperty.getInstance("c")
        ));

        // Check result
        assertTrue(e instanceof StringProperty);
        String result = ((StringProperty) e).getValue();
        assertEquals(result, expected);
    }
}

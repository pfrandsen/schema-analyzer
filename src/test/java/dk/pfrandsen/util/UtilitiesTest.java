package dk.pfrandsen.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class UtilitiesTest {

    @Test
    public void testValidLowerCamelCase() {
        assertTrue(Utilities.isLowerCamelCaseAscii("abcDefGhi"));
        assertTrue(Utilities.isLowerCamelCaseAscii("abcDefGhiV20"));
    }

    @Test
    public void testValidUpperCamelCase() {
        assertTrue(Utilities.isUpperCamelCaseAscii("AbcDefGhi"));
        assertTrue(Utilities.isUpperCamelCaseAscii("AbcDefGhiV20"));
    }

    @Test
    public void testInvalidLowerCamelCase() {
        assertFalse(Utilities.isLowerCamelCaseAscii("AbcDefGhi"));
        assertFalse(Utilities.isLowerCamelCaseAscii("abc DefGhi"));
        assertFalse(Utilities.isLowerCamelCaseAscii("12AbcDefGhi"));
        assertFalse(Utilities.isLowerCamelCaseAscii("øbcDefGhiV20"));
        assertFalse(Utilities.isLowerCamelCaseAscii("bcDefGhi-V20"));
    }

    @Test
    public void testInvalidUpperCamelCase() {
        assertFalse(Utilities.isUpperCamelCaseAscii("abcDefGhi"));
        assertFalse(Utilities.isUpperCamelCaseAscii("Abc DefGhi"));
        assertFalse(Utilities.isUpperCamelCaseAscii("12AbcDefGhi"));
        assertFalse(Utilities.isUpperCamelCaseAscii("ØbcDefGhiV20"));
        assertFalse(Utilities.isUpperCamelCaseAscii("BcDefGhi-V20"));
    }

    @Test
    public void testSplitOnUppercase() {
        List<String> words = Utilities.splitOnUppercase("AAbcDef23GhiB");
        assertEquals(5, words.size());
        assertEquals("A", words.get(0));
        assertEquals("Abc", words.get(1));
        assertEquals("Def23", words.get(2));
        assertEquals("Ghi", words.get(3));
        assertEquals("B", words.get(4));
    }

    @Test
    public void testSplitOnUppercase2() {
        List<String> words = Utilities.splitOnUppercase("aAbcDef23GhiB1");
        assertEquals(5, words.size());
        assertEquals("a", words.get(0));
        assertEquals("Abc", words.get(1));
        assertEquals("Def23", words.get(2));
        assertEquals("Ghi", words.get(3));
        assertEquals("B1", words.get(4));
    }

    @Test
    public void testSplitJoin() {
        List<String> words = Utilities.splitOnUppercase("aa1AbcDef23B1");
        assertEquals(4, words.size());
        assertEquals("aa1;Abc;Def23;B1", Utilities.join(";", words));
    }

    @Test
    public void testSplitJoinAllCaps() {
        List<String> words = Utilities.splitOnUppercase("AAA");
        assertEquals(3, words.size());
        assertEquals("A;A;A", Utilities.join(";", words));
    }

    @Test
    public void testToUpperCamelCaseString() {
        String unCapitalized = "thisIsIt";
        String capitalized = "ThisIsIt";
        assertEquals(capitalized, Utilities.toUpperCamelCase(unCapitalized));
    }

    @Test
    public void testToUpperCamelCaseStringNop() {
        String capitalized = "ThisIsIt";
        assertEquals(capitalized, Utilities.toUpperCamelCase(capitalized));
    }

    @Test
    public void testToUpperCamelCaseList() {
        String [] unCapitalized = {"thisIsIt", "soIsThis"};
        String [] capitalized = {"ThisIsIt", "SoIsThis"};
        List<String> ucc = Utilities.toUpperCamelCase(Arrays.asList(unCapitalized));
        assertEquals(capitalized[0], ucc.get(0));
        assertEquals(capitalized[1], ucc.get(1));
    }

    @Test
    public void testToLowerCamelCaseString() {
        String unCapitalized = "thisIsIt";
        String capitalized = "ThisIsIt";
        assertEquals(unCapitalized, Utilities.toLowerCamelCase(capitalized));
    }

    @Test
    public void testToLowerCamelCaseStringNop() {
        String unCapitalized = "thisIsIt";
        assertEquals(unCapitalized, Utilities.toLowerCamelCase(unCapitalized));
    }

    @Test
    public void testToLowerCamelCaseList() {
        String [] unCapitalized = {"thisIsIt", "soIsThis"};
        String [] capitalized = {"ThisIsIt", "SoIsThis"};
        List<String> lcc = Utilities.toLowerCamelCase(Arrays.asList(capitalized));
        assertEquals(unCapitalized[0], lcc.get(0));
        assertEquals(unCapitalized[1], lcc.get(1));
    }

    @Test
    public void testPathToNamespace() {
        Path path = Paths.get("abc", "def", "ghi");
        assertEquals(Utilities.pathToNamespace("x.y", path), "http://x.y/abc/def/ghi");
    }

    @Test
    public void testPathToNamespaceSingle() {
        Path path = Paths.get("abc");
        assertEquals(Utilities.pathToNamespace("x.y.z", path), "http://x.y.z/abc");
    }

    @Test
    public void testPathToNamespaceEmpty() {
        Path path = Paths.get("");
        assertEquals(Utilities.pathToNamespace("", path), "http:///");
    }

}

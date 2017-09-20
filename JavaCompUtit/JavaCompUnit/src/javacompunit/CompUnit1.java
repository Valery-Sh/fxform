/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javacompunit;

import java.net.URI;
import static javax.tools.JavaFileObject.Kind.SOURCE;
import javax.tools.SimpleJavaFileObject;

/**
 *
 * @author Valery
 */
public class CompUnit1 extends SimpleJavaFileObject {

    public CompUnit1() {
        super(URI.create("myfo:///Test.java"), SOURCE);
    }

    @Override
    public String getCharContent(boolean ignoreEncodingErrors) {
        //      0         1         2
        //      0123456789012345678901234
        return "class Test { Test() { } }";
    }
}

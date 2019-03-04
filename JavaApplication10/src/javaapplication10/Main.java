/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication10;

public class Main {
    public static void main(String[] args) {
        TEST2.set();
        new TEST2().set();
    }
    public static class TEST {

        public static void set() {
            set1();
        }
        public static void set1() {
        }
        
    }

    public static class TEST2 {

        TEST TEST1;

        public static void set() {
            TEST.set();
        }
        
    }
}

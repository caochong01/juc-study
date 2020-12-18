package com.devil.juc;

public class TestOne {

    public static void main(String[] args) {
        long longBits = Double.doubleToRawLongBits(3.14159265D);
        System.out.println(longBits);
        double aDouble = Double.longBitsToDouble(longBits);
        System.out.println(aDouble);

        int a = -1;

        if ((a += 1) == 0) {
            System.out.println("1");
            a = 1;
        } else if (a == 1) {
            System.out.println("2");
            a = 2;
        }

        System.out.println(a);
    }
}

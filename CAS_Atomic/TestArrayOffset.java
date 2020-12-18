package com.devil.juc;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Atomic Array形式值实现细节：
 * 1. 数组在Java中是具有单独的对象头
 * 2. byteOffset偏移量计算
 */
public class TestArrayOffset {

    private static final Unsafe unsafe = getUnsafeInstance();
    private static final int base = unsafe.arrayBaseOffset(int[].class); // 获取int[]类型的 java对象头长度
    private static final int shift;
    private final int[] array;

    public TestArrayOffset(int i) {
        this.array = new int[i];
    }

    public static void main(String[] args) {
        int arrayLen = 3;
        TestArrayOffset offset = new TestArrayOffset(arrayLen);
        for (int i = 0; i < arrayLen; i++) {
            System.out.println("索引i = " + i + " 的偏移量为 " + offset.checkedByteOffset(i));
        }

        /* result:
            scale: 4
            numberOfLeadingZeros: 29
            shift: 2
            base: 16
            索引i = 0 的偏移量为 16
            索引i = 1 的偏移量为 20
            索引i = 2 的偏移量为 24
         */
    }


    static {
        int scale = unsafe.arrayIndexScale(int[].class); // 获取int[]类型的 元素大小，好比int为4字节
        System.out.println("scale: " + scale);
        if ((scale & (scale - 1)) != 0)
            throw new Error("data type scale not a power of two");
        shift = 31 - Integer.numberOfLeadingZeros(scale);
        System.out.println("numberOfLeadingZeros: " + (31 - shift)); // 获取二进制从左开始 连续为0个数
        System.out.println("shift: " + shift);
        System.out.println("base: " + base);
    }


    private long checkedByteOffset(int i) {
        if (i < 0 || i >= array.length)
            throw new IndexOutOfBoundsException("index " + i);

        return byteOffset(i);
    }

    /**
     * 获取数组坐标在对象中的偏移量，N代表入参i:
     * N * 2的shift次幂 + 对象头base
     * 等价于 (N * 元素大小scale + 对象头base)
     */
    private static long byteOffset(int i) {
        return ((long) i << shift) + base;
    }

    // 使用Unsafe方法
    private static Unsafe getUnsafeInstance() {
        Field theUnsafeInstance;
        try {
            theUnsafeInstance = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeInstance.setAccessible(true);
            return (Unsafe) theUnsafeInstance.get(Unsafe.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("theUnsafe Exception");
    }
}

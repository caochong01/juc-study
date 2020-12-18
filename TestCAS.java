package com.devil.juc;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * CAS虽然很高效的解决了原子操作问题，但是CAS仍然存在三大问题。
 * 1. 循环时间长开销很大，CAS适用于读多写少操作；
 * 2. 只能保证一个变量的原子操作；
 * 3. ABA问题。
 */
public class TestCAS {

    private static final Unsafe U = getUnsafeInstance();

    // 如果使用 CAS方法对其直接更改则无法修改，
    // TODO 初步估略原因为unsafe是对jvm内存的直接操作，所以必须用偏移量来比对
    private volatile int t_val = 0;

    // 如果想获取一个对象的属性的值，我们一般通过getter方法获得，
    // 而sun.misc.Unsafe却不同，我们可以把一个对象实例想象成一块内存，
    // 而这块内存中包含了一些属性，如何获取这块内存中的某个属性呢？
    // 那就需要该属性在该内存的偏移量了，每个属性在该对象内存中valueOffset偏移量不同，
    // 每个属性的偏移量在该类加载或者之前就已经确定了(则该类的不同实例的同一属性偏移量相等)，
    // 所以sun.misc.Unsafe可以通过一个对象实例和该属性的偏移量用原语获得该对象对应属性的值；
    private static final long valueOffset;

    static {
        try {
            valueOffset = U.objectFieldOffset
                    (TestCAS.class.getDeclaredField("t_val"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TestCAS testCAS = new TestCAS();
        testCAS.testCas();
    }

    // 测试 cas 操作
    // compareAndSwapInt(操作对象, 原始值, 期望值, 修改值)
    public void testCas() throws InterruptedException {
        Runnable runnable = () -> {
            int f_val;
            int u_val;
            do {
                f_val = t_val;
                u_val = f_val + 1;
                // System.out.println(t_val + " " + f_val + " " + u_val);
            } while (!U.compareAndSwapInt(this, valueOffset, f_val, u_val));

//            synchronized (this){
//                t_val = t_val + 1;
//            }
        };

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(runnable);
            thread.start();
        }
        Thread.sleep(500); // 主线程比子线程执行的快，导致输出不正确
        System.out.println(t_val);
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

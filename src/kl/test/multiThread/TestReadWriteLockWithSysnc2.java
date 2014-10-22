package kl.test.multiThread;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
/**
***┏┓           ┏┓
*┏┛┻━━━┛┻┓
*┃                          ┃
*┃           ━           ┃
*┃     ┳┛  ┗┳    ┃
*┃                          ┃
*┃ ``` ┻   ```┃
*┃                         ┃
*┗━┓          ┏━┛
*****┃       ┃Code is far away from bug with the animal protecting.
*****┃       ┃神兽护佑,代码无bug.
*****┃       ┗━━━━  ┓
*****┃                            ┣┓
*****┃                            ┏┛
*****┗ ┓ ┓┏━┳┓┏┛
*******┃┫┫    ┃┫┫
*******┗┻┛    ┗┻┛
* @author K.L
* 与TestReadWriteLockUseSysnc不同，修改了TestReadWriteLockUseSysnc中
* “//这里可能出现问题，writeLock释放后，但readCount未改变前/readCountLock未获取前”
* 发现，其实只需要一个临界区，共享变量是readCount和needWrite，needWrite是用来通知读线程
* 需要写，所以读要阻塞自己，readCount是在读的读线程数量，写线程通过查看这个共享变量决定是写
* 还是阻塞自己。由于是写优先，所以写线程先把needWrite设为true，再判断阻塞。读线程则先看看有没有写
* 线程需要写，再readCount++。注意看阻塞和唤醒的条件。
 */
public class TestReadWriteLockWithSysnc2 {

	static volatile int readCount = 0;
	static volatile boolean needWrite = false;
	static StringBuilder book = new StringBuilder("this is a book about...");
	static CountDownLatch startLatch = new CountDownLatch(1);

	/**
	 * 临界区是readCount needWrite
	 */
	static Object mutex = new Object();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Thread(new Reader("Tiffany")).start();
		new Thread(new Reader("YoYo")).start();
		new Thread(new Writer()).start();
		new Thread(new Reader("Kate")).start();
		startLatch.countDown();
	}

	static Random random = new Random();
	public static int getRandom() {
		return random.nextInt(301) + 200;
	}

	public static class Reader implements Runnable {
		String name;

		public Reader(String name) {
			this.name = name;
		}

		@Override
		public void run() {
			try {
				startLatch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			for (int i = 0; i != 20; i++) {
				synchronized (mutex) {
					//synchronized的一个坏处，就是notifyAll之后，
					//还要用While来再次判断是否符合条件了，synchronized的粒度很大
					//如果用Lock，则可以为needWrite和readCount分别new一个Condition，分别去阻塞！去唤醒！
					while (needWrite) {
						try {
							mutex.wait();// 自动重获锁
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					readCount++;
				}
				reading(i);
			}
		}

		void reading(int i) {
			//System.out.print(name + " read " + needWrite);
			int t = getRandom();
			try {
				Thread.sleep(t);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(name + " read " + i + " times ,content is "
					+ book.toString());
			synchronized (mutex) {
				readCount--;
				if (readCount == 0) {
					//为什么是all，因为有可能有reading因为needWrite
					//而阻塞
					mutex.notifyAll();
				}
			}
		}
	}

	static class Writer implements Runnable {
		String[] contents = new String[] { " a story.", "Long long ago,",
				"a preson", " called xiao ming.", "One day,", "he ", "died." };

		public Writer() {
		}

		@Override
		public void run() {
			try {
				startLatch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			for (int i = 0; i != contents.length; i++) {
				synchronized (mutex) {
					needWrite = true;
					while (readCount != 0) {
						try {
							mutex.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					System.out.print("writing...");
					try {
						Thread.sleep(getRandom() * 3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(" i write " + book.append(contents[i]));
					needWrite = false;
					mutex.notifyAll();
				}
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}
}

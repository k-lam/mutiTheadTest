package kl.test.multiThread;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

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
* 
* 信号量  PV,P = acquire, V = release
* 好处：
* 1. 阻塞与数量相关，如果以数量来判断的是否阻塞，明显比Lock.Contition 和 object.wait好
* 2. 不需要拥有线程就可以进行PV操作
* 
 */
public class TestReadWriteLockWithSemaphore {

	/**
	 * 同时只可以有一个允许写
	 */
	static Semaphore writeLock = new Semaphore(1);
	static Object lock = new Object();
	/**
	 * 没有这把锁，写线程会饿死的
	 */
	static Object needWriteLock = new Object();
	static volatile int readCount = 0;

	static StringBuilder book = new StringBuilder("this is a book about...");
	static CountDownLatch startLatch = new CountDownLatch(1);

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
				synchronized (needWriteLock) {
					synchronized (lock) {
						readCount++;
						if (readCount == 1) {
							try {
								writeLock.acquire();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
				reading(i);
				synchronized (lock) {
					readCount--;
					if (readCount == 0) {
						writeLock.release();
					}
				}
			}
		}

		void reading(int i) {
			int t = getRandom();
			try {
				Thread.sleep(t);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(name + " read " + i + " times ,content is "
					+ book.toString());
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
				synchronized (needWriteLock) {
					try {
						writeLock.acquire();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					System.out.print("writing...");
					try {
						Thread.sleep(getRandom() * 3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(" i write " + book.append(contents[i]));
					writeLock.release();
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

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
* 用synchronized完成的读写锁，只有一个写，多个读，读的时候，不可以写，写的时候不可以读，读的时候，其他人可以读
* 
 */
public class TestReadWriteLockWithSysnc {
	static Object readCountLock = new Object();
	static volatile int readCount = 0;
	static Object writeLock = new Object();
	static volatile boolean needWrite = false;
	static StringBuilder book = new StringBuilder("this is a book about...");
	static CountDownLatch startLatch = new CountDownLatch(1);//用来一起开始的

	//static Object mutex = new Object();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Thread(new Writer()).start();
		new Thread(new Reader("Tiffany")).start();
		new Thread(new Reader("YoYo")).start();
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
				synchronized (writeLock) {
					if (needWrite) {
						// System.out.println("r needWrite is " + needWrite);
						try {
							writeLock.wait();// 自动重获锁
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				//这里可能出现问题，writeLock释放后，但readCount未改变前/readCountLock未获取前
				reading(i);
			}
		}

		void reading(int i) {
			System.out.print(name + " read ");
			synchronized (readCountLock) {
				readCount++;
			}
			int t = getRandom();
			try {
				Thread.sleep(t);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(name + " read " + i + " times ,content is "
					+ book.toString());
			synchronized (readCountLock) {
				readCount--;
				readCountLock.notify();
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
				needWrite = true;
				synchronized (writeLock) {
					synchronized (readCountLock) {
						while (readCount != 0) {
							try {
								readCountLock.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					// while (readCount != 0);
					System.out.print("writing...");
					try {
						Thread.sleep(getRandom() * 3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(" i writer " + book.append(contents[i]));
					needWrite = false;
					writeLock.notifyAll();
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

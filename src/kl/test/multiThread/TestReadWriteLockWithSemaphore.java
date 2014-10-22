package kl.test.multiThread;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
***����           ����
*�����ߩ��������ߩ�
*��                          ��
*��           ��           ��
*��     �ש�  ����    ��
*��                          ��
*�� ``` ��   ```��
*��                         ��
*������          ������
*****��       ��Code is far away from bug with the animal protecting.
*****��       �����޻���,������bug.
*****��       ����������  ��
*****��                            �ǩ�
*****��                            ����
*****�� �� �������ש�����
*******���ϩ�    ���ϩ�
*******���ߩ�    ���ߩ�
* @author K.L
* 
* �ź���  PV,P = acquire, V = release
* �ô���
* 1. ������������أ�������������жϵ��Ƿ����������Ա�Lock.Contition �� object.wait��
* 2. ����Ҫӵ���߳̾Ϳ��Խ���PV����
* 
 */
public class TestReadWriteLockWithSemaphore {

	/**
	 * ͬʱֻ������һ������д
	 */
	static Semaphore writeLock = new Semaphore(1);
	static Object lock = new Object();
	/**
	 * û���������д�̻߳������
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

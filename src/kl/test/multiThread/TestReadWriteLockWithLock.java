package kl.test.multiThread;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
* ������Lock ��Condition���������н�һ������ߣ��ĵ�����һ��������ϲ�����
* "A Condition implementation can provide behavior and semantics 
* that is different from that of the Object monitor methods, 
* such as guaranteed ordering for notifications, or not requiring 
* a lock to be held when performing notifications"
* ���������ź����ǣ����е�����ʵ�֣�������Ҫ��ǰ�߳�hold the lock,����signal��signalAll
* �Һã��������������ͼ������������ԣ������Ϳ������ź���Semaphore��������⣨��ʵ����PV������
* �� TestReadWriteLockUseSysnc4
 */
public class TestReadWriteLockWithLock {
	
	static Lock lock = new ReentrantLock();
//	static ReentrantLock readLock = new ReentrantLock();
//	static ReentrantLock writeLock = new ReentrantLock();
	static volatile int readCount = 0;
	static volatile boolean needWrite = false;
	static Condition cRead = lock.newCondition();
	static Condition cWrite = lock.newCondition();
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
				lock.lock();
				try {
					if (needWrite) {// �����ȷ����������while
						try {
							cRead.await();// �Զ��ػ���
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					readCount++;
				} finally {
					lock.unlock();
				}
				reading(i);
			}
		}

		void reading(int i) {
			 System.out.print(name + " read " + needWrite);
			int t = getRandom();
			try {
				Thread.sleep(t);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(name + " read " + i + " times ,content is "
					+ book.toString());
			lock.lock();
			try {
				readCount--;
				if (readCount == 0) {
					//cWrite.signal();
				}
			}
//			catch(Exception ex){
//				System.out.println(ex.getMessage());
//				System.exit(0);
//			}
			finally {
				lock.unlock();
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
				lock.lock();
				try {
					needWrite = true;
					if (readCount != 0) {// �����ȷ����������while
						try {
							cWrite.await();
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
					cRead.signalAll();
				} finally {
					lock.unlock();
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

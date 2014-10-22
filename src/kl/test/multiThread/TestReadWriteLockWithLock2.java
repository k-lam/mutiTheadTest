package kl.test.multiThread;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class TestReadWriteLockWithLock2 {
	static CountDownLatch startLatch = new CountDownLatch(1);
	static StringBuilder book = new StringBuilder("this is a book about...");
	static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Thread(new Reader()).start();
		new Thread(new Reader()).start();
		new Thread(new Writer()).start();
		new Thread(new Reader()).start();
		startLatch.countDown();
	}

	public static class Writer extends Reader {
		//String role = "Writer";
		{
			role = "Writer";
		}
		String[] contents = new String[] { " a story.", "Long long ago,",
				"a preson", " called xiao ming.", "One day,", "he ", "died." };
		WriteLock w = lock.writeLock();

		void write_append(String content) {
				w.lock();
				try {
					System.out.print("writing...");
					Thread.sleep(getRandom() * 3);
					System.out.println(role + id +" writes " + book.append(content));
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					w.unlock();
				}
				try {
					Thread.sleep(getRandom());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}

		@Override
		public void run() {
			try {
				startLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(int i = 0; i != contents.length; i++){
				if(i % 4 == 1){
					read(i / 4);
				}
				write_append(contents[i]);
			}
		}

	}

	public static class Reader implements Runnable {
		static int i = 0;
		int id = i++;
		ReadLock r = lock.readLock();
		String role = "Reader";
		String read(int count) {
			r.lock();
			try {
				System.out.println(role + id + " read " + count + " times ,content is "
						+ book.toString());
				return book.toString();
			} finally {
				r.unlock();
				//System.out.println("finally");
			}
		}

		@Override
		public void run() {
			try {
				startLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (int i = 0; i != 10; i++) {
				int t = getRandom();
				try {
					Thread.sleep(t);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				read(i);
			}
		}

	}

	static Random random = new Random();

	public static int getRandom() {
		return random.nextInt(31) + 20;
	}

}

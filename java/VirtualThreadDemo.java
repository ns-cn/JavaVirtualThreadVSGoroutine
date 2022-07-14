public class VirtualThreadDemo {

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			int finalI = i;
			Thread platformThread = Thread.ofPlatform().name("myPlatformThread").start(() -> System.out.println("hello platform thread"));
			Thread virtualThread = Thread.ofVirtual().name("myVirtualThread").start(() -> System.out.println("hello virtual thread"));
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}

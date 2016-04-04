import te.philips_hue.HueBridgeService;

public class test {
	public static void main(String... args) {
		HueBridgeService.createWithBridgeConnectionCallback("hey", () -> {
			System.out.println("oh shit");
		});
	}
}

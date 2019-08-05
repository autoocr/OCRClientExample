package ocr.fm;

import java.io.IOException;

public class Main {
	public static void main(String args[]) {
		OCRClient client = new OCRClient("0bee89b07a248e27c83fc3d5951213c1");
		try {
			/**
			 * 语文和英语
			 */
			String testPath = "/tmp/1.jpg";
			String b64 = OCRClient.getBase64(testPath);
			OCRResult r = client.doOCR(b64, false);
			System.out.println(r.txtResult.toString());
			
			
			/**
			 * 数学
			 */
			System.out.println("========================\n");
			testPath = "/tmp/051.jpg";
			b64 = OCRClient.getBase64(testPath);
			r = client.doOCR(b64, true);
			System.out.println(r.txtfmlResult.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

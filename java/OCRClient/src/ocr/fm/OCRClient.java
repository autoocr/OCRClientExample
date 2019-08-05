package ocr.fm;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;



import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * OCR识别客户端 封装OCR识别请求
 */
public class OCRClient {
	// 用户id，自己保密
	String uid_;

	//只识别文本，公式也当做文本识别
	final static String api_textonly_url = "http://api.ocr.fm:7080/ocr_text";

	//同时识别文本和公式，公式输出latex格式
	final static String api_textfml_url = "http://api.ocr.fm:7080/ocr_formula";

	public OCRClient(String uid) {
		uid_ = uid;
	}

	/**
	 * @throws IOException
	 *             做OCR识别
	 * 
	 * @param imageBase64
	 *            图像的base64编码
	 * @param uid
	 *            用户id
	 * @return 返回识别的结果
	 * @throws IOException
	 * @throws
	 */
	public OCRResult doOCR(String imageBase64, boolean textFormula) throws IOException {
		String url = (textFormula? api_textfml_url:api_textonly_url);
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		HttpPost httpPost = new HttpPost(url);
		params.add(new BasicNameValuePair("img", imageBase64));
		params.add(new BasicNameValuePair("uid", uid_));
		httpPost.setEntity(new UrlEncodedFormEntity(params));
		    
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		OCRResult r = null;
		try {
			response = httpclient.execute(httpPost);
			// System.out.println(response.getStatusLine().getStatusCode());
			HttpEntity httpEntity = response.getEntity();
			String apiOutput = EntityUtils.toString(httpEntity);
			System.out.println(apiOutput);
			r = OCRResult.parseResult(apiOutput, textFormula);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null)
				response.close();
		}
		return r;
	}

	/**
	 * 对jpeg图像做压缩
	 * 
	 * @param img
	 * @return
	 * @throws IOException
	 */
	public static byte[] CompressJpg(byte[] imgdata, float scale)
			throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(imgdata);
		BufferedImage img = ImageIO.read(bis);

		ByteArrayOutputStream compressed = new ByteArrayOutputStream();
		ImageOutputStream outputStream = new MemoryCacheImageOutputStream(
				compressed);
		ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg")
				.next();
		// Configure JPEG compression: 70% quality
		ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
		jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		jpgWriteParam.setCompressionQuality(scale);
		// Set your in-memory stream as the output
		jpgWriter.setOutput(outputStream);

		// Write image as JPEG w/configured settings to the in-memory stream
		// (the IIOImage is just an aggregator object, allowing you to associate
		// thumbnails and metadata to the image, it "does" nothing)
		jpgWriter.write(null, new IIOImage(img, null, null), jpgWriteParam);
		// Dispose the writer to free resources
		jpgWriter.dispose();

		byte[] jpegData = compressed.toByteArray();
		return jpegData;

	}

	/**
	 * 从图片路径读取图片，转换成base64编码返回 如果图像太大，尝试做jpeg压缩
	 * 
	 * @param path
	 *            图片路径，注意，仅支持jpg格式的图片
	 * @return 图片的base64编码
	 * @throws IOException
	 */
	public static String getBase64(String path) throws IOException {
		File input = new File(path);
		byte[] fileContent = Files.readAllBytes(input.toPath());
		// 如果图像太大，就做一下压缩, 2M以上必须得压缩
		final float sizeLimit = 2 * 1024 * 1024;
		if (fileContent.length > sizeLimit) {
			float scale = 0.7f;
			do {
				System.out.println("before compress: " + fileContent.length
						+ ", scale = " + scale);
				fileContent = CompressJpg(fileContent, scale);
				System.out.println("after compress: " + fileContent.length);

				scale -= 0.1;
				if (scale < 0.2)
					break;
			} while (fileContent.length > sizeLimit);

		}
		Base64 b64 = new Base64();
		return new String(b64.encode(fileContent));
	}
}

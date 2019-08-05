package ocr.fm;

import java.util.ArrayList;

import com.google.gson.Gson;

/**
 * OCR的识别结果
 */
public class OCRResult {
	String Status;
	String Result;
	TextResult txtResult;
	TextFormulaResult txtfmlResult;
	
	public class TextResult {
		// 图片的方向，上下左右
		String orientation;

		// 段落
		ArrayList<Region> regions;

		public class Region {
			// 段落的位置，用包围这个段落的四边形的四个定点表示：x1,y1, x2,y2, x3,y3, x4,y4
			String boundingBox;
			// 语言
			String lang;

			// 行
			public class Line {
				// 行的位置，用包围这个行的四边形的四个定点表示
				String boundingBox;
				// 行文本
				String text;

				// 每行的语言
				String lang;
				
				String type;
				
				String text_height;

				// 行里面的每个字
				public class Word {
					// 字的位置，四个定点
					String boundingBox;

					// 字的内容
					String word;
				}

				ArrayList<Word> words;
			}

			ArrayList<Line> lines;
			
		}
		

		/**
		 * 简单输出里面的文字，用户可以自己修改
		 */
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < regions.size(); ++i) {
				sb.append("--------------------------------\n");
				for (int j = 0; j < regions.get(i).lines.size(); ++j) {
					sb.append(regions.get(i).lines.get(j).text);
					sb.append("\n");
				}
			}

			return sb.toString();
		}
	}

	public class TextFormulaResult {
		// 图片的方向，上下左右
		String orientation;

		// 段落
		ArrayList<Region> regions;

		public class Region {
			// 段落的位置，用包围这个段落的四边形的四个定点表示：x1,y1, x2,y2, x3,y3, x4,y4
			String boundingBox;
			// 语言
			String lang;

			// 行
			public class Line {
				// 行的位置，用包围这个行的四边形的四个定点表示
				String boundingBox;
				// 行文本
				String text;

				// 每行的语言
				String lang;
				
				String type;
				
				String text_height;

				// 行里面的每个字
				public class Word {
					// 字的位置，四个定点
					String boundingBox;

					// 字的内容
					String word;
				}

				ArrayList<Word> words;
			}

			ArrayList<ArrayList<Line> > lines;
			
		}
		

		/**
		 * 简单输出里面的文字，用户可以自己修改
		 */
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < regions.size(); ++i) {
				sb.append("--------------------------------\n");
				for (int j = 0; j < regions.get(i).lines.size(); ++j) {
					for(int k = 0; k < regions.get(i).lines.get(j).size(); ++k){
						if(regions.get(i).lines.get(j).get(k).type.equals("text")){
							sb.append(regions.get(i).lines.get(j).get(k).text);			
						} else {
							sb.append(" $");
							sb.append(regions.get(i).lines.get(j).get(k).text);			
							sb.append("$ ");
						}
					}
					sb.append("\n");
				}
			}

			return sb.toString();
		}
		
	}

		
	/**
	 * 从OCR返回的json串解析OCRResult结构
	 * 
	 * @param http请求OCR的api的返回结果
	 * @return OCRResult 解析的结果
	 */
	public static OCRResult parseResult(String s, boolean textFormula) {
		Gson gson = new Gson();
		OCRResult rs = gson.fromJson(s, OCRResult.class);
		if(rs == null || rs.Result == null){
			return rs;
		}
		
		if(textFormula){
			rs.txtfmlResult = gson.fromJson(rs.Result, OCRResult.TextFormulaResult.class);		
		} else {
			rs.txtResult = gson.fromJson(rs.Result, OCRResult.TextResult.class);
		}
		return rs;
	}

}

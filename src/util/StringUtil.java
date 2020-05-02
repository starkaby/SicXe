package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 문자열 유틸리티
 * 
 * @author Cary
 *
 */
public class StringUtil {
	
	private static final String LITERAL_PATTERN_REGEXP = "^={1}[CX]{1}[']{1}[0-9a-zA-Z]*[']{1}$";
	private static final String LITERAL_FIND_PATTERN_REGEXP = "'[0-9a-zA-Z]*'";
	private static final String DATA_PATTERN_REGEXP = "^[CX]{1}[']{1}[0-9a-zA-Z]*[']{1}$";
	private static final String DATA_FIND_PATTERN_REGEXP = "'[0-9a-zA-Z]*'";
	private static int FIND_TOKEN_INDEX = 0;
	private static int FIND_TOKEN_TYPE = -1; // 0 : END, 1 : TOKEN, 2 : OPERATOR

	/**
	 * 문자열이 널이거나 empty일 경우 빈 문자열 리턴
	 * 
	 * @param val
	 * @return
	 */
	public static String nvl(String val) {
		val = val.trim();

		return nvl(val, "");
	}

	/**
	 * 문자열이 널이거나 empty일 경우 rep 리턴
	 * 
	 * @param val
	 * @param rep
	 * @return
	 */
	public static String nvl(String val, String rep) {
		if (val == null || "".equals(val)) {
			val = rep;
		}
		
		val = val.trim();

		return val;
	}

	/**
	 * 문자열이 널이거나 empty일 경우 빈 문자열 리턴
	 * 
	 * @param val
	 * @return
	 */
	public static boolean isEmpty(String val) {
		return nvl(val, "").equals("");
	}

	/**
	 * 문자열이 모두 숫자로만 이루어져 있는지 확인한다.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNumber(String str) {
		CharSequence cs = str;

		if (StringUtil.isEmpty(str)) {
			return false;
		}

		final int sz = cs.length();

		for (int i = 0; i < sz; i++) {
			if (!Character.isDigit(cs.charAt(i))) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * 문자열이 모두 문자로만 이루어져 있는지 확인한다.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isLetter(String str) {
		CharSequence cs = str;

		if (StringUtil.isEmpty(str)) {
			return false;
		}

		final int sz = cs.length();

		for (int i = 0; i < sz; i++) {
			if (!Character.isLetter(cs.charAt(i))) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * 문자열이 리터럴인지 확인한다.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isLiteral(String str) {
		if(StringUtil.isEmpty(str)) {
			return false;
		}
		
        return Pattern.matches(LITERAL_PATTERN_REGEXP, str);
	}
	
	/**
	 * 리터럴 문자열에서 문자 부분만 추출한다.
	 * 
	 * @param str
	 * @return
	 */
	public static String getLiteral(String str) {
		if(!StringUtil.isLiteral(str)) {
			return null;
		}
		
		Pattern pattern = Pattern.compile(LITERAL_FIND_PATTERN_REGEXP);
		Matcher matcher = pattern.matcher(str);
		
		if(matcher.find()) {
			String matchers = matcher.group();
			matchers = matchers.replace("'", "");
			
			return matchers;
		}
		
		return null;
	}
	
	/**
	 * 리터럴의 타입을 추출한다.
	 * 
	 * @param str
	 * @return
	 */
	public static Character getLiteralType(String str) {
		if(!StringUtil.isLiteral(str)) {
			return null;
		}
		
		return str.charAt(1);
	}
	
	/**
	 * 수식인지 조회한다.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isArithmetic(String str) {
		if(!str.startsWith("+") && !str.startsWith("-") // 시작점이 수식인것은 제외
				&& !str.startsWith("*") && !str.startsWith("/")) {
			if(str.contains("-") || str.contains("+")  // 수식 기호를 하나라도 포함하면
					|| str.contains("*") || str.contains("/")) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 수식을 문자열의 배열로 잘라서 조회
	 * 
	 * @param str
	 * @return
	 */
	public static String[] getArithmetic(String str) {
		if(!StringUtil.isArithmetic(str)) {
			return null;
		}
		
		// 데이터 초기화
		FIND_TOKEN_INDEX = 0;
		FIND_TOKEN_TYPE = -1;
		int i = 0;
		
		String[] arithmetic = new String[10];
		String data = StringUtil.getToken(str);
		
		while(FIND_TOKEN_TYPE != 0) {
			arithmetic[i++] = data;
			data = StringUtil.getToken(str);
		}
		
		return arithmetic;
	}
	
	/**
	 * 어드레싱 모드를 반환한다.
	 * 
	 * @param str
	 * @return
	 */
	public static int getAddressingMode(String str) {
		if(str.startsWith("@")) { // 간접 참조
			return 32;
		} else if(str.startsWith("#")) { // 직접 참조
			return 16;
		}
		
		return 32+16;
	}
	
	/**
	 * 문자열을 받아 레지스터의 번호를 반환한다.
	 * 
	 * @param str
	 * @return
	 */
	public static int getRegisterNumber(String str) {
		if(StringUtil.isEmpty(str)) {
			return 0;
		}
		
		if(str.equals("A")) {
			return 0;
		} else if(str.equals("X")) {
			return 1;
		} else if(str.equals("L")) {
			return 2;
		} else if(str.equals("B")) {
			return 3;
		} else if(str.equals("S")) {
			return 4;
		} else if(str.equals("T")) {
			return 5;
		} else if(str.equals("F")) {
			return 6;
		} else if(str.equals("PC")) {
			return 8;
		} else if(str.equals("SW")) {
			return 9;
		}
		
		return 0;
	}
	
	/**
	 * 문자열이 특수 형식을 가진 문자열인지 확인한다.
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isForm(String str) {
		if(StringUtil.isEmpty(str)) {
			return false;
		}
		
        return Pattern.matches(DATA_PATTERN_REGEXP, str);
	}
	
	/**
	 * 특수 형식 문자열에서 문자 부분만 추출한다.
	 * 
	 * @param str
	 * @return
	 */
	public static String getFormDataToHex(String str) {
		if(!StringUtil.isForm(str)) {
			return null;
		}
		
		Pattern pattern = Pattern.compile(DATA_FIND_PATTERN_REGEXP);
		Matcher matcher = pattern.matcher(str);
		
		if(matcher.find()) {
			String hexData = "";
			String matchers = matcher.group();
			matchers = matchers.replace("'", "");
			
			if(str.charAt(0) == 'X') {
				hexData = matchers;
			} else if(str.charAt(0) == 'C') {
				for(int i = 0; i < matchers.length(); i++) {
					hexData = hexData + String.format("%02X", str.charAt(i));
				}
			}
			
			return matchers;
		}
		
		return null;
	}
	
	/**
	 * 문자열을 토큰 단위로 자르는 함수 
	 * 
	 * @param str
	 * @return
	 */
	private static String getToken(String str) {
		if(FIND_TOKEN_INDEX >= str.length()) { // 문자열의 크기가 끝이라면
			FIND_TOKEN_TYPE = 0;
			return null;
		}
		
		if(Character.isLetter(str.charAt(FIND_TOKEN_INDEX))) { // 문자라면
			String newStr = "";
			
			for(int i = FIND_TOKEN_INDEX; i < str.length(); i++) { // 문자가 끝날 때 까지 반복
				if(Character.isLetter(str.charAt(i))) {
					newStr = newStr + str.charAt(i);
					FIND_TOKEN_INDEX++;
				} else {
					break;
				}
			}
			
			FIND_TOKEN_TYPE = 1; // 토큰
			return newStr;
		} else if(str.charAt(FIND_TOKEN_INDEX) == '+' || str.charAt(FIND_TOKEN_INDEX) == '-' // 수식 기호의 경우 수식을 리턴
				|| str.charAt(FIND_TOKEN_INDEX) == '*' || str.charAt(FIND_TOKEN_INDEX) == '/') {
			
			char symbol = str.charAt(FIND_TOKEN_INDEX);
			
			FIND_TOKEN_INDEX++;
			FIND_TOKEN_TYPE = 2; // 수식
			
			return String.valueOf(symbol);
		}
		
		FIND_TOKEN_TYPE = 0;
		return null;
	}
}

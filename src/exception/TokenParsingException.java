package exception;

/**
 * 토큰 파싱 과정 수행 중 오류가 발생시 호출
 * 
 * @author Cary
 *
 */
public class TokenParsingException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	public TokenParsingException() {
		super("An error occurred during the token parsing.");
	}
	
	
	public TokenParsingException(String msg) {
		super(msg);
	}
}

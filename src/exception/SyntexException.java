package exception;

/**
 * 토큰 파싱 과정 수행 중 오류가 발생시 호출
 * 
 * @author Cary
 *
 */
public class SyntexException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	public SyntexException() {
		super("A syntex error occurred.");
	}
	
	
	public SyntexException(String msg) {
		super(msg);
	}
}

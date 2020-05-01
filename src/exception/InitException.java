package exception;

/**
 * Init 과정 수행 중 오류가 발생시 호출
 * 
 * @author Cary
 *
 */
public class InitException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	public InitException() {
		super("An error occurred during initialization.");
	}
	
	
	public InitException(String msg) {
		super(msg);
	}
}

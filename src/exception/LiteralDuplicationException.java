package exception;

/**
 * 리터럴이 중복 등록되었을 때 호출
 * 
 * @author Cary
 *
 */
public class LiteralDuplicationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LiteralDuplicationException() {
		super("Duplicate literal exist.");
	}
}

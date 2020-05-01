package exception;

/**
 * 리터럴이 없을 때 호출
 * 
 * @author Cary
 *
 */
public class LiteralNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LiteralNotFoundException() {
		super("Litera not found.");
	}
}

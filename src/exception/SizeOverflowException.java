package exception;

/**
 * 할당한 크기보다 큰 데이터가 발생했을 때
 * 
 * @author Cary
 *
 */
public class SizeOverflowException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SizeOverflowException() {
		super("Data overflow.");
	}
}

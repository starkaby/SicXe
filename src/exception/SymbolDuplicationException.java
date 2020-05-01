package exception;

/**
 * 심볼이 중복 등록되었을 때 호출
 * 
 * @author Cary
 *
 */
public class SymbolDuplicationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SymbolDuplicationException() {
		super("Duplicate symbol exist.");
	}
}

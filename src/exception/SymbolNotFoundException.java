package exception;

/**
 * 심볼이 없을 때 호출
 * 
 * @author Cary
 *
 */
public class SymbolNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SymbolNotFoundException() {
		super("Symbol not found.");
	}
}

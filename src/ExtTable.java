import java.util.ArrayList;
import java.util.Arrays;

/**
 * 외부 정의와 외부 참조를 관리하는 클래스
 * 
 * @author Cary
 *
 */
public class ExtTable {
	ArrayList<Extdef> extdef; // 외부 정의
	ArrayList<Extref> extref; // 외부 참조
	
	public ExtTable() {
		this.extdef = new ArrayList<>();
		this.extref = new ArrayList<>();
	}
	
	/**
	 * 외부 정의 추가
	 * 
	 * @param str
	 */
	public void addExtdef(String str) { 
		String[] data = str.split(",");
		
		Arrays.stream(data).forEach(x-> extdef.add(new Extdef(x)));
	}
	
	/**
	 * 외부 참조 추가
	 * 
	 * @param str
	 */
	public void addExtref(String str) {
		String[] data = str.split(",");
		
		Arrays.stream(data).forEach(x-> extref.add(new Extref(x)));
	}
}


/**
 * 외부 정의 객체
 * 
 * @author Cary
 *
 */
class Extdef {
	public String name;
	public int location;
	
	public Extdef(String name) {
		this.name = name;
		this.location = -1;
	}
	
	public void modifyExtdef(int location) {
		this.location = location;
	}
}

/**
 * 외부 참조 객체
 * 
 * @author Cary
 *
 */
class Extref {
	public String name;
	
	public Extref(String name) {
		this.name = name;
	}
}
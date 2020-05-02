import java.util.ArrayList;
import java.util.Arrays;

import exception.SymbolNotFoundException;

/**
 * 외부 정의와 외부 참조를 관리하는 클래스
 * 
 * @author Cary
 *
 */
public class ExtTable {
	ArrayList<Extdef> extdef; // 외부 정의
	ArrayList<Extref> extref; // 외부 참조
	
	private static StringBuilder stringBuilder = new StringBuilder();
	
	public ExtTable() {
		this.extdef = new ArrayList<>();
		this.extref = new ArrayList<>();
	}
	
	/**
	 * 외부 정의 추가
	 * 
	 * @param str
	 */
	public void addExtdef(String[] data) { 
		Arrays.stream(data).forEach(x-> extdef.add(new Extdef(x)));
	}
	
	/**
	 * 외부 참조 추가
	 * 
	 * @param str
	 */
	public void addExtref(String[] data) {
		Arrays.stream(data).forEach(x-> extref.add(new Extref(x)));
	}
	
	/**
	 * 외부 정의 정보를 검증한다.
	 * 
	 * @param symbolTab
	 */
	public void validation(SymbolTable symbolTab) {
		extdef.stream().forEach(x-> {
			int symbolIndex = symbolTab.search(x.name);
			
			if(symbolIndex == -1) {
				throw new SymbolNotFoundException();
			}
			
			x.modifyExtdef(symbolTab.locationList.get(symbolIndex));
		});
	}

	/**
	 * 외부 참조 영역에 데이터가 있는지 확인한다.
	 * 
	 * @param str
	 * @return
	 */
	public boolean isExtref(String str) {
		return extref.stream()
				     .anyMatch(x-> x.name.equals(str));
	}
	
	/**
	 * 외부 정의 영역을 오브젝트 프로그램 형식에 맞추어 출력한다.
	 * 
	 * @return
	 */
	public String printDef() {
		stringBuilder.setLength(0);
		
		stringBuilder.append("D");
		this.extdef.forEach(x-> stringBuilder.append(x.print()));
		stringBuilder.append("\n");
		
		return stringBuilder.toString();
	}
	
	
	/**
	 * 외부 참조 영역을 오브젝트 프로그램 형식에 맞추어 출력한다.
	 * 
	 * @return
	 */
	public String printRef() {
		stringBuilder.setLength(0);
		
		stringBuilder.append("R");
		this.extref.forEach(x-> stringBuilder.append(String.format("%-6s", x.name)));
		stringBuilder.append("\n");
		
		return stringBuilder.toString();
	}
}


/**
 * 외부 정의 객체
 * 
 * @author Cary
 *
 */
class Extdef {
	// 이름
	public String name;
	// 주소
	public int location;
	
	private static StringBuilder stringBuilder = new StringBuilder();
	
	public Extdef(String name) {
		this.name = name;
		this.location = -1;
	}
	
	/**
	 * 외부 정의 객체의 주소를 수정한다.
	 * 
	 * @param location
	 */
	public void modifyExtdef(int location) {
		this.location = location;
	}
	
	/**
	 * 외부 정의 객체를 오브젝트 프로그램 형식에 맞추어 출력한다.
	 * 
	 * @return
	 */
	public String print() {
		stringBuilder.setLength(0);
		
		stringBuilder.append(String.format("%-6s", name))
				     .append(String.format("%06X", location));
	
		return stringBuilder.toString();
	}
}

/**
 * 외부 참조 객체
 * 
 * @author Cary
 *
 */
class Extref {
	// 이름
	public String name;
	
	private static StringBuilder stringBuilder = new StringBuilder();
	
	public Extref(String name) {
		this.name = name;
	}
	
	/**
	 * 외부 참조 객체를 오브젝트 프로그램 형식에 맞추어 출력한다.
	 * 
	 * @return
	 */
	public String print() {
		stringBuilder.setLength(0);
		
		stringBuilder.append(String.format("%-6s", name));
	
		return stringBuilder.toString();
	}
}
import java.util.ArrayList;

/**
 * 수정이 필요한 정보를 담아두는 테이블
 * 
 * @author Cary
 *
 */
public class ModifyTable {
	ArrayList<Modify> modifyList;
	
	public ModifyTable() {
		modifyList = new ArrayList<>();
	}
	
	/**
	 * 수정이 필요한 정보를 저장한다.
	 * 
	 * @param location
	 * @param length
	 * @param sign
	 * @param label
	 */
	public void add(int location, int length, char sign, String label) {
		modifyList.add(new Modify(location, length, sign, label));
	}
}

/**
 * 수정 정보 객체
 * 
 * @author Cary
 *
 */
class Modify {
	public int location; // 주소 값
	public int lenght; // 단위 : 니블
	public char sign; // 부호
	public String label; // 레이블
	
	private static StringBuilder stringBuilder = new StringBuilder();
	
	public Modify(int location, int length, char sign, String label) {
		this.location = location;
		this.lenght = length;
		this.sign = sign;
		this.label = label;
	}
	
	/**
	 * 수정 정보를 오브젝트 프로그램 형식에 맞추어 출력한다.
	 * 
	 * @return
	 */
	public String print() {
		stringBuilder.setLength(0);
		
		stringBuilder.append("M")
					 .append(String.format("%06X", location))
				     .append(String.format("%02X", lenght))
					 .append(sign)
					 .append(label)
					 .append("\n");
	
		return stringBuilder.toString();
	}
}

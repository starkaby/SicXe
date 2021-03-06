import java.util.ArrayList;

import exception.LiteralDuplicationException;
import exception.LiteralNotFoundException;

/**
 * literal과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class LiteralTable {
	ArrayList<String> literalList;
	ArrayList<Integer> locationList;
	ArrayList<Character> literalTypeList;
	// 기타 literal, external 선언 및 처리방법을 구현한다.
	
	/**
	 * 생성자 선언
	 */
	public LiteralTable() {
		this.literalList = new ArrayList<>();
		this.locationList = new ArrayList<>();
		this.literalTypeList = new ArrayList<>();
	}
	
	/**
	 * 새로운 Literal을 table에 추가한다.
	 * @param literal : 새로 추가되는 literal의 label
	 * @param location : 해당 literal이 가지는 주소값
	 * @param literalType : 해당 literal의 타입
	 * 주의 : 만약 중복된 literal이 putLiteral을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifyLiteral()을 통해서 이루어져야 한다.
	 */
	public void putLiteral(String literal, int location, Character literalType) {
		int literalIndex = this.search(literal);
		
		if(literalIndex > -1) {
			throw new LiteralDuplicationException();
		}
		
		this.literalList.add(literal);
		this.locationList.add(location);
		this.literalTypeList.add(literalType);
	}
	
	/**
	 * 기존에 존재하는 literal 값에 대해서 가리키는 주소값을 변경한다.
	 * @param literal : 변경을 원하는 literal의 label
	 * @param newLocation : 새로 바꾸고자 하는 주소값
	 */
	public void modifyLiteral(String literal, int newLocation) {
		int literalIndex = this.search(literal);
		
		if(literalIndex == -1) {
			throw new LiteralNotFoundException();
		}
		
		this.locationList.set(literalIndex, newLocation);
	}
	
	/**
	 * 인자로 전달된 literal이 어떤 주소를 지칭하는지 알려준다. 
	 * @param literal : 검색을 원하는 literal의 label
	 * @return literal이 가지고 있는 주소값. 해당 literal이 없을 경우 -1 리턴
	 */
	public int search(String literal) {
		int address = 0;
		
		for(String lit : literalList) { // 리터럴을 반복하여
			if(lit.equals(literal)) {
				return address; // 해당 리터럴이 찾는 리터럴일 경우 리턴
			}
			
			address++;
		}
		
		return -1;
	}
	
	
	/**
	 * 리터럴을 출력 형태로 반환한다.
	 * 
	 * @return
	 */
	public String print() {
		String data = "";
		
		for(int i = 0; i < literalList.size(); i++) {
			if(literalTypeList.get(i) == 'X') {
				data = literalList.get(i);
			} else if(literalTypeList.get(i) == 'C') {
				for(int z = 0; z < literalList.get(i).length(); z++) {
					data = data + String.format("%02X", (int)literalList.get(i).charAt(z));
				}
			}
		}
		
		return data;
	}
}

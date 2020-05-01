import java.util.ArrayList;

import exception.SymbolDuplicationException;
import exception.SymbolNotFoundException;

/**
 * symbol과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	// 기타 literal, external 선언 및 처리방법을 구현한다.
	
	/**
	 * 생성자 선언
	 */
	public SymbolTable() {
		this.symbolList = new ArrayList<>();
		this.locationList = new ArrayList<>();
	}
	
	/**
	 * 새로운 Symbol을 table에 추가한다.
	 * @param symbol : 새로 추가되는 symbol의 label
	 * @param location : 해당 symbol이 가지는 주소값
	 * 주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
	 */
	public void putSymbol(String symbol, int location) {
		int symbolIndex = this.search(symbol); // 심볼 검색
		
		// 심볼이 존재 할 경우
		if(symbolIndex > -1) {
			throw new SymbolDuplicationException(); // 심볼 중복 exception 발생
		}
		
		// 심볼테이블 추가
		this.symbolList.add(symbol);
		this.locationList.add(location);
	}
	
	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * @param symbol : 변경을 원하는 symbol의 label
	 * @param newLocation : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newLocation) {
		int symbolIndex = this.search(symbol); // 심볼 검색
		
		// 심볼이 존재하지 않을 경우
		if(symbolIndex == -1) {
			throw new SymbolNotFoundException(); // 심볼 없음 exception 발생
		}

		// 심볼테이블 업데이트
		this.locationList.set(symbolIndex, newLocation);
	}
	
	/**
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다. 
	 * @param symbol : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
	 */
	public int search(String symbol) {
		int address = 0;
		
		for(String sym : symbolList) { // 심볼을 반복하여
			if(sym.equals(symbol)) {
				return address; // 해당 심볼이 찾는 심볼일 경우 리턴
			}
			
			address++;
		}
		
		return -1;
	}
}

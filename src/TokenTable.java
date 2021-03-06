import java.util.ArrayList;
import java.util.Arrays;

import exception.LiteralNotFoundException;
import exception.SizeOverflowException;
import exception.SymbolNotFoundException;
import exception.SyntexException;
import util.StringUtil;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	LiteralTable literalTab;
	InstTable instTab;
	ExtTable extTab;
	Section section;
	ModifyTable modifyTab;
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab, LiteralTable literalTab, 
			ExtTable extTab, Section section, ModifyTable modifyTab) {
		tokenList = new ArrayList<>(); // 토큰 리스트 생성
		
		this.symTab = symTab; // 섹션의 심볼테이블 연결
		this.instTab = instTab; // 기계어 테이블 연결
		this.literalTab = literalTab; // 리터럴 테이블 연결
		this.extTab = extTab; // 외부테이블 연결
		this.section = section; // 섹션 연결
		this.modifyTab = modifyTab; // 수정 테이블 연결
	}
	
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		// 토큰 생성
		Token token = new Token(line);
		
		// 토큰 검토
		token.validation(instTab.findByOperator(token.operator));
		
		// 토큰테이블에 추가
		tokenList.add(token);
	}
	
	/**
	 * 토큰을 받아 토큰 테이블에 추가한다.
	 * 
	 * @param token
	 */
	public void setToken(Token token) {
		tokenList.add(token);
	}
	
	
	/**
	 * 마지막 토큰을 삭제한다. (CSECT)
	 */
	public void removeLastToken() {
		if(tokenList.size() > 0) { // 토큰 리스트의 사이즈가 하나라도 있을 경우
			tokenList.remove(tokenList.size() - 1); // 마지막 토큰 삭제
		}
	}
	
	/**
	 * 해당 토큰 테이블의 토큰을 순서대로 반복하며 주소값을 할당한다.
	 * 또한, 일부 어셈블러 지시자도 같이 분석한다.
	 */
	public void setLocation() {
		int location = 0;
		Instruction instruction = null;
		
		// 토큰 반복
		for(Token token : tokenList) {
			
			// 주소값 할당 begin --
			instruction = instTab.findByOperator(token.operator);
			
			token.location = location; // 토큰에 주소값 할당
			location = location + instruction.format; // 일반 명령어는 포맷만큼 증가
			// 주소값 할당 end --
			
			// 어셈블리 지시어 처리 begin --
			if(StringUtil.nvl(instruction.operator).equals("START") 
					|| StringUtil.nvl(instruction.operator).equals("CSECT")) { // 신규 섹션 시작 시
				if(!StringUtil.isEmpty(token.label)) { // 레이블이 반드시 존재해야 함
					this.section.programName = token.label;
					
					if(StringUtil.nvl(instruction.operator).equals("START")) { // START 경우에만 시작 주소 할당 
						this.section.isMain = true; // START는 메인 섹션
						if(StringUtil.isNumber(token.operand[0])) { // 시작 주소는 숫자
							this.section.startAddress = Integer.parseInt(token.operand[0]);
						} else {
							throw new SyntexException("A number must be entered for this parameter."); // 파라미터는 반드시 숫자여야 함
						}
					}
				} else { 
					throw new SyntexException("START instruction label cannot be null."); // 스타트 명령은 반드시 레이블이 존재해야 함
				}
				
			} else if(StringUtil.nvl(instruction.operator).equals("EXTDEF")) { // 외부 정의 접촉 시 외부 참조 테이블에 추가 (Pass1에서는 데이터만 추가하고 pass2에서 검증한다)
				this.extTab.addExtdef(token.operand);
			} else if(StringUtil.nvl(instruction.operator).equals("EXTREF")) { // 외부 참조 접촉 시 외부 참조 테이블에 추가 (Pass1에서는 데이터만 추가하고 pass2에서 검증한다)
				this.extTab.addExtref(token.operand);
			} else if(StringUtil.nvl(instruction.operator).equals("RESW")) { // 해당 지시어 접촉 시 매개변수 만큼 메모리 공간 확보 (3byte)
				if(StringUtil.isNumber(token.operand[0])) {
					location = location + (Integer.parseInt(token.operand[0]) * 3);
				} else {
					throw new SyntexException("A number must be entered for this parameter.");
				}
			} else if(StringUtil.nvl(instruction.operator).equals("RESB")) { // 해당 지시어 접촉 시 매개변수 만큼 메모리 공간 확보 (1byte)
				if(StringUtil.isNumber(token.operand[0])) {
					location = location + Integer.parseInt(token.operand[0]);
				} else {
					throw new SyntexException("A number must be entered for this parameter.");
				}
			} else if(StringUtil.nvl(instruction.operator).equals("EQU")) { // 해당 지시어 접촉 시 메모리의 공간을 변경한다.
				if(token.operand[0].equals("*")) { // 현재 메모리 주소를 주소값으로 설정
					token.location = location;
				} else if(StringUtil.isArithmetic(token.operand[0])) { // 수식의 경우
					String[] arithmetics = StringUtil.getArithmetic(token.operand[0]);
					char arithmeticSymbol = 0; 
					
					for(String arithmetic : arithmetics) { // 메모리 주소 계산
						if(!StringUtil.isEmpty(arithmetic)) {
							if(StringUtil.isLetter(arithmetic)) { // 변수의 경우
								int symbolIndex = this.symTab.search(arithmetic); // 심볼 테이블 조회
								
								if(symbolIndex == -1) { // 심볼이 없으면 예외
									throw new SymbolNotFoundException();
								}
								
								if(arithmeticSymbol == 0) { // 최초 사칙연산
									token.location = this.symTab.locationList.get(symbolIndex);
								} else if(arithmeticSymbol == '+') {
									token.location = token.location + this.symTab.locationList.get(symbolIndex);
								} else if(arithmeticSymbol == '-') {
									token.location = token.location - this.symTab.locationList.get(symbolIndex);
								} else if(arithmeticSymbol == '*') {
									token.location = token.location * this.symTab.locationList.get(symbolIndex);
								} else if(arithmeticSymbol == '/') {
									token.location = token.location / this.symTab.locationList.get(symbolIndex);
								}
							} else { // 사칙연산 기호의 경우
								arithmeticSymbol = arithmetic.charAt(0);
							}
						}
					}
				} else if(StringUtil.isNumber(token.operand[0])) {
					token.location = Integer.parseInt(token.operand[0]);
				}
			} else if(StringUtil.nvl(instruction.operator).equals("LTORG") || StringUtil.nvl(instruction.operator).equals("END")) { // 리터럴 할당
				
				for(int i = 0; i < literalTab.literalList.size(); i++) { // 현재 섹션의 리터럴 반복
					// 리터럴 정보 조회
					String literal = literalTab.literalList.get(i); 
					int literalLocation = literalTab.locationList.get(i);
					char literalType = literalTab.literalTypeList.get(i);
					
					if(literalLocation == -1) { // 리터럴의 주소가 아직 할당되지 않았을 때
						literalTab.modifyLiteral(literal, location);
						
						location = (int)(location + (literal.length() * (literalType == 'C' ? 1 : 0.5)));
					}
				}
				
			}
			// 어셈블리 지시어 처리 end --
			
			// 심볼테이블 등록
			if(token.label != null) { 
				symTab.putSymbol(token.label, token.location);
			}
			
			// 리터럴 테이블 등록
			if(token.operand != null) {
				Arrays.stream(token.operand)
					  .filter(x -> StringUtil.isLiteral(x)) // 리터럴 검색
					  .forEach(x -> { // 리터럴을 반복하여 리터럴 테이블에 등록
						  String literal = StringUtil.getLiteral(x);
						  
						  if(literalTab.search(literal) == -1) { // 리터럴 테이블에 등록되어 있지 않을 경우에만 등록하도록 처리
							  literalTab.putLiteral(literal, -1, StringUtil.getLiteralType(x));
						  }
					  });
			}
		}
		
		section.programLength = location;
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table literal table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(){
		// pass2 에서 사용하는 변수 초기화
		Instruction instruction = null;
		StringBuilder binaryObjectCode = new StringBuilder();
		int addressingMode = 0;
		
		// 토큰 반복
		for(Token token : tokenList) {
			// 명령 정보 조회
			instruction = instTab.findByOperator(token.operator);
			
			// 빌더 초기화
			binaryObjectCode.setLength(0); 
					
			// nixbpe 설정 begin --
			if(token.operand != null) {
				addressingMode = StringUtil.getAddressingMode(token.operand[0]);
				token.setFlag(addressingMode, 1); // 어드레싱 모드 설정
				
				if(token.operand.length > 1 
						&& StringUtil.nvl(token.operand[1]).equals("X")) {
					token.setFlag(xFlag, 1);
				}
				
				if(instruction.format == 3 // 포맷 3 형식이면서
						&& (addressingMode == nFlag || addressingMode == (nFlag+iFlag))) { // 어드레싱 모드가 간접 참조거나, SIC/XE모드 일 경우
					token.setFlag(pFlag, 1);
				}
				
				if(instruction.format == 4) { // 포맷 4 형식의 경우
					token.setFlag(eFlag, 1);
				}
			}
			// nixbpe 설정 end --
			
			// object code 설정 begin --
			if(instruction.opcode != -1) { // 어셈블리어 지시자가 아닌 경우만 처리, 어셈블리어 지시자는 뒤에서 처리토록 함.
				String opcodeBinary = Integer.toBinaryString(0x100 | instruction.opcode).substring(1);
				
				if(instruction.format == 1) { // 포맷 1의 경우
					binaryObjectCode.append(opcodeBinary); // 포맷 1은 opcode를 8비트 모두 사용한다.
				} else if(instruction.format == 2) { // 포맷 2의 경우
					binaryObjectCode.append(opcodeBinary); // 포맷 2는 opcode를 8비트 모두 사용한다.
					
					int registerNo = StringUtil.getRegisterNumber(token.operand[0]); // 포맷 2의 첫 파라미터는 무조건 레지스터 번호
					
					String binaryRegisterNo = Integer.toBinaryString(0x10 | registerNo).substring(1); // 레지스터 번호를 바이너리로 변경
					binaryObjectCode.append(binaryRegisterNo); // 코드에 추가
					
					if(instruction.operator.equals("SHIFTR") 
							|| instruction.operator.equals("SHIFTL")) { // 포맷 2 명령어중 다음 명령어는 2번째 오퍼랜드에 숫자가 들어 간다.
						binaryRegisterNo = Integer.toBinaryString(0x10 | Integer.parseInt(token.operand[1])).substring(1);
						binaryObjectCode.append(binaryRegisterNo);
					} else {
						if(token.operand.length > 1 && !StringUtil.isEmpty(token.operand[1])) {
							registerNo = StringUtil.getRegisterNumber(token.operand[1]); // 포맷 2의 두번째 파라미터는 레지스터거나 없음
							
							binaryRegisterNo = Integer.toBinaryString(0x10 | registerNo).substring(1); // 레지스터 번호를 바이너리로 변경
							binaryObjectCode.append(binaryRegisterNo);
						} else {
							binaryRegisterNo = Integer.toBinaryString(0x10 | 0).substring(1); // 레지스터 번호를 바이너리로 변경
							binaryObjectCode.append(binaryRegisterNo);
						}
					}
				} else if(instruction.format == 3 || instruction.format == 4) { // 포맷 3혹은 4의 경우
					
					if(instruction.operator.equals("RSUB")) {
						binaryObjectCode.setLength(0);
						binaryObjectCode.append(Integer.toBinaryString(0x4F0000));
					} else {
						opcodeBinary = opcodeBinary.substring(0, 6); // 6비트만 자르기
						binaryObjectCode.append(opcodeBinary); // 포맷 3, 4는 opcode를 6비트 사용한다.
						
						String nixbpeBinary = Integer.toBinaryString(0b1000000 | token.nixbpe).substring(1); // nixbpe를 바이너리 형태로 변경
						binaryObjectCode.append(nixbpeBinary); // 포맷 3, 4는 nixbpe를 사용
						
						int disp = 0;
						String operand = token.operand[0];
						
						if(addressingMode != (nFlag+iFlag)) { // 간접참조거나 직접참조의 경우
							operand = operand.substring(1);
						} else if(StringUtil.isLiteral(operand)) {
							String literal = StringUtil.getLiteral(operand);
							
							int literalIndex = literalTab.search(literal);
							
							if(literalIndex > -1) {
								disp = literalTab.locationList.get(literalIndex) - (token.location + instruction.format); // target - PC;
							} else {
								throw new LiteralNotFoundException(); // 리터럴이 없을 때
							}
						}
						
						// 주소 처리
						if(disp == 0) { // 리터럴은 이미 주소값을 구했으므로 패스
							if(StringUtil.isLetter(operand)) { // 심볼의 경우
								int symbolIndex = symTab.search(operand);
								
								if(symbolIndex > -1) {
									disp = symTab.locationList.get(symbolIndex);
									
									if(instruction.format == 3) {
										disp = disp - (token.location + instruction.format);
									}
								} else {
									if(extTab.isExtref(operand)) {
										disp = 0;
										
										modifyTab.add(token.location+1, 5, '+', operand);
									} else {
										throw new SymbolNotFoundException();
									}
								}
							} else if(StringUtil.isNumber(operand)) { // 숫자의 경우
								disp = Integer.parseInt(operand);
							}
						}
						
						String targetBinary = "";
						
						if(instruction.format == 3) {
							targetBinary = Integer.toBinaryString(0x1000 | disp).substring(1);
							targetBinary = targetBinary.substring(targetBinary.length()-12, targetBinary.length());
						} else if(instruction.format == 4) {
							targetBinary = Integer.toBinaryString(0x100000 | disp).substring(1);
							targetBinary = targetBinary.substring(targetBinary.length()-20, targetBinary.length());
						}
						
						binaryObjectCode.append(targetBinary); // 최종 Object Code
					}
				}
				
				int objectCode = Integer.parseInt(binaryObjectCode.toString(), 2);
				
				String hexObjectCode = String.format("%0"+(instruction.format * 2)+"X", objectCode);
				
				token.objectCode = hexObjectCode;
				token.byteSize = token.objectCode.length() / 2;
			// object code 설정 end --
			} else if(instruction.operator.equals("BYTE") || instruction.operator.equals("WORD")) { // 해당 내용은 오브젝트 코드를 할당해야함
				String operand = token.operand[0];
				
				if(StringUtil.isForm(operand)) { // 형식이 있는 매개변수의 경우 (EX: X'05')
					String hexData = StringUtil.getFormDataToHex(operand); // 데이터를 HEX 형식으로 변경
					
					if((hexData.length() / 2) <= instruction.format) { // 해당 데이터가 오버플로우인지 확인
						token.objectCode = hexData; // 데이터 할당
						token.byteSize = token.objectCode.length() / 2;
					} else {
						throw new SizeOverflowException(); 
					}
				} else if(StringUtil.isArithmetic(operand)) { // 수식의 경우
					String[] arithmetics = StringUtil.getArithmetic(operand); // 수식 정보 조회
					char arithmeticSymbol = 0; // 수식 심볼
					
					for(String arithmetic : arithmetics) { // 수식 반복
						if(!StringUtil.isEmpty(arithmetic)) {
							if(StringUtil.isLetter(arithmetic)) { // 심볼의 경우
								int symbolIndex = this.symTab.search(arithmetic); // 심볼 테이블 조회
								
								if(symbolIndex == -1) { 
									if(extTab.isExtref(arithmetic)) { // 심볼이 아니지만 외부 참조의 경우
										arithmeticSymbol = arithmeticSymbol == 0 ? '+' : arithmeticSymbol; // 최초 데이터는 +로 
										
										modifyTab.add(token.location, instruction.format*2, arithmeticSymbol, arithmetic); // 수정 테이블 등록
									} else {
										throw new SymbolNotFoundException(); // 심볼이 없을 경우
									}
								} 
							} else { // 사칙연산 기호의 경우
								arithmeticSymbol = arithmetic.charAt(0);
							}
						}
					}
					
					if(instruction.format == 3) { // word
						token.objectCode = Integer.toHexString(0x1000000 | 0).substring(1); // 6자리 할당
					} else { // byte
						token.objectCode = Integer.toHexString(0x100 | 0).substring(1); // 2자리 할당
					}
					
					token.byteSize = token.objectCode.length() / 2; 
				} else if(StringUtil.isLetter(operand)) { // 문자의 경우
					int symbolIndex = this.symTab.search(operand); // 심볼 테이블 조회
					
					if(symbolIndex == -1) { 
						if(extTab.isExtref(operand)) { // 심볼이 아니지만 외부 참조의 경우
							modifyTab.add(token.location, instruction.format*2, '+', operand); // 수정 테이블 등록
						} else {
							throw new SymbolNotFoundException();
						}
					} 
					
					if(instruction.format == 3) { // word
						token.objectCode = Integer.toHexString(0x1000000 | 0).substring(1); // 6자리 할당
					} else { // byte
						token.objectCode = Integer.toHexString(0x100 | 0).substring(1); // 2자리 할당
					}
					
					token.byteSize = token.objectCode.length() / 2;
				} else if(StringUtil.isNumber(operand)) { // 숫자의 경우
					if(instruction.format == 3) { // word
						token.objectCode = Integer.toHexString(0x1000000 | Integer.parseInt(operand)).substring(1); // 6자리 할당
					} else { // byte
						token.objectCode = Integer.toHexString(0x100 | Integer.parseInt(operand)).substring(1); // 2자리 할당
					}
					
					token.byteSize = token.objectCode.length() / 2;
				}
			}
		}
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * Object Program을 생성하여 문자열의 형태로 리턴한다.
	 * @param index
	 */
	public String makeObjectProgram() {
		StringBuilder stringBuilder = new StringBuilder();
		
		// 오브젝트 프로그램의 헤더 영역 설정
		stringBuilder.append(String.format("H%-6s%06X%06X\n", section.programName, section.startAddress, section.programLength));
		
		// 오브젝트 프로그램의 외부 정의 영역 설정
		if(extTab.extdef.size() > 0) {
			stringBuilder.append(extTab.printDef());
		}
		
		// 오브젝트 프로그램의 외부 참조 영역 설정
		if(extTab.extref.size() > 0) {
			stringBuilder.append(extTab.printRef());
		}
		
		// 오브젝트 프로그램의 바디 영역 설정
		StringBuilder bodyBuilder = new StringBuilder();
		int startLocation = section.startAddress; // 시작 주소
		boolean isNewLine = false; // 새로운 줄 생성 여부
		
		// 토큰 반복
		for(Token token : tokenList) {
			
			// 문자열의 길이가 초과하였거나, 새로운 라인 생성 플래그가 참일경우
			if((bodyBuilder.length() + token.byteSize > 60) || isNewLine) { 
				if(!StringUtil.isEmpty(bodyBuilder.toString())) { // 라인이 비어있지 않은 경우
					// 라인 출력
					stringBuilder.append(String.format("T%06X%02X%s\n", startLocation, bodyBuilder.length() / 2, bodyBuilder.toString()));
					
					// 정보 초기화
					isNewLine = false; 
					bodyBuilder.setLength(0); 
				}
			}
			
			if(!StringUtil.isEmpty(token.objectCode)) { // 오브젝트 코드가 있는 경우
				if(StringUtil.isEmpty(bodyBuilder.toString())) { // 문자열이 비어 있는 경우
					startLocation = token.location; // 시작 주소를 해당 코드로 설정
				}
				
				bodyBuilder.append(token.objectCode); // 해당 코드 추가
			} else if(token.operator.equals("RESB") || token.operator.equals("RESW")) { // 다음 명령을 만날 경우
				if(!StringUtil.isEmpty(bodyBuilder.toString())) { // 무조건 신규 라인으로 변경
					isNewLine = true;
				}
			} else if(token.operator.equals("LTORG") || token.operator.equals("END")) { // 리터럴 출력이 필요한 경우
				if(literalTab.literalList.size() > 0) { // 리터럴이 존재 할 경우에만
					if(StringUtil.isEmpty(bodyBuilder.toString())) {
						startLocation = literalTab.locationList.get(0); // 리터럴의 주소를 시작 주소로 설정
					}
					
					bodyBuilder.append(literalTab.print()); // 리터럴 출력
				}
			}
		}
		
		if(!StringUtil.isEmpty(bodyBuilder.toString())) { // 최종 라인 출력
			stringBuilder.append(String.format("T%06X%02X%s\n", startLocation, bodyBuilder.length() / 2, bodyBuilder.toString()));
		}
		
		// 오브젝트 프로그램의 수정 라인 출력
		if(modifyTab.modifyList.size() > 0) {
			modifyTab.modifyList.forEach(x-> stringBuilder.append(x.print()));
		}
		
		
		// 종료
		stringBuilder.append("E");
		if(section.isMain) {
			stringBuilder.append(String.format("%06X", section.startAddress));	
		}
		stringBuilder.append("\n\n");
		
		return stringBuilder.toString();
	}
	
	/** 
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
	@Override
	public String toString(){
	    return tokenList.toString();
	}
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		// 파싱 과정에서 사용하는 변수 초기화
		String[] parsingData = line.split("\t");
		
		// label 설정
		if(!StringUtil.isEmpty(parsingData[0])) {  // 존재할 경우
			this.label = parsingData[0]; // label 
		}
		
		// 명령어 설정
		if(!StringUtil.isEmpty(parsingData[1])) {
			this.operator = parsingData[1];
		} else {
			throw new SyntexException("Operator is required."); // 명령어가 없으면 오류
		}
		
		// operand 설정
		if(parsingData.length > 2 
				&& !StringUtil.isEmpty(parsingData[2])) { // 존재할 경우
			String[] operandData = parsingData[2].split(",");
			
			this.operand = new String[operandData.length]; // 오퍼랜드의 길이만큼 할당
			this.operand = operandData;
		}
		
		// 코멘트 설정
		if(parsingData.length > 3) {
			this.comment = parsingData[3];
		}
	}
	
	/**
	 * 실제 명령어가 정상적으로 입력되었는지 검토한다.
	 * 
	 * @param instruction
	 */
	public void validation(Instruction instruction) {
		if(instruction == null) {
			throw new SyntexException("This instruction does not exist."); // 명령어가 존재하지 않을 때
		}
		
		if(this.operand != null 
				&& this.operand.length < instruction.minOperandCount) { // 매개변수 숫자 미달 시
			throw new SyntexException("The minimum number of parameters is "+instruction.minOperandCount+".");
		}
		
		if(instruction.isNewSection()) { // 새로운 섹션이 필요한 경우
			Assembler.numberOfSection++;
		}
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. 
	 * 
	 * 사용 예 : setFlag(nFlag, 1); 
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		if(this.getFlag(flag) == flag) { // 플래그가 설정 되어 있을 경우
			if(value == 0) {
				this.nixbpe -= flag;
			}
		} else { // 플래그가 설정 되어 있지 않을 경우
			if(value == 1) {
				this.nixbpe += flag;
			}
		}
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 
	 * 
	 * 사용 예 : getFlag(nFlag)
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
	
	/**
	 * 개발의 편의성을 위해 임시로 오버라이딩
	 */
	@Override
	public String toString(){
	    return 	 "{ "
	    		+ "location : " + this.location + ","
	    		+ "label : " + this.label + ", "
	    		+ "operator : " + this.operator + ", "
	    		+ "operand : " + Arrays.toString(this.operand) + ","
				+ "nixbpe : " + (int)this.nixbpe
	    		+ " }";
	}
}

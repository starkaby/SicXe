import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import exception.InitException;


/**
 * Assembler : 
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. 
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) 
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) 
 * 
 * 
 * 작성중의 유의사항 : 
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 *     
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class Assembler {
	
	// 해당 클래스에서 사용하는 전역 변수 초기화
	private static final String INPUT_FILE_PREFIX = "src/";
	private static final String OUTPUT_FILE_PREFIX = "src/output/";
	
	public static int numberOfSection = 0;
	
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간*/
	ArrayList<LiteralTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 프로그램의 section별로 정의와 참조를 저장하는 공간*/
	ArrayList<ExtTable> extList;
	/** 프로그램의 section 정보를 저장하는 공간*/
	ArrayList<Section> sectionList;
	/** 프로그램의 section 정보를 저장하는 공간*/
	ArrayList<ModifyTable> modifyList;
	
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간.   
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		literaltabList = new ArrayList<LiteralTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
		
		// 신규 추가 테이블
		extList = new ArrayList<>();
		sectionList = new ArrayList<>();
		modifyList = new ArrayList<>();
	}

	/** 
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler(INPUT_FILE_PREFIX + "inst.data");
		assembler.loadInputFile(INPUT_FILE_PREFIX + "input.txt");	
		assembler.pass1();

		assembler.printSymbolTable(OUTPUT_FILE_PREFIX + "symtab_20180427");
		assembler.printLiteralTable(OUTPUT_FILE_PREFIX + "literaltab_20180427");
		assembler.pass2();
		assembler.printObjectCode(OUTPUT_FILE_PREFIX + "output_20180427");
	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * @param inputFile : input 파일 이름.
	 */
	private void loadInputFile(String inputFile) {
		try {
			File file = new File(inputFile);
			
			FileReader fileReader = new FileReader(file); // 파일 스트림 생성
			BufferedReader bufferedReader = new BufferedReader(fileReader); // 버퍼 생성
			
			String line = ""; // 라인 변수 생성
			
			while((line = bufferedReader.readLine()) != null) {
				if(line.trim().length() != 0 
						&& !line.trim().startsWith(".")) { // 양 옆 공백을 제거한 상태의 길이가 0일경우와 주석의 경우 추가하지 않음
					lineList.add(line);
				}
			}
			
			System.out.println("init line complete!");
			
			bufferedReader.close();
		} catch(FileNotFoundException e) {
			throw new InitException(inputFile + " not found.");
		} catch (IOException e) {
			throw new InitException("An error occurred while reading the " + inputFile + ".");
		}
	}

	/** 
	 * pass1 과정을 수행한다.
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성
	 *   2) label을 symbolTable에 정리
	 *   
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		// 패스 1 과정에서 사용하는 변수 초기화
		TokenTable tokenTable = null;
		LiteralTable literalTable = null;
		SymbolTable symbolTable = null;
		ExtTable extTable = null;
		Section section = null;
		ModifyTable modify = null;
		Token newSectionFirstToken = null;
		
		// 토큰 설정
		for(String line : lineList) {
			
			// 섹션 변경 데이터 초기화 begin --
			if(TokenList.size() < (Assembler.numberOfSection + 1)) { 
				if(tokenTable != null) { // 토큰 테이블이 존재 할 경우
					newSectionFirstToken = tokenTable.getToken(tokenTable.tokenList.size() - 1); // 마지막 토큰 조회 (CSECT)
					tokenTable.removeLastToken(); // 마지막 토큰 삭제
					
					tokenTable.setLocation(); // 토큰에 주소정보 할당
				}
				
				symbolTable = new SymbolTable(); // 신규 섹션의 심볼 테이블 생성
				literalTable = new LiteralTable(); // 신규 섹션의 리터럴 테이블 생성
				extTable = new ExtTable();  // 신규 섹션의 외부참조/정의 테이블 생성
				section = new Section(); // 섹션 정보를 관리하는 섹션 객체 생성
				modify = new ModifyTable();
				
				// 리스트에 할당
				symtabList.add(symbolTable);
				literaltabList.add(literalTable);
				extList.add(extTable);
				sectionList.add(section);
				modifyList.add(modify);
				
				tokenTable = new TokenTable(symbolTable, instTable, literalTable, extTable, section, modify); // 토큰 테이블 생성
				
				if(newSectionFirstToken != null) {
					tokenTable.setToken(newSectionFirstToken); // 신규 섹션의 첫번째를 설정
					
					newSectionFirstToken = null; // 다시 신규섹션이 들어올 때 처리될 수 있도록 초기화
				}
				
				TokenList.add(tokenTable); // 토큰 리스트에 토큰 테이블 추가
			}
			// 섹션 변경 데이터 초기화 end --
			
			tokenTable.putToken(line); // 토큰 삽입
		}
		
		// 마지막 토큰 테이블 로케이션 할당
		tokenTable.setLocation();
		
		System.err.println("pass 1 complete!");
	}
	
	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		try {
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName));
			StringBuilder stringBuilder = new StringBuilder();
			
			for(int i = 0; i < TokenList.size(); i++) { // 섹션 수 만큼 반복
				TokenTable tokenTable = TokenList.get(i); 
				
				for(int z = 0; z < tokenTable.symTab.symbolList.size(); z++) { // 각 섹션 별 심볼 수 만큼 반복
					stringBuilder.append(tokenTable.symTab.symbolList.get(z)) // 버퍼에 입력
							     .append("\t")
							     .append(String.format("%02X", tokenTable.symTab.locationList.get(z)))
								 .append("\n");
				}
				
				stringBuilder.append("\n");
			}
			
			bufferedOutputStream.write(stringBuilder.toString().getBytes()); // 출력 
			bufferedOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("print symbol table complete!");
	}

	/**
	 * 작성된 LiteralTable들을 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printLiteralTable(String fileName) {
		try {
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName));
			StringBuilder stringBuilder = new StringBuilder();
			
			for(int i = 0; i < TokenList.size(); i++) { // 섹션 수 만큼 반복
				TokenTable tokenTable = TokenList.get(i); 
				
				for(int z = 0; z < tokenTable.literalTab.literalList.size(); z++) { // 각 섹션 별 리터럴 수 만큼 반복
					stringBuilder.append(tokenTable.literalTab.literalList.get(z)) // 버퍼에 입력
							     .append("\t")
							     .append(String.format("%02X", tokenTable.literalTab.locationList.get(z)))
								 .append("\n");
				}
			}
			
			bufferedOutputStream.write(stringBuilder.toString().getBytes()); // 출력 
			bufferedOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("print literal table complete!");
	}

	/**
	 * pass2 과정을 수행한다.
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		
		// 패스 1 과정에서 사용하는 변수 초기화
		TokenTable tokenTable = null;
		SymbolTable symbolTable = null;
		ExtTable extTable = null;
		
		// pass 2 수행
		for(int sectionNumber = 0; sectionNumber < sectionList.size(); sectionNumber++) {
			// 섹션 별 변수 초기화 
			tokenTable = TokenList.get(sectionNumber);
			extTable = extList.get(sectionNumber);
			symbolTable = symtabList.get(sectionNumber);
			
			// 외부 정의 테이블을 검증한다.
			extTable.validation(symbolTable);
			
			// 오브젝트 코드를 생성한다.
			tokenTable.makeObjectCode();
			
			// 오브젝트 프로그램을 생성한다.
			String objectProgram = tokenTable.makeObjectProgram();
			
			codeList.add(objectProgram);
		}
		
		System.err.println("pass 2 complete!");
	}
	
	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		try {
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fileName));
			StringBuilder stringBuilder = new StringBuilder();
			
			codeList.stream().forEach(stringBuilder::append);
			
			bufferedOutputStream.write(stringBuilder.toString().getBytes()); // 출력 
			bufferedOutputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("print object program complete!");
	}
	
}

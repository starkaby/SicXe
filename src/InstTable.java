import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import exception.InitException;


/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/** 
	 * inst.data 파일을 불러와 저장하는 공간.
	 *  명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;
	
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 */
	public InstTable(String instFile) {
		instMap = new HashMap<String, Instruction>();
		openFile(instFile);
	}
	
	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 */
	public void openFile(String fileName) {
		try {
			File file = new File(fileName);
			
			FileReader fileReader = new FileReader(file); // 파일 스트림 생성
			BufferedReader bufferedReader = new BufferedReader(fileReader); // 버퍼 생성
			
			String line = ""; // 라인 변수 생성
			
			while((line = bufferedReader.readLine()) != null) {
				Instruction instruction = new Instruction(line);
				
				instMap.put(instruction.operator, instruction);
				
				//System.out.println("instruction : " + instruction);
			}
			
			System.out.println("init instruction complete!");
			
			bufferedReader.close();
		} catch(FileNotFoundException e) {
			throw new InitException(fileName + " not found.");
		} catch (IOException e) {
			throw new InitException("An error occurred while reading the " + fileName + ".");
		}
	}
	
	/**
	 * 테이블에 저장되어 있는 인스트럭션을 명령어로 검색
	 * 
	 * @param operator
	 * @return
	 */
	public Instruction findByOperator(String operator) {
		return instMap.get(operator);
	}
}
/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {
	
	// 명령어
	String operator;
	
	// 기계어 코드
	int opcode;
	
	// 형식
	int format;
	
	// 최소 오퍼랜드의 수
	int minOperandCount;
	
	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		String[] parsingData = line.split("\\|"); // | 단위로 나눔
		
		// 명령어
		this.operator = parsingData[0]; 
		
		// 형식
		this.format = Integer.parseInt(parsingData[1]); 
		
		if(parsingData[2].equals("NULL")) {
			parsingData[2] = "-1";
		}
		
		// 기계어 코드
		this.opcode = Integer.parseInt(parsingData[2], 16);
		
		
		// 최소 오퍼랜드의 수
		this.minOperandCount = Integer.parseInt(parsingData[3]); 
	}
	
	/**
	 * 해당 명령어가 새로운 섹션을 만드는지 확인한다.
	 * @return
	 */
	public boolean isNewSection() {
		return this.operator.equals("CSECT");
	}
	
	/**
	 * 개발의 편의성을 위해 임시로 오버라이딩
	 */
	@Override
	public String toString(){
	    return 	 "{ "
	    		+ "operator : " + this.operator + ", "
	    		+ "format : " + this.format + ", "
	    		+ "opcode : " + this.opcode + ", "
	    		+ "minOperandCount : " + this.minOperandCount
	    		+ " }";
	}
}

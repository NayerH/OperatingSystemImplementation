import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;


public class OS {
	
	String[] memory = new String[300];
	ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(10);
	int pid;
	String state;
	int pc;
	int min;
	int max;
	
	public void memoryInitialise(String fileName){
		Integer pid = Integer.parseInt(fileName.substring(1,2));
		int baseI = (pid - 1) * 100;
		min = baseI;
		max = baseI + 99;
		writeToMemory("pid", pid.toString());
		writeToMemory("state", "ready");
		writeToMemory("pc", baseI + 50+"");
		writeToMemory("min", min+"");
		writeToMemory("max", max + "");
		q.add(pid);
		try {
	      File myObj = new File("src/" + fileName);
	      Scanner myReader = new Scanner(myObj);
	      while (myReader.hasNextLine()) {
	        String line = myReader.nextLine();
	        writeToMemory("", line);
	      } 
	      myReader.close();
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
	}
	
	public void scheduler(){
		while(!q.isEmpty()){
			int quanta = 0;
			int pid = q.remove();
			System.out.println("The PID of the process chosen by scheduler: " + pid);
			Integer baseI = (pid - 1) * 100;
			this.pid = pid;
			this.min = baseI;
			this.max = baseI + 99;
			this.state = readFromMemory("state", null);
			writeToMemory("state", "running");
			String line1 = getInstruction(pid);
			String line2 = null;
			if(line1 == null) {
				this.state = "terminated";
			} else {
				quanta++;
				interpreter(line1);
				line2 = getInstruction(pid);
			}
			if(line2 == null){
				this.state = "terminated";
			} else {
				quanta++;
				interpreter(line2);
			}
			if(!this.state.equals("terminated")){
				q.add(pid);
				writeToMemory("state", "ready");
			} else {
				writeToMemory("state", this.state);
			}
			System.out.println("The PID of the process that ended: " + pid + ", using " + quanta + " quanta");
		}
	}
	
	public void interpreter(String data){
		String[] rowSplit = data.split(" ",2);
        switch(rowSplit[0]){
	        case "assign":
	        	assign(rowSplit[1]);
	        	break;
	        case "print":
	        	print(rowSplit[1]);
	        	break;
	        case "writeFile":
	        	String[] strSplit = rowSplit[1].split(" ");
	        	writeFile(strSplit[0],strSplit[1]);
	        	break;
	        case "add":
	        	add(rowSplit[1]);
	        	break;
        }
	}
	
	public String getInstruction(int pid){
		int pc = Integer.parseInt(readFromMemory("pc", null));
		writeToMemory("pc", pc + 1 +"");
		return readFromMemory(null, pc);
	}
	
	//NON SYSTEM
	public void assign(String str) {
		String[] strArr = str.split(" ");
		if(strArr[1].equals("readFile")){
			String fileName = readFromMemory(strArr[2],null);
			String placeHolder = readFile(fileName);
			writeToMemory(strArr[0], placeHolder);
		} else {
			String input = input();
			writeToMemory(strArr[0],input);
		}
	}
	
	//NON SYSTEM
	public void add(String str) {
		String[] strArr = str.split(" ");
		String strnum1 = readFromMemory(strArr[0],null);
		String strnum2 = readFromMemory(strArr[1],null);
		Integer num1 = Integer.parseInt(strnum1);
		Integer num2 = Integer.parseInt(strnum2);
		Integer res = num1 + num2;
		writeToMemory(strArr[0],res.toString());
	}
	
	//SYSTEM
	public String input(){
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		String res = sc.nextLine();
		return res;
	}
	
	//SYSTEM
	public void print(String s){
		String res = readFromMemory(s, null);
		if(res == null){
			res = s;
		}
		System.out.println(res);
	}
	
	//SYSTEM
	public String readFile(String str){
		String res = "";
		try {
	      File myObj = new File("src/" + str +".txt");
	      Scanner myReader = new Scanner(myObj);
	      while (myReader.hasNextLine()) {
	        res += myReader.nextLine() + '\n';
	      }
	      myReader.close();
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
		return res;
	}

	
	//SYSTEM
	public void writeFile(String fN, String dat){
		String fileName = readFromMemory(fN, null);
		String data = readFromMemory(dat, null);
		try {
	      FileWriter myWriter = new FileWriter("src/" + fileName + ".txt");
	      myWriter.write(data);
	      myWriter.close();
	    } catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
	}
	
	public String readFromMemory(String s, Integer i){
		if(i != null){
			if(i >= min+50 && i <= max){
				System.out.println("MEMORY[" + i + "] is being accessed to read from it: " + memory[i]);
				return memory[i];
			} else {
				return null;
			}
		}
		for(int index = min; index <= max - 50; index++){
			if(memory[index] != null){
				if((memory[index]).split(",")[0].equals(s)){
					System.out.println("MEMORY[" + index + "] is being accessed to read from it: " + memory[index]);
					return (memory[index]).split(",")[1];
				}
			} else{
				break;
			}
		}
		return null;
	}
	
	public void writeToMemory(String var, String value){
		if(var.equals("")){
			if(value != null){
				for(int i = min + 50; i <= max; i++){
					if(memory[i] == (null)){
						System.out.println("MEMORY[" + i + "] is being accessed to write to it: " + value);
						memory[i] = value;
						break;
					}
				}
			}
		} else {
			for(int i = min; i <= max - 50; i++){
				if(memory[i] == (null)){
					memory[i] = var + "," + value;
					System.out.println("MEMORY[" + i + "] is being accessed to write to it: " + memory[i]);
					break;
				} else if((memory[i]).split(",")[0].equals(var)){
					memory[i] = var + "," + value;
					System.out.println("MEMORY[" + i + "] is being accessed to write to it: " + memory[i]);
					break;
				}
			}
		}
	}
	
	public static void main(String[] args){
		OS os = new OS();
		os.memoryInitialise("P1.txt");
		os.memoryInitialise("P2.txt");
		os.memoryInitialise("P3.txt");
		os.scheduler();
	}
}

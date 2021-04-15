package telkom.eai.env.tools;

public class MiscUtils {
	
	public static void printEx(Throwable e) {
		for(StackTraceElement ste : e.getStackTrace()) {
			System.out.println(ste.toString());
		}
	}
	
	public static String stackTraceToStr(Throwable e) {		
		String s="";
		for(StackTraceElement ste : e.getStackTrace()) {
			s+=ste.toString()+"\n";
		}		
		return s;
	}
	
	public static String implode(String [] csv, String delim) {
		String s="";
		for(int i=0;i<csv.length;++i) {
			if(i==0) {
				s+=csv[i];
			}
			else{
				s+=delim+csv[i];
			}
		}		
		return s;
	}
	
	
}

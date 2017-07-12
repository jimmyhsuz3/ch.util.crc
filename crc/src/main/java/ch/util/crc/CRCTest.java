package ch.util.crc;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import ch.util.crc.CRCUtil;
public class CRCTest {
	private final List<String> filePathList = new ArrayList<String>();
	{
		filePathList.add("D:\\vm\\MSEdge - Win10_preview.ova");
		filePathList.add("C:\\Users\\jimmy.shu\\Desktop\\vm\\MSEdge.Win10.RS2.VirtualBox\\MSEdge - Win10_preview.ova");
		filePathList.add("C:\\Users\\jimmy.shu\\Desktop\\test_vm_share\\CRCUtil.java");
		filePathList.add("C:\\Users\\jimmy.shu\\Desktop\\test_vm_share\\CRCUtil_.java");
		filePathList.add("C:\\Users\\jimmy.shu\\Desktop\\vm\\IE11.Win8.1.For.Windows.VirtualBox\\IE11 - Win8.1.ova");
		filePathList.add("C:\\Users\\jimmy.shu\\Desktop\\test_vm_share\\TortoiseSVN-1.9.5.27581-x64-svn-1.9.5.msi");
	}
	public List<String[]> test(){
		List<String[]> crcList = new ArrayList<String[]>();
		crcList.add(testStream(filePathList.get(0)));
		crcList.add(testFile(filePathList.get(0)));
		crcList.add(testFile(filePathList.get(1)));
		crcList.add(testStream(filePathList.get(1)));
		crcList.add(testFile(filePathList.get(2)));
		crcList.add(testFile(filePathList.get(3)));
		crcList.add(testFile(filePathList.get(4)));
		crcList.add(testFile(filePathList.get(5)));
		return crcList;
	}
	private String[] testStream(String filePath){
		try {
			CRCUtil.CRCValue crcValue = CRCUtil.crcStream(new File(filePath));
			return new String[]{filePath, Long.toString(crcValue.getValue()), Long.toString(crcValue.getTime())};
		} catch(RuntimeException re){
			return new String[]{filePath, re.getLocalizedMessage(), "-"};
		}
	}
	private String[] testFile(String filePath){
		try {
			CRCUtil.CRCValue crcValue = CRCUtil.crcFile(new File(filePath));
			return new String[]{filePath, Long.toString(crcValue.getValue()), Long.toString(crcValue.getTime())};
		} catch(RuntimeException re){
			return new String[]{filePath, re.getLocalizedMessage(), "-"};
		}
	}
}
package me.asu.test.testcase;

import static org.junit.Assert.*;

import com.google.common.collect.Multimap;
import java.util.LinkedHashMap;
import me.asu.test.util.ExcelUtils;
import org.junit.Test;

/**
 * Created by suk on 2018/7/15.
 *
 * @author suk
 * @version 1.0.0
 * @since 2018/7/15
 */
public class TestDirGeneratorTest {

	@Test
	public void generate() throws Exception {
		String file = "D:\\03_projects\\suk\\auto-test\\src\\main\\resources\\excels-repo.tmpl.xlsx";
		Multimap<String, LinkedHashMap<String, String>> sheets = ExcelUtils
				.readExcel(file, true);
		TestDirGenerator generator = new TestDirGenerator("d:\\tmp\\test", sheets);
		generator.generate();
	}

}
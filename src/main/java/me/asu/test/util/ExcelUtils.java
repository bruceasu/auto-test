package me.asu.test.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * ExcelUtils.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-11-29 14:00
 */
public class ExcelUtils {

	private static final String EXT_XLS = "xls";
	private static final String EXT_XLSX = "xlsx";


	/**
	 * 张三   25岁     男   175cm
	 * 李四   22岁     女   160cm
	 * 返回结果最外层的list对应一个excel文件，第二层的list对应一个sheet页，第三层的list对应sheet页中的一行
	 */
	public static Multimap<String, LinkedHashMap<String, String>> readExcel(String filepath, boolean withHeaderRow) throws Exception {
		/*
		 * workbook Map<String, List>
		 * sheet List<LinkedHashMap>
		 * row LinkedHashMap<String, Object>
		*/
		Multimap<String, LinkedHashMap<String, String>> sheets = ArrayListMultimap.create();

		String fileType = filepath.substring(filepath.lastIndexOf(".") + 1, filepath.length());
		try (InputStream is = new FileInputStream(filepath);
			Workbook wb = openWB(fileType, is);
		){
			int sheetSize = wb.getNumberOfSheets();
			/* 遍历sheet页 */
			for (int i = 0; i < sheetSize; i++) {
				Sheet sheet = wb.getSheetAt(i);
				String sheetName = sheet.getSheetName();
				int rowSize = sheet.getLastRowNum() + 1;
				List<String> headers = new ArrayList<>();
				/* 遍历行 */
				for (int j = 0; j < rowSize; j++) {
					Row row = sheet.getRow(j);
					/* 略过空行 */
					if (row == null) {
						continue;
					}
					/* 行中有多少个单元格，也就是有多少列 */
					int cellSize = row.getLastCellNum();
					/* 对应一个数据行 */
					List<String> rowList = new ArrayList<String>();
					for (int k = 0; k < cellSize; k++) {
						Cell cell = row.getCell(k);
						String value = null;
						if (cell != null) {
							cell.setCellType(CellType.STRING);
							value = cell.toString();
						}
						rowList.add(value);
					}
					if (withHeaderRow && j == 0) {
						// 标题行
						for (int k = 0; k < rowList.size(); k++) {
							headers.add(rowList.get(k).toLowerCase());
						}

					} else {
						LinkedHashMap<String, String> rowMap = new LinkedHashMap<>();
						for (int k = 0; k < rowList.size(); k++) {
							if (k < headers.size()) {
								rowMap.put(headers.get(k), rowList.get(k));
							} else {
								rowMap.put("column-" + k, rowList.get(k));
							}
						}
						sheets.put(sheetName, rowMap);
					}
				}
			}
			return sheets;
		} catch (FileNotFoundException e) {
			throw e;
		}
	}

	private static Workbook openWB(String fileType, InputStream is) throws IOException {
		if (EXT_XLS.equals(fileType)) {
			return new HSSFWorkbook(is);
		} else if (EXT_XLSX.equals(fileType)) {
			return new XSSFWorkbook(is);
		} else {
			throw new IllegalArgumentException("读取的不是excel文件");
		}
	}

	/**
	 * excel导出到输出流
	 * 谁调用谁负责关闭输出流
	 *
	 * @param os 输出流
	 * @param excelExtName excel文件的扩展名，支持xls和xlsx，不带点号
	 */
	public static void writeExcel(OutputStream os, String excelExtName,
			Map<String, List<List<String>>> data) throws IOException {
		Workbook wb = null;
		try {

			if (EXT_XLS.equals(excelExtName)) {
				wb = new HSSFWorkbook();
			} else if (EXT_XLSX.equals(excelExtName)) {
				wb = new XSSFWorkbook();
			} else {
				throw new Exception("当前文件不是excel文件");
			}
			for (String sheetName : data.keySet()) {
				Sheet sheet = wb.createSheet(sheetName);
				List<List<String>> rowList = data.get(sheetName);
				for (int i = 0; i < rowList.size(); i++) {
					List<String> cellList = rowList.get(i);
					Row row = sheet.createRow(i);
					for (int j = 0; j < cellList.size(); j++) {
						Cell cell = row.createCell(j);
						cell.setCellValue(cellList.get(j));
					}
				}
			}
			wb.write(os);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (wb != null) {
				wb.close();
			}
		}
	}

	/**
	 * excel导出到输出流
	 * 谁调用谁负责关闭输出流
	 *
	 * @param os 输出流
	 * @param excelExtName excel文件的扩展名，支持xls和xlsx，不带点号
	 * @param data excel数据，map中的key是标签页的名称，value对应的list是标签页中的数据。 list中的子list是标签页中的一行，子list中的对象是一个单元格的数据，
	 * 包括是否居中、跨几行几列以及存的值是多少
	 */
	public static void writeExcelWithMergeCell(OutputStream os, String excelExtName,
			Map<String, List<List<ExcelData>>> data) throws IOException {
		Workbook wb = null;
		CellStyle cellStyle = null;
		boolean isXls;
		try {
			if (EXT_XLS.equals(excelExtName)) {
				wb = new HSSFWorkbook();
				isXls = true;
			} else if (EXT_XLSX.equals(excelExtName)) {
				wb = new XSSFWorkbook();
				isXls = false;
			} else {
				throw new Exception("当前文件不是excel文件");
			}
			cellStyle = wb.createCellStyle();
			if (isXls) {
				cellStyle.setAlignment(HorizontalAlignment.CENTER);
				cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			} else {
				cellStyle.setAlignment(HorizontalAlignment.CENTER);
				cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			}
			for (String sheetName : data.keySet()) {
				Sheet sheet = wb.createSheet(sheetName);
				List<List<ExcelData>> rowList = data.get(sheetName);
				//i 代表第几行 从0开始
				for (int i = 0; i < rowList.size(); i++) {
					List<ExcelData> cellList = rowList.get(i);
					Row row = sheet.createRow(i);
					//j 代表第几列 从0开始
					int j = 0;
					for (ExcelData excelData : cellList) {
						if (excelData != null) {
							if (excelData.getColSpan() > 1 || excelData.getRowSpan() > 1) {
								CellRangeAddress cra = new CellRangeAddress(i,
										i + excelData.getRowSpan() - 1, j,
										j + excelData.getColSpan() - 1);
								sheet.addMergedRegion(cra);
							}
							Cell cell = row.createCell(j);
							cell.setCellValue(excelData.getValue());
							if (excelData.isAlignCenter()) {
								cell.setCellStyle(cellStyle);
							}
							j = j + excelData.getColSpan();
						} else {
							j++;
						}
					}
				}
			}
			wb.write(os);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (wb != null) {
				wb.close();
			}
		}
	}


	public static class ExcelData {

		/**
		 * 单元格的值
		 */
		private String value;
		/**
		 * 单元格跨几列
		 */
		private int colSpan = 1;
		/**
		 * 单元格跨几行
		 */
		private int rowSpan = 1;
		/**
		 * 单元格是否居中，默认不居中，如果选择是，则水平和上下都居中
		 */
		private boolean alignCenter;

		public boolean isAlignCenter() {
			return alignCenter;
		}

		public void setAlignCenter(boolean alignCenter) {
			this.alignCenter = alignCenter;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public int getColSpan() {
			return colSpan;
		}

		public void setColSpan(int colSpan) {
			this.colSpan = colSpan;
		}

		public int getRowSpan() {
			return rowSpan;
		}

		public void setRowSpan(int rowSpan) {
			this.rowSpan = rowSpan;
		}
	}

}

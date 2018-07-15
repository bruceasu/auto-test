package me.asu.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TableGenerator.
 *
 * @author suk
 * @version 1.0.0
 * @since 2017-10-13 9:52
 */

public class TableGenerator {
    private static final int PADDING_SIZE = 2;
    private static final String NEW_LINE = "\n";
    private static final String TABLE_JOINT_SYMBOL = "+";
    private static final String TABLE_V_SPLIT_SYMBOL = "|";
    private static final String TABLE_H_SPLIT_SYMBOL = "-";

    public String generateTable(List<String> headersList, List<List<String>> rowsList,
            int... overRiddenHeaderHeight) {
        StringBuilder stringBuilder = new StringBuilder();

        int rowHeight = overRiddenHeaderHeight.length > 0 ? overRiddenHeaderHeight[0] : 1;

        Map<Integer, Integer> columnMaxWidthMapping = getMaximumWidthOfTable(headersList, rowsList);
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(NEW_LINE);
        createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);
        stringBuilder.append(NEW_LINE);

        for (int headerIndex = 0; headerIndex < headersList.size(); headerIndex++) {
            fillCell(stringBuilder, headersList.get(headerIndex), headerIndex,
                    columnMaxWidthMapping);
        }

        stringBuilder.append(NEW_LINE);

        createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);

        for (List<String> row : rowsList) {

            for (int i = 0; i < rowHeight; i++) {
                stringBuilder.append(NEW_LINE);
            }

            for (int cellIndex = 0; cellIndex < row.size(); cellIndex++) {
                fillCell(stringBuilder, row.get(cellIndex), cellIndex, columnMaxWidthMapping);
            }

        }

        stringBuilder.append(NEW_LINE);
        createRowLine(stringBuilder, headersList.size(), columnMaxWidthMapping);
        stringBuilder.append(NEW_LINE);
        stringBuilder.append(NEW_LINE);

        return stringBuilder.toString();
    }

    private void fillSpace(StringBuilder stringBuilder, int length) {
        for (int i = 0; i < length; i++) {
            stringBuilder.append(" ");
        }
    }

    private void createRowLine(StringBuilder stringBuilder, int headersListSize,
            Map<Integer, Integer> columnMaxWidthMapping) {
        for (int i = 0; i < headersListSize; i++) {
            if (i == 0) {
                stringBuilder.append(TABLE_JOINT_SYMBOL);
            }

            for (int j = 0; j < columnMaxWidthMapping.get(i) + PADDING_SIZE * 2; j++) {
                stringBuilder.append(TABLE_H_SPLIT_SYMBOL);
            }
            stringBuilder.append(TABLE_JOINT_SYMBOL);
        }
    }


    public static Map<Integer, Integer> getMaximumWidthOfTable(List<String> headersList,
            List<List<String>> rowsList) {
        Map<Integer, Integer> columnMaxWidthMapping = new HashMap<>();

        for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {
            columnMaxWidthMapping.put(columnIndex, 0);
        }

        for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {
            int textLength = getTextLength(headersList.get(columnIndex));
            if (textLength > columnMaxWidthMapping.get(columnIndex)) {
                columnMaxWidthMapping.put(columnIndex, textLength);
            }
        }

        for (List<String> row : rowsList) {
            for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {
                int textLength = getTextLength(row.get(columnIndex));
                if (textLength > columnMaxWidthMapping.get(columnIndex)) {
                    columnMaxWidthMapping.put(columnIndex, textLength);
                }
            }
        }

        for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {
            if (columnMaxWidthMapping.get(columnIndex) % 2 != 0) {
                columnMaxWidthMapping.put(columnIndex, columnMaxWidthMapping.get(columnIndex) + 1);
            }
        }
        return columnMaxWidthMapping;
    }

    private int getOptimumCellPadding(int cellIndex, int datalength,
            Map<Integer, Integer> columnMaxWidthMapping, int cellPaddingSize) {
        if (datalength % 2 != 0) {
            datalength++;
        }

        if (datalength < columnMaxWidthMapping.get(cellIndex)) {
            cellPaddingSize =
                    cellPaddingSize + (columnMaxWidthMapping.get(cellIndex) - datalength) / 2;
        }

        return cellPaddingSize;
    }

    private void fillCell(StringBuilder stringBuilder, String cell, int cellIndex,
            Map<Integer, Integer> columnMaxWidthMapping) {
        if (cellIndex == 0) {
            stringBuilder.append(TABLE_V_SPLIT_SYMBOL);
        }

        boolean startWithSpace = false;
        boolean endWithSpace = false;
        if (cell != null && !cell.isEmpty()) {
            startWithSpace = Character.isSpaceChar(cell.charAt(0));
            endWithSpace = Character.isSpaceChar(cell.charAt(cell.length() - 1));
        }
        if (startWithSpace && endWithSpace) {
            // align center
            int cellPaddingSize = getOptimumCellPadding(cellIndex, getTextLength(cell), columnMaxWidthMapping,
                    PADDING_SIZE);
            fillSpace(stringBuilder, cellPaddingSize);
            stringBuilder.append(cell);
            if (getTextLength(cell) % 2 != 0) {
                stringBuilder.append(" ");
            }

            fillSpace(stringBuilder, cellPaddingSize);
        } else if (startWithSpace) {
            // align right
            int cellPaddingSize = getOptimumCellPadding(cellIndex, getTextLength(cell), columnMaxWidthMapping,
                    PADDING_SIZE);
            fillSpace(stringBuilder, cellPaddingSize * 2);
            if (getTextLength(cell) % 2 != 0) {
                stringBuilder.append(" ");
            }
            stringBuilder.append(cell);
        } else if (endWithSpace) {
            // datetime align left
            int cellPaddingSize = getOptimumCellPadding(cellIndex, getTextLength(cell), columnMaxWidthMapping,
                    PADDING_SIZE);
            stringBuilder.append(cell);
            if (getTextLength(cell) % 2 != 0) {
                stringBuilder.append(" ");
            }
            fillSpace(stringBuilder, cellPaddingSize * 2);
        } else {
            // auto detect by rules
            if (cell.matches("[0-9.Ee%]+")) {
                // number align right
                int cellPaddingSize = getOptimumCellPadding(cellIndex, getTextLength(cell), columnMaxWidthMapping,
                        PADDING_SIZE);
                fillSpace(stringBuilder, cellPaddingSize * 2);
                if (getTextLength(cell) % 2 != 0) {
                    stringBuilder.append(" ");
                }
                stringBuilder.append(cell);


            } else if (cell.matches("\\d{2,4}.\\d{1,2}.\\d{1,2}( \\d{1,2}:\\d{1,2}:\\d{1,2}(.\\d{3})?)?")) {
                // datetime align center
                int cellPaddingSize = getOptimumCellPadding(cellIndex, getTextLength(cell), columnMaxWidthMapping,
                        PADDING_SIZE);
                fillSpace(stringBuilder, cellPaddingSize);
                stringBuilder.append(cell);
                if (getTextLength(cell) % 2 != 0) {
                    stringBuilder.append(" ");
                }

                fillSpace(stringBuilder, cellPaddingSize);
            } else {
                // datetime align left
                int cellPaddingSize = getOptimumCellPadding(cellIndex, getTextLength(cell), columnMaxWidthMapping,
                        PADDING_SIZE);
                stringBuilder.append(cell);
                if (getTextLength(cell) % 2 != 0) {
                    stringBuilder.append(" ");
                }
                fillSpace(stringBuilder, cellPaddingSize * 2);
            }

        }


        stringBuilder.append(TABLE_V_SPLIT_SYMBOL);

    }

    public static String makeAlignCenter(String cell) {
        if (cell == null) {
            return "";
        }
        boolean startWithSpace = false;
        boolean endWithSpace = false;
        if (cell != null && !cell.isEmpty()) {
            startWithSpace = Character.isSpaceChar(cell.charAt(0));
            endWithSpace = Character.isSpaceChar(cell.charAt(cell.length() - 1));
        }
        if (startWithSpace && endWithSpace) {
            return cell;
        } else if (startWithSpace) {
            return cell + " ";
        } else if (endWithSpace) {
            return " " + cell;
        } else {
            return " " + cell + " ";
        }
    }

    public static String makeAlignLeft(String cell) {
        if (cell == null) {
            return "";
        }
        boolean startWithSpace = false;
        boolean endWithSpace = false;
        if (cell != null && !cell.isEmpty()) {
            startWithSpace = Character.isSpaceChar(cell.charAt(0));
            endWithSpace = Character.isSpaceChar(cell.charAt(cell.length() - 1));
        }
        String v = cell;
        if (startWithSpace) {
            /* avoid getfield opcode */
            char[] val = cell.toCharArray();
            int len = val.length;
            int st = 0;
            while ((st < len) && (val[st] <= ' ')) {
                st++;
            }
            v = ((st > 0) || (len < val.length)) ? cell.substring(st, len) : cell;
        }
        if (!endWithSpace) {
            return  v + " ";
        }
        return v;
    }

    public static String makeAlignRight(String cell) {
        if (cell == null) {
            return "";
        }
        boolean startWithSpace = false;
        boolean endWithSpace = false;
        if (cell != null && !cell.isEmpty()) {
            startWithSpace = Character.isSpaceChar(cell.charAt(0));
            endWithSpace = Character.isSpaceChar(cell.charAt(cell.length() - 1));
        }
        String v = cell;
        if (endWithSpace) {
            /* avoid getfield opcode */
            char[] val = cell.toCharArray();
            int len = val.length;
            int st = 0;
            while ((st < len) && (val[len - 1] <= ' ')) {
                len--;
            }
            v = ((st > 0) || (len < val.length)) ? cell.substring(st, len) : cell;
        }
        if (!startWithSpace) {
            return  " " + v;
        }
        return v;
    }


    public static  int getTextLength(String text) {
        int length = 0;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (CheckChinese.isChinese(chars[i])) {
                length += 2;
            } else {
                length++;
            }
        }
        return length;
    }

    public static void main(String[] args) {
        List<String> headersList = Arrays.asList("col1", "column2", "列3");
        List<List<String>> rowsList = new ArrayList<>();
        rowsList.add(Arrays.asList("english", "简体中文", "繁体"));
        rowsList.add(Arrays.asList("english", " 简体中文 ", "繁体 "));
        rowsList.add(Arrays.asList(makeAlignRight("english"), makeAlignCenter("简体中文"), makeAlignLeft(" 12343554")));
        rowsList.add(Arrays.asList("2010-10-10 12:21:22", "简体中文", "繁体"));
        rowsList.add(Arrays.asList("2000-10-10", "简体中文", "12343554"));
        TableGenerator tg = new TableGenerator();
        String table = tg.generateTable(headersList, rowsList);
        System.out.println(table);
    }
}

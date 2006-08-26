/*
 * Created on May 11, 2005
 *
 */
package org.apache.poi.hssf.record.formula.eval;

import java.io.FileInputStream;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.functions.TestMathX;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public class GenericFormulaTestCase extends TestCase {

    protected final String FILENAME = System.getProperty("HSSF.testdata.path")+ "/FormulaEvalTestData.xls";

    protected HSSFWorkbook workbook = null;

    protected CellReference beginCell;
    protected int getBeginRow() {
        return beginCell.getRow();
    }
    
    protected short getBeginCol() {
        return beginCell.getCol();
    }

    protected static final HSSFCell getExpectedValueCell(HSSFSheet sheet, HSSFRow row, HSSFCell cell) {
        HSSFCell retval = null;
        if (sheet != null) {
            row = sheet.getRow(row.getRowNum()+1);
            if (row != null) {
                retval = row.getCell(cell.getCellNum());
            }
        }
        
        return retval;
    }

    protected void assertEquals(String msg, HSSFCell expected, HSSFFormulaEvaluator.CellValue actual) {
        if (expected != null && actual!=null) {
            if (expected!=null && expected.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                String value = expected.getStringCellValue();
                if (value.startsWith("#")) {
                    expected.setCellType(HSSFCell.CELL_TYPE_ERROR);
                }
            }
            if (!(expected == null || actual == null)) {
                switch (expected.getCellType()) {
                case HSSFCell.CELL_TYPE_BLANK:
                    assertEquals(msg, HSSFCell.CELL_TYPE_BLANK, actual.getCellType());
                    break;
                case HSSFCell.CELL_TYPE_BOOLEAN:
                    assertEquals(msg, HSSFCell.CELL_TYPE_BOOLEAN, actual.getCellType());
                    assertEquals(msg, expected.getBooleanCellValue(), actual.getBooleanValue());
                    break;
                case HSSFCell.CELL_TYPE_ERROR:
                    assertEquals(msg, HSSFCell.CELL_TYPE_ERROR, actual.getCellType()); // TODO: check if exact error matches
                    break;
                case HSSFCell.CELL_TYPE_FORMULA: // will never be used, since we will call method after formula evaluation
                    throw new AssertionFailedError("Cannot expect formula as result of formula evaluation: " + msg);
                case HSSFCell.CELL_TYPE_NUMERIC:
                    assertEquals(msg, HSSFCell.CELL_TYPE_NUMERIC, actual.getCellType());
                    TestMathX.assertEquals(msg, expected.getNumericCellValue(), actual.getNumberValue(), TestMathX.POS_ZERO, TestMathX.DIFF_TOLERANCE_FACTOR);
//                    double delta = Math.abs(expected.getNumericCellValue()-actual.getNumberValue());
//                    double pctExpected = Math.abs(0.00001*expected.getNumericCellValue());
//                    assertTrue(msg, delta <= pctExpected);
                    break;
                case HSSFCell.CELL_TYPE_STRING:
                    assertEquals(msg, HSSFCell.CELL_TYPE_STRING, actual.getCellType());
                    assertEquals(msg, expected.getStringCellValue(), actual.getStringValue());
                    break;
                }
            }
            else {
                throw new AssertionFailedError("expected: " + expected + " got:" + actual);
            }
        }
    }

    public GenericFormulaTestCase(String beginCell) {
        super("genericTest");      
        this.beginCell = new CellReference(beginCell);
    }
    
    public void setUp() throws Exception {
        FileInputStream fin = new FileInputStream( FILENAME );
        workbook = new HSSFWorkbook( fin );
        fin.close();    	
    }
    
    public void genericTest() throws Exception {
        HSSFSheet s = workbook.getSheetAt( 0 );
        HSSFRow r = s.getRow(getBeginRow());
        short endcolnum = r.getLastCellNum();
        HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(s, workbook);
        evaluator.setCurrentRow(r);

        HSSFCell c = null;
        for (short colnum=getBeginCol(); colnum < endcolnum; colnum++) {
            try {
            c = r.getCell(colnum);
            if (c==null || c.getCellType() != HSSFCell.CELL_TYPE_FORMULA)
                continue;
            
            HSSFFormulaEvaluator.CellValue actualValue = evaluator.evaluate(c);
            
            HSSFCell expectedValueCell = getExpectedValueCell(s, r, c);
            assertEquals("Formula: " + c.getCellFormula() 
                    + " @ " + getBeginRow() + ":" + colnum, 
                    expectedValueCell, actualValue);
            } catch (RuntimeException re) {
                throw new RuntimeException("CELL["+getBeginRow()+","+colnum+"]: "+re.getMessage(), re);
            }
        }
    }
    
}

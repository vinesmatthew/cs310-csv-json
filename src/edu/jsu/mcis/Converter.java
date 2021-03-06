package edu.jsu.mcis;

import java.io.*;
import java.util.*;
import com.opencsv.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import java.sql.*;

public class Converter {
    
    /*
    
        Consider the following CSV info:
        
        "ID","Total","Assignment 1","Assignment 2","Exam 1"
        "111278","611","146","128","337"
        "111352","867","227","228","412"
        "111373","461","96","90","275"
        "111305","835","220","217","398"
        "111399","898","226","229","443"
        "111160","454","77","125","252"
        "111276","579","130","111","338"
        "111241","973","236","237","500"
        
        The corresponding JSON info would be similar to the following (tabs and
        other whitespace have been added for clarity).  Note the curly braces,
        square brackets, and double-quotes!  These indicate which values should
        be encoded as strings, and which values should be encoded as integers!
        
        {
            "col":["ID","Total","Assignment 1","Assignment 2","Exam 1"],
            "row":["111278","111352","111373","111305","111399","111160",
            "111276","111241"],
            "info":[[611,146,128,337],
                    [867,227,228,412],
                    [461,96,90,275],
                    [835,220,217,398],
                    [898,226,229,443],
                    [454,77,125,252],
                    [579,130,111,338],
                    [973,236,237,500]
            ]
        }
    
        Your task for this program is to complete the two conversion methods in
        this class, "csvToJson()" and "jsonToCsv()", so that the CSV info shown
        above can be converted to JSON format, and vice-versa.  Both methods
        should return the converted info as strings, but the strings do not need
        to include the newlines and whitespace shown in the examples; again,
        this whitespace has been added only for clarity.
    
        NOTE: YOU SHOULD NOT WRITE ANY CODE WHICH MANUALLY COMPOSES THE OUTPUT
        STRINGS!!!  Leave ALL string conversion to the two info conversion
        libraries we have discussed, OpenCSV and json-simple.  See the "Data
        Exchange" lecture notes for more details, including example code.
    
    */
    
    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) throws ParseException {
        
        String results = "";
        
        try {
            
            CSVReader reader = new CSVReader(new StringReader(csvString));
            List<String[]> full = reader.readAll();
            Iterator<String[]> iterator = full.iterator();
            String[] colHeaders = iterator.next();
            
            JSONArray modifyRecord = new JSONArray();
            LinkedHashMap<String, JSONArray> jsonData = new LinkedHashMap<>();
            
            String[] record;
            
            String[] rowHeaders = new String[full.size() - 1];
            int[][] data = new int[full.size() - 1][colHeaders.length - 1];
            int counter = 0;
            int counter2 = 0;

            while (iterator.hasNext()) {
                record = iterator.next();
                for (int i = 0; i < colHeaders.length; i++) {
                    if (i == 0) {
                        rowHeaders[counter] = record[i];
                        counter++;
                    } else {
                        data[counter2][i - 1] = Integer.parseInt(record[i]);
                    }
                }
                counter2++;
            }
            
            counter = 0;
            for (String i : rowHeaders) {
                modifyRecord.add(counter, i);
                counter++;
            }
            
            jsonData.put("rowHeaders", modifyRecord);
            
            modifyRecord = new JSONArray();
            JSONArray modifyRecord2 = new JSONArray();
            
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    modifyRecord2.add(j, data[i][j]);
                }
                modifyRecord.add(i, modifyRecord2);
                modifyRecord2 = new JSONArray();
            }
            
            jsonData.put("data", modifyRecord);
            
            modifyRecord = new JSONArray();
            counter = 0;
            
            for (String i : colHeaders) {
                modifyRecord.add(counter, i);
                counter++;
            }
            
            jsonData.put("colHeaders", modifyRecord);
            
            results = JSONValue.toJSONString(jsonData);
        } catch(IOException e) { return e.toString(); }
        
        return results.trim();
        
    }
    
    public static String jsonToCsv(String jsonString) {
                
        String results = "";
        
        try {
            
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer, ',', '"', '\n');
                        
            String[] sections = jsonString.split("],\"");
            String[] rowHeaders = sections[0].split(":\\[")[1].replaceAll("\"", "").split(",");
                        
            String[] colHeaders = sections[2].split(":")[1].replaceAll("\"", "").replace("[", "").replace("]", "").replace("}", "").split(",");
                        
            String[] almostData = sections[1].split(":\\[")[1].split("],\\[");
            String[][] data = new String[almostData.length][colHeaders.length];
                        
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < colHeaders.length; j++) {
                    if (j == 0) {
                        data[i][j] = rowHeaders[i];
                    } else {
                        data[i][j] = almostData[i].split(",")[j - 1].replace("[", "").replace("]", "");
                    }
                }
            }
                        
            csvWriter.writeNext(colHeaders);
            for (String[] i : data) {
                csvWriter.writeNext(i);
            }
                        
            results = writer.toString();
            
        }
        
        catch(Exception e) { return e.toString(); }
        
        return results.trim();
        
    }

    public static JSONArray getJSONData() throws ClassNotFoundException, InstantiationException {
        JSONArray results = null;
        
        Connection conn = null;
        PreparedStatement pstSelect = null, pstUpdate = null;
        ResultSet resultset = null;
        ResultSetMetaData metadata = null;
        
        String query, value;
        String[] headers;
        
        JSONArray records = new JSONArray();
        
        boolean hasresults;
        int resultCount, columnCount;
        
        try {
            
            /* Identify the Server */
            
            String server = ("jdbc:mysql://localhost/p2_test");
            String username = "root";
            String password = "CS310";
            
            /* Load the MySQL JDBC Driver */
            
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            /* Open Connection */

            conn = DriverManager.getConnection(server, username, password);
            /* Test Connection */
            
            if (conn.isValid(0)) {
                
                /* Prepare Select Query */
                
                query = "SELECT * FROM people";
                pstSelect = conn.prepareStatement(query);
                
                /* Execute Select Query */
                hasresults = pstSelect.execute();                
                
                /* Get Results */
                                
                while ( hasresults || pstSelect.getUpdateCount() != -1 ) {

                    if ( hasresults ) {
                        
                        /* Get ResultSet Metadata */
                        resultset = pstSelect.getResultSet();
                        metadata = resultset.getMetaData();
                        columnCount = metadata.getColumnCount();
                        
                        headers = new String[columnCount - 1];
                        
                        for (int i = 0; i < headers.length; i++) {
                            headers[i] = metadata.getColumnLabel(i + 2);
                        }
                        
                        /* Get Data; Print as Table Rows */
                        
                        LinkedHashMap data = new LinkedHashMap();
                        
                        while(resultset.next()) {
                            
                            /* Begin Next ResultSet Row */
                            
                            /* Loop Through ResultSet Columns; Print Values */
                            
                            data = new LinkedHashMap();
                            
                            for (int i = 0; i < headers.length; i++) {
                               
                                value = resultset.getString(i + 2);

                                if (resultset.wasNull()) {
                                    data.put(headers[i], "");
                                }

                                else {
                                    data.put(headers[i], value);
                                }

                            }
                            
                            records.add(data);
                        }
                        
                    }

                    else {

                        resultCount = pstSelect.getUpdateCount();  

                        if ( resultCount == -1 ) {
                            break;
                        }

                    }
                    
                    /* Check for More Data */

                    hasresults = pstSelect.getMoreResults();

                }
                
                results = records;
            }
            
            System.out.println();
            
            /* Close Database Connection */
            
            conn.close();
            
        }
        
        catch (Exception e) {
            System.err.println(e.toString());
        }
        
        /* Close Other Database Objects */
        
        finally {
            
            if (resultset != null) { try { resultset.close(); resultset = null; } catch (Exception e) {} }
            
            if (pstSelect != null) { try { pstSelect.close(); pstSelect = null; } catch (Exception e) {} }
            
            if (pstUpdate != null) { try { pstUpdate.close(); pstUpdate = null; } catch (Exception e) {} }
            
        }
        
        return results;
    }
}
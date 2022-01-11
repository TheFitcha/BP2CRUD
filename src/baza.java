import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Locale;
import java.util.Vector;

public class baza {
    private static Connection conn;
    private static Statement stmt;
    private static Vector<Vector<String>> foreignKeysColumns;
    public static void baza(String args[]){
    }

    public static void createConnection(){
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "bp2_user", "bp2_user");
            if(conn == null){
                throw new Exception("Neuspjela konekcija na bazu!");
            }
            stmt = conn.createStatement();
            stmt.executeQuery("alter session set container = bp2_pdb");
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    public static DefaultTableModel getData(String tableName){
        DefaultTableModel model = null;
        try{
            createConnection();
            ResultSet rs = stmt.executeQuery("select * from " + tableName);
            ResultSetMetaData metaData = rs.getMetaData();

            Vector<String> naziviAtributa = new Vector<String>();
            for(int i = 1; i <= metaData.getColumnCount(); i++){
                naziviAtributa.add(metaData.getColumnName(i));
            }

            Vector<Vector<Object>> podaci = new Vector<>();
            foreignKeysColumns = getAllForeignKeys(tableName);
            int foreignIndex;
            while(rs.next()){
                Vector<Object> pomocni = new Vector<>();
                for (int j = 1; j <= metaData.getColumnCount(); j++){
                    foreignIndex = checkForeignKey(metaData.getColumnName(j));
                    if(foreignIndex != -1){
                        pomocni.add(rs.getString(j) + " (" + getColumnValues(foreignIndex, rs.getString(j))
                                + ")");
                    }
                    else {
                        pomocni.add(rs.getObject(j));
                    }
                }
                podaci.add(pomocni);
            }
            model = new DefaultTableModel(podaci, naziviAtributa);
        }
        catch (Exception e){
            System.out.println("getData: " + e);
        }
        finally{
            closeConnection();
        }
        return model;
    }

    public static void closeConnection(){
        try {
            conn.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void executeQuery(String query) throws Exception{
        try{
            createConnection();
            int result = stmt.executeUpdate(query);
            System.out.println(query);
            System.out.println("Modified " + result + " rows.");
        }
        catch (Exception e){
            System.out.println("Execute query: " + e);
            throw e;
        }
        finally{
            closeConnection();
        }
    }

    public static Vector<Vector<String>> getAllForeignKeys(String tableName){
        Vector<Vector<String>> foreignKeys = new Vector<>();
        try{
            createConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rsFk = metaData.getImportedKeys(conn.getCatalog(), null, tableName.toUpperCase(Locale.ROOT));
            while(rsFk.next()){
                Vector<String> newFk = new Vector<>();
                newFk.add(rsFk.getString("PKTABLE_NAME"));
                newFk.add(rsFk.getString("PKCOLUMN_NAME"));
                newFk.add(rsFk.getString("FKCOLUMN_NAME"));
                foreignKeys.add(newFk);
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
        finally {
            closeConnection();
        }
        System.out.println(foreignKeys);
        return foreignKeys;
    }

    public static int checkForeignKey(String columnName){
        int counter = -1;
        for(Vector<String> keyPair : foreignKeysColumns){
            counter++;
            if(keyPair.contains(columnName)){
                return counter;
            }
        }
        return -1;
    }

    public static String getColumnValues(int index, String columnValue){
        String columnNameResult = "";
        String query;
        try{
            createConnection();
            query = "select * from " + foreignKeysColumns.get(index).get(0) + " where " + foreignKeysColumns.get(index).get(1)
                    + " = " + columnValue;
            System.out.println(query);
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                columnNameResult = rs.getString(2);
            }
        }
        catch (Exception e){
            System.out.println("getColumnNames: " + e);
        }
        finally {
            closeConnection();
        }
        return columnNameResult;
    }

    public static String getForeignKeyValue(String tableName, String column, int row){
        String resultString = "";
        try{
            createConnection();
            ResultSet rs = stmt.executeQuery("select " + column + " from " + tableName);
            for(int i = 0; i<=row; i++){
                rs.next();
                resultString = rs.getString(1);
            }
        }
        catch (Exception e){
            System.out.println("getForeignKeysValue: " + e);
        }
        finally {
            closeConnection();
        }
        return resultString;
    }
}

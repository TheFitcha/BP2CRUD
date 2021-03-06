import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class main extends baza{
    private static JTable dataTable;
    private static baza konekcija;
    private static JComboBox comboBox;
    private static JFrame mainFrame;
    private static JPanel mainPanel;
    private static JPanel tablePanel;
    private static JPanel userInputPanel;
    private static JPanel comboboxPanel;
    private static JPanel userInteractionPanel;
    private static JPanel userButtonsPanel;
    private static JPanel manualSQLPanel;
    private static Font mainFont;
    private static JTextField[] textFields;
    private static JTextField manualSQLText;
    private static JButton manualSQLButton;
    private static JButton applyButton;
    private static JButton deleteButton;
    private static JButton clearButton;
    public static void main(String[] args){
        mainFrame = new JFrame();
        mainPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        tablePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        comboboxPanel = new JPanel();
        userInputPanel = new JPanel(new GridLayout(0, 2, 10, 20));
        userInteractionPanel = new JPanel(new GridBagLayout());
        userButtonsPanel = new JPanel(new GridLayout(0, 3, 10, 0));
        manualSQLPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        konekcija = new baza();
        mainFont = new Font("Consolas", Font.BOLD, 14);
        applyButton = new JButton("Unesi");
        clearButton = new JButton("Ocisti");
        deleteButton = new JButton("Obrisi");
        manualSQLText = new JTextField("SELECT 'alive' FROM dual", 90);
        manualSQLButton = new JButton("Izvrsi");

        mainFrame.setTitle("BP2GUI");
        mainFrame.setPreferredSize(new Dimension(850, 530));
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        mainFrame.setContentPane(mainPanel);
        mainPanel.add(tablePanel);
        mainPanel.add(userInteractionPanel);
        mainPanel.add(manualSQLPanel);

        userInteractionPanel.setPreferredSize(new Dimension(300, 400));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.CENTER;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.ipady = 20;
        constraints.ipadx = 15;
        constraints.insets = new Insets(0, 20, 0,0);
        userInteractionPanel.add(comboboxPanel, constraints);
        constraints.gridy = 1;
        userInteractionPanel.add(userInputPanel, constraints);
        constraints.gridy = 2;
        constraints.ipady = 0;
        constraints.ipadx = 0;
        constraints.insets = new Insets(10, 20, 0, 0);
        userInteractionPanel.add(userButtonsPanel, constraints);

        userButtonsPanel.add(applyButton);
        userButtonsPanel.add(deleteButton);
        userButtonsPanel.add(clearButton);

        manualSQLText.setFont(mainFont);
        manualSQLText.setPreferredSize(new Dimension(manualSQLText.getWidth(), 25 ));
        manualSQLPanel.add(manualSQLText);
        manualSQLPanel.add(manualSQLButton);

        manualSQLButton.addActionListener(e -> {
            sendQueryManual();
        });

        applyButton.addActionListener(e -> {
            if(checkUpdate())
                updateData();
            else
                insertData();
        });

        deleteButton.addActionListener(e -> deleteData());
        clearButton.addActionListener(e -> clearFields());

        dataTable = new JTable(){
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };
        dataTable.getTableHeader().setReorderingAllowed(false);

        dataTable.getSelectionModel().addListSelectionListener(e -> {
            if(!dataTable.getSelectionModel().isSelectionEmpty()){
                fillFieldsWhenSelected(dataTable.getSelectedRow());
            }
            else {
                clearFields();
            }
        });

        tablePanel.add(dataTable);
        tablePanel.add(new JScrollPane(dataTable));

        comboBox = new JComboBox();
        populateComboBox();
        comboBox.addActionListener(e -> {
            if(comboBox.getSelectedItem() != ""){
                readData(String.valueOf(comboBox.getSelectedItem()));
                applyButton.setEnabled(true);
                deleteButton.setEnabled(true);
                clearButton.setEnabled(true);
            }
            else{
                applyButton.setEnabled(false);
                deleteButton.setEnabled(false);
                clearButton.setEnabled(false);
            }
        });

        comboboxPanel.add(comboBox);

        readData(String.valueOf(comboBox.getSelectedItem()));
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static boolean checkUpdate(){    //check all primary keys if they have the same value
        if(dataTable.getRowCount() == 0)
            return false;
        if(!checkIfExists(textFields[0].getText(), 0))
            return false;
        for(int i = 0; i<dataTable.getColumnCount(); i++)
            if(konekcija.checkForeignKey(dataTable.getColumnName(i)) != -1)
                if(!checkIfExists(textFields[i].getText(), i)){
                    return false;
                }
        return true;
    }

    public static boolean checkIfExists(String id, int column){     //check values from textboxes
        for(int i = 0; i<dataTable.getRowCount(); i++){
            if(dataTable.getValueAt(i, column).toString().contains(id)){
                return true;
            }
        }
        return false;
    }

    public static void populateComboBox(){      //self-explainatory
        String[] naziviTablica = {"privilegija", "privilegija_korisnik", "korisnik", "instalirao", 
            "aplikacija", "dokument", "kreirao_dokument", "vrsta_dokumenta", "direktorij", "particija"};
        comboBox.addItem("");
        for (String naziv : naziviTablica){
            comboBox.addItem(naziv);
        }
        comboBox.setSelectedIndex(1);
    }

    public static void readData(String name){       //get data from database
        DefaultTableModel dataModel = konekcija.getData(name);
        if(dataModel == null) {
            JOptionPane.showMessageDialog(mainFrame, "Pogre??ka prilikom spajanja na bazu!", "Pogre??ka",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        dataTable.setModel(dataModel);
        createFields();
    }

    public static void createFields(){      //dynamically create fields for user interaction
        userInputPanel.removeAll();
        int columnCount = dataTable.getColumnCount();
        textFields = new JTextField[columnCount];
        for(int i = 0; i<columnCount; i++){
            userInputPanel.add(new JLabel(dataTable.getColumnName(i)));
            textFields[i] = new JTextField();
            textFields[i].setFont(mainFont);
            userInputPanel.add(textFields[i]);
        }
        userInputPanel.revalidate();
        userInputPanel.repaint();
    }

    public static void fillFieldsWhenSelected(int selectedRow){     //fill textboxes with data from the table
        for(int i = 0; i<textFields.length; i++){
            if(konekcija.checkForeignKey(dataTable.getColumnName(i)) != -1){
                textFields[i].setText(konekcija.getForeignKeyValue(comboBox.getSelectedItem().toString(),
                        dataTable.getColumnName(i), selectedRow));
            }
            else{
                textFields[i].setText(dataTable.getValueAt(selectedRow, i).toString());
            }
        }
    }

    public static void clearFields(){
        for(int i = 0; i<textFields.length; i++){
            textFields[i].setText("");
        }
        dataTable.clearSelection();
    }

    public static void insertData(){
        String query = "INSERT INTO " + comboBox.getSelectedItem() + " VALUES (";
        for(int i = 0; i<textFields.length; i++){
            if(i+1 == textFields.length)
                query += "'" + textFields[i].getText() + "')";
            else
                query += "'" + textFields[i].getText() + "', ";
        }
        sendQuery(query);
    }

    public static void updateData(){
        String query = "UPDATE " + comboBox.getSelectedItem() + " SET ";
        for(int i = 0; i<textFields.length; i++){
            if(i+1 == textFields.length)
                query += dataTable.getColumnName(i) + " = '" + textFields[i].getText() + "' WHERE ";
            else
                query += dataTable.getColumnName(i) + " = '" + textFields[i].getText() + "', ";
        }
        for(int j = 0; j<dataTable.getColumnCount(); j++){
            if(konekcija.checkPrimaryKey(dataTable.getColumnName(j)) != -1){
                query += dataTable.getColumnName(j) + " = " + textFields[j].getText() + " AND ";
            }
        }
        query = query.substring(0, query.length()-5);
        System.out.println(query);
        //sendQuery(query);
    }

    public static void deleteData(){
        String query = "DELETE FROM " + comboBox.getSelectedItem() + " WHERE ";
        if(konekcija.checkKeys()){
            query += dataTable.getColumnName(0) + " = " + textFields[0].getText();
        }
        else {
            for(int j = 0; j<dataTable.getColumnCount(); j++){
                if(konekcija.checkPrimaryKey(dataTable.getColumnName(j)) != -1){
                    query += dataTable.getColumnName(j) + " = " + textFields[j].getText() + " AND ";
                }
            }
            query = query.substring(0, query.length()-5);
        }
        sendQuery(query);
    }

    public static void sendQuery(String query){
        try{
            System.out.println(query);
            konekcija.executeQuery(query);
        }
        catch (Exception e){
            String error = String.valueOf(e);
            JOptionPane.showMessageDialog(mainFrame, "Pogre??ka prilikom izvo??enja operacije!\n" + error,
                    "Pogre??ka", JOptionPane.ERROR_MESSAGE);
        }
        readData(String.valueOf(comboBox.getSelectedItem()));
    }

    public static void sendQueryManual(){
        String query = manualSQLText.getText();
        try{
            DefaultTableModel model = konekcija.getDataManual(query);
            dataTable.setModel(model);
            createFields();
            comboBox.setSelectedIndex(0);
        }
        catch (Exception e){
            String error = String.valueOf(e);
            JOptionPane.showMessageDialog(mainFrame, "Pogre??ka prilikom izvo??enja operacije!\n" + error,
                    "Pogre??ka", JOptionPane.ERROR_MESSAGE);
        }
    }
}
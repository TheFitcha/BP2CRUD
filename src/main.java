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
    private static Font mainFont;
    private static JTextField[] textFields;
    private static JButton applyButton;
    private static JButton deleteButton;
    private static JButton clearButton;
    public static void main(String[] args){
        mainFrame = new JFrame();
        mainPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        tablePanel = new JPanel();
        comboboxPanel = new JPanel();
        userInputPanel = new JPanel(new GridLayout(0, 2, 10, 20));
        userInteractionPanel = new JPanel(new GridBagLayout());
        userButtonsPanel = new JPanel(new GridLayout(0, 3, 10, 0));
        konekcija = new baza();
        mainFont = new Font("Consolas", Font.BOLD, 14);
        applyButton = new JButton("Unesi");
        clearButton = new JButton("Ocisti");
        deleteButton = new JButton("Obrisi");

        mainFrame.setTitle("BP2GUI");
        mainFrame.setPreferredSize(new Dimension(850, 500));
        mainFrame.setResizable(false);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);

        mainFrame.setContentPane(mainPanel);
        mainPanel.add(tablePanel);
        mainPanel.add(userInteractionPanel);
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

        applyButton.addActionListener(e -> {
            if(checkIfExists(textFields[0].getText()))
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
        comboBox.addActionListener(e -> readData(String.valueOf(comboBox.getSelectedItem())));

        comboboxPanel.add(comboBox);

        readData(String.valueOf(comboBox.getSelectedItem()));
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public static boolean checkIfExists(String id){
        for(int i = 0; i<dataTable.getRowCount(); i++){
            if(id.equalsIgnoreCase(dataTable.getValueAt(i, 0).toString())){
                return true;
            }
        }
        return false;
    }

    public static void populateComboBox(){
        String[] naziviTablica = {"privilegija", "privilegija_korisnik", "korisnik", "instalirao", 
            "aplikacija", "dokument", "kreirao_dokument", "vrsta_dokumenta", "direktorij", "particija"};
        for (String naziv : naziviTablica){
            comboBox.addItem(naziv);
        }
    }

    public static void readData(String name){
        DefaultTableModel dataModel = konekcija.getData(name);
        if(dataModel == null) {
            JOptionPane.showMessageDialog(mainFrame, "Pogreška prilikom spajanja na bazu!", "Pogreška",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        dataTable.setModel(dataModel);
        createFields();
    }

    public static void createFields(){
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

    public static void fillFieldsWhenSelected(int selectedRow){
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
        try{
            konekcija.executeQuery(query);
        }
        catch (Exception e){
            String error = String.valueOf(e);
            JOptionPane.showMessageDialog(mainFrame, "Pogreška prilikom izvođenja operacije!\n" + error,
                    "Pogreška", JOptionPane.ERROR_MESSAGE);
        }
        readData(String.valueOf(comboBox.getSelectedItem()));
    }

    public static void updateData(){
        String query = "UPDATE " + comboBox.getSelectedItem() + " SET ";
        for(int i = 0; i<textFields.length; i++){
            if(i+1 == textFields.length)
                query += dataTable.getColumnName(i) + " = '" + textFields[i].getText() + "' WHERE " +
                        dataTable.getColumnName(0) + " = " + textFields[0].getText();
            else
                query += dataTable.getColumnName(i) + " = '" + textFields[i].getText() + "', ";
        }
        try{
            konekcija.executeQuery(query);
        }
        catch (Exception e){
            String error = String.valueOf(e);
            JOptionPane.showMessageDialog(mainFrame, "Pogreška prilikom izvođenja operacije!\n" + error,
                    "Pogreška", JOptionPane.ERROR_MESSAGE);
        }
        readData(String.valueOf(comboBox.getSelectedItem()));
    }

    public static void deleteData(){
        String query = "DELETE FROM " + comboBox.getSelectedItem() + " WHERE " + dataTable.getColumnName(0) + "="
                + dataTable.getValueAt(dataTable.getSelectedRow(), 0).toString();
        try{
            konekcija.executeQuery(query);
        }
        catch (Exception e){
            String error = String.valueOf(e);
            JOptionPane.showMessageDialog(mainFrame, "Pogreška prilikom izvođenja operacije!\n" + error,
                    "Pogreška", JOptionPane.ERROR_MESSAGE);
        }
        readData(String.valueOf(comboBox.getSelectedItem()));
    }
}
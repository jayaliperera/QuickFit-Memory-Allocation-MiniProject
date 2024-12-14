import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;


public class QuickFitMemoryAllocation {

    // Fixed categories of block sizes for Quick Fit
    private final int[] categories = {50, 100, 200, 300, 500}; 
    private final HashMap<Integer, LinkedList<Integer>> freeLists; 
    private JTable memoryTable;
    private QuickFitTableModel tableModel;

    public QuickFitMemoryAllocation() {
        // Initialize free lists for each category
        freeLists = new HashMap<>();
        for (int size : categories) {
            LinkedList<Integer> freeList = new LinkedList<>();
            for (int i = 0; i < 5; i++) { // Add 5 blocks of each size initially
                freeList.add(size);
            }
            freeLists.put(size, freeList);
        }
    }

    /**
     * Creates the GUI and sets up the actions.
     */
    public void createAndShowGUI() {
        // Create the frame
        JFrame frame = new JFrame("Quick Fit Memory Allocation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null); // Center the window

        // Set custom background color for the window
        frame.getContentPane().setBackground(new Color(245, 245, 245));

        // Create the table model and table
        tableModel = new QuickFitTableModel(freeLists);
        memoryTable = new JTable(tableModel);
        memoryTable.setFont(new Font("Arial", Font.PLAIN, 16)); 
        memoryTable.setRowHeight(30);

        // Center-align text in table
        memoryTable.setDefaultRenderer(Object.class, (table, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(value.toString(), JLabel.CENTER);
            label.setOpaque(true);
            if (row % 2 == 0) {
                label.setBackground(new Color(240, 240, 240)); 
            } else {
                label.setBackground(Color.WHITE);
            }
            if (column == 2) { // Set background color for "Status" column
                String status = (String) table.getValueAt(row, column);
                if ("Free".equals(status)) {
                    label.setBackground(new Color(144, 238, 144)); 
                } else {
                    label.setBackground(new Color(255, 99, 71)); 
                }
            }
            return label;
        });

        // Create GUI elements with custom styling
        JLabel processLabel = new JLabel("Process Size (KB):");
        processLabel.setFont(new Font("Arial", Font.BOLD, 16));
        processLabel.setForeground(new Color(50, 50, 50)); 

        JTextField processField = new JTextField(10);
        processField.setFont(new Font("Arial", Font.PLAIN, 16));
        processField.setBackground(Color.WHITE);

        JButton allocateButton = createStyledButton("Allocate Memory");
        JButton deallocateButton = createStyledButton("Deallocate Memory");
        JButton resetButton = createStyledButton("Reset Memory");

        // Layout setup
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(new Color(245, 245, 245));
        inputPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        inputPanel.add(processLabel);
        inputPanel.add(processField);
        inputPanel.add(allocateButton);
        inputPanel.add(deallocateButton);
        inputPanel.add(resetButton);

        JScrollPane tableScrollPane = new JScrollPane(memoryTable);

        // Add panels to the frame
        frame.setLayout(new BorderLayout(20, 20));
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(tableScrollPane, BorderLayout.CENTER);

        // Action listeners
        allocateButton.addActionListener(e -> {
            try {
                int processSize = Integer.parseInt(processField.getText());
                allocateMemory(processSize);
                tableModel.fireTableDataChanged();
                processField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid process size.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        deallocateButton.addActionListener(e -> {
            String sizeInput = JOptionPane.showInputDialog(frame, "Enter Block Size to Deallocate:");
            if (sizeInput != null) {
                try {
                    int blockSize = Integer.parseInt(sizeInput);
                    deallocateMemory(blockSize);
                    tableModel.fireTableDataChanged();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter a valid block size.",
                            "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        resetButton.addActionListener(e -> {
            resetMemory();
            tableModel.fireTableDataChanged();
        });

        // Show the frame
        frame.setVisible(true);
    }

    /**
     * Allocates memory for a process using the Quick Fit algorithm.
     *
     * @param processSize The size of the process to allocate (in KB).
     */
    private void allocateMemory(int processSize) {
        for (int size : categories) {
            if (processSize <= size && !freeLists.get(size).isEmpty()) {
                freeLists.get(size).removeFirst();
                JOptionPane.showMessageDialog(null, "Process of size " + processSize + " KB allocated in block size " + size + " KB.",
                        "Allocation Successful", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "No suitable block found for process size " + processSize + " KB.",
                "Allocation Failed", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Deallocates memory from a specific category.
     *
     * @param blockSize The size of the block to deallocate.
     */
    private void deallocateMemory(int blockSize) {
        if (!freeLists.containsKey(blockSize)) {
            JOptionPane.showMessageDialog(null, "Invalid block size.",
                    "Deallocation Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        freeLists.get(blockSize).add(blockSize);
        JOptionPane.showMessageDialog(null, "Block of size " + blockSize + " KB deallocated.",
                "Deallocation Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Resets all free lists to their initial state.
     */
    private void resetMemory() {
        // Clear the current free lists
        freeLists.clear();

        // Restore the free lists to their initial state (5 blocks for each size)
        for (int size : categories) {
            LinkedList<Integer> freeList = new LinkedList<>();
            for (int i = 0; i < 5; i++) { // Add 5 blocks of each size initially
                freeList.add(size);
            }
            freeLists.put(size, freeList);
        }

        // Notify the user that memory has been reset
        JOptionPane.showMessageDialog(null, "Memory has been reset.",
                "Reset Successful", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Custom Table Model for displaying memory free lists.
     */
    static class QuickFitTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Block Size (KB)", "Free Blocks", "Status"};
        private final HashMap<Integer, LinkedList<Integer>> freeLists;

        QuickFitTableModel(HashMap<Integer, LinkedList<Integer>> freeLists) {
            this.freeLists = freeLists;
        }

        @Override
        public int getRowCount() {
            return freeLists.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            int size = (int) freeLists.keySet().toArray()[rowIndex];
            switch (columnIndex) {
                case 0:
                    return size;
                case 1:
                    return freeLists.get(size).size(); // Number of free blocks in this category
                case 2:
                    return freeLists.get(size).isEmpty() ? "Allocated" : "Free"; // Block status
            }
            return null;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
    }

    /**
     * Method to create styled buttons.
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(70, 130, 180)); 
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(160, 40));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237)); 
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180)); 
            }
        });

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QuickFitMemoryAllocation simulator = new QuickFitMemoryAllocation();
            simulator.createAndShowGUI();
        });
    }
}

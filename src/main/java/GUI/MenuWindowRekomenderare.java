package GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import Recept.FileReceptRepository;
import Recept.ReceptFörslag;
import org.javatuples.Triplet;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MenuWindowRekomenderare extends JDialog {
    private FileReceptRepository receptRepository;
    private ReceptFörslag receptFörslag;

    private JComboBox<String> targetDayComboBox;
    private String targetDay;

    private JPanel checkboxPanel;
    private Map<String, JCheckBox> ingredientCheckboxes = new HashMap<>();
    private JButton recommendButton;
    private JSpinner maxMissingSpinner;

    private JTable resultsTable;
    private DefaultTableModel resultsModel;
    private JTextArea previewArea;
    private JButton addButton;

    public MenuWindowRekomenderare(FileReceptRepository receptRepository, JComboBox<String> targetDayComboBox,
                                 JSpinner targetPortionSpinner, String targetDay) {
        this.receptRepository = receptRepository;
        this.receptFörslag = new ReceptFörslag();
        this.targetDayComboBox = targetDayComboBox;

        this.targetDay = targetDay;

        setTitle("Rekommendera recept för " + targetDay);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModal(true);

        initComponents();
        loadIngredients();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(8, 8));
        main.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Left: ingredient chooser with checkboxes
        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        JScrollPane ingredientScroll = new JScrollPane(checkboxPanel);
        ingredientScroll.setPreferredSize(new Dimension(220, 0));
        ingredientScroll.getVerticalScrollBar().setUnitIncrement(16); // faster scrolling

        JPanel leftPanel = new JPanel(new BorderLayout(4, 4));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Dina ingredienser"));
        leftPanel.add(ingredientScroll, BorderLayout.CENTER);
        

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recommendButton = new JButton("Rekommendera recept");
        maxMissingSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 20, 1));
        leftControls.add(new JLabel("Max saknade:"));
        leftControls.add(maxMissingSpinner);
        leftControls.add(recommendButton);
        leftPanel.add(leftControls, BorderLayout.SOUTH);

        main.add(leftPanel, BorderLayout.WEST);

        // Center: results table
        resultsModel = new DefaultTableModel(new Object[] {"Recept", "#Matchade", "#Saknade", "Matchade", "Saknade"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(resultsModel);
        // hide the two set columns
        resultsTable.getColumnModel().getColumn(3).setMinWidth(0);
        resultsTable.getColumnModel().getColumn(3).setMaxWidth(0);
        resultsTable.getColumnModel().getColumn(4).setMinWidth(0);
        resultsTable.getColumnModel().getColumn(4).setMaxWidth(0);

        JScrollPane resultsScroll = new JScrollPane(resultsTable);
        resultsScroll.setBorder(BorderFactory.createTitledBorder("Rekommendationer"));
        main.add(resultsScroll, BorderLayout.CENTER);

        // Right: preview and actions
        JPanel right = new JPanel(new BorderLayout(4, 4));
        previewArea = new JTextArea(10, 30);
        previewArea.setEditable(false);
        right.add(new JScrollPane(previewArea), BorderLayout.CENTER);

        addButton = new JButton("Lägg till i " + targetDay);
        addButton.setEnabled(false);
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rightButtons.add(addButton);
        right.add(rightButtons, BorderLayout.SOUTH);

        right.setBorder(BorderFactory.createTitledBorder("Förhandsgranskning"));
        main.add(right, BorderLayout.EAST);

        add(main);

        // Listeners
        recommendButton.addActionListener(e -> runRecommendation());

        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelectedPreview();
        });

        addButton.addActionListener(e -> addSelectedToDay());
    }

    private void loadIngredients() {
        new SwingWorker<Set<String>, Void>() {
            @Override
            protected Set<String> doInBackground() {
                return receptFörslag.läsInIngredienser(receptRepository);
            }

            @Override
            protected void done() {
                try {
                    Set<String> s = get();
                    List<String> sorted = new ArrayList<>(s);
                    Collections.sort(sorted);
                    checkboxPanel.removeAll();
                    ingredientCheckboxes.clear();
                    for (String i : sorted) {
                        JCheckBox cb = new JCheckBox(i);
                        cb.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyPressed(KeyEvent e) {
                                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                                    cb.setSelected(!cb.isSelected());
                                    e.consume();
                                }
                            }
                        });
                        ingredientCheckboxes.put(i, cb);
                        checkboxPanel.add(cb);
                    }
                    checkboxPanel.revalidate();
                    checkboxPanel.repaint();
                } catch (Exception ex) {
                    System.err.println("Kunde inte läsa ingredienser: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void runRecommendation() {
        Set<String> selected = new HashSet<>();
        for (String ing : ingredientCheckboxes.keySet()) {
            if (ingredientCheckboxes.get(ing).isSelected()) {
                selected.add(ing);
            }
        }

        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Välj minst en ingrediens för rekommendation.");
            return;
        }

        int maxMissing = (int) maxMissingSpinner.getValue();

        recommendButton.setEnabled(false);
        resultsModel.setRowCount(0);

        new SwingWorker<List<Triplet<String, Set<String>, Set<String>>>, Void>() {
            @Override
            protected List<Triplet<String, Set<String>, Set<String>>> doInBackground() {
                return receptFörslag.matchandeRecept(selected, receptRepository);
            }

            @Override
            protected void done() {
                recommendButton.setEnabled(true);
                try {
                    List<Triplet<String, Set<String>, Set<String>>> res = get();
                    for (Triplet<String, Set<String>, Set<String>> t : res) {
                        Set<String> missing = t.getValue2();
                        if (missing.size() <= maxMissing) {
                            resultsModel.addRow(new Object[] {t.getValue0(), t.getValue1().size(), missing.size(), t.getValue1(), missing});
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Fel vid rekommendation: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @SuppressWarnings("unchecked")
    private void showSelectedPreview() {
        int row = resultsTable.getSelectedRow();
        if (row < 0) {
            previewArea.setText("");
            addButton.setEnabled(false);
            return;
        }
        String name = (String) resultsModel.getValueAt(row, 0);
        Set<String> matched = (Set<String>) resultsModel.getValueAt(row, 3);
        Set<String> missing = (Set<String>) resultsModel.getValueAt(row, 4);

        StringBuilder sb = new StringBuilder();
        sb.append("Recept: ").append(name).append("\n\n");
        sb.append("Matchade ingredienser (" + matched.size() + "):\n");
        for (String m : matched) sb.append(" - ").append(m).append("\n");
        sb.append("\nSaknade ingredienser (" + missing.size() + "):\n");
        for (String m : missing) sb.append(" - ").append(m).append("\n");

        previewArea.setText(sb.toString());
        addButton.setEnabled(true);
    }

    private void addSelectedToDay() {
        int row = resultsTable.getSelectedRow();
        if (row < 0) return;
        String recipeName = (String) resultsModel.getValueAt(row, 0);

        targetDayComboBox.setSelectedItem(recipeName);

        JOptionPane.showMessageDialog(this, "Recept '" + recipeName + "' tillagd för " + targetDay + ".");
        dispose();
    }
}
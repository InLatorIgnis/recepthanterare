package GUI;
import javax.swing.*;

import Butikslayout.ButiksLayout;
import Butikslayout.FileButiksLayoutRepository;
import ENUMS.Kategori;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class LayoutWindow extends JFrame {
    private FileButiksLayoutRepository layoutRepository;

    private DefaultComboBoxModel<String> layoutComboModel;
    private JComboBox<String> layoutComboBox;
    private JTextField layoutNameField;

    private DefaultComboBoxModel<Kategori> categoryComboModel;
    private JComboBox<Kategori> categoryCombo;
    private DefaultListModel<Kategori> orderModel;
    private JList<Kategori> orderList;

    private JButton addLastButton;
    private JButton insertButton;
    private JButton removeButton;
    private JButton upButton;
    private JButton downButton;
    private JButton newButton;
    private JButton saveButton;
    private JButton deleteButton;
    private JButton closeButton;

    public LayoutWindow(FileButiksLayoutRepository layoutRepository) {
        this.layoutRepository = layoutRepository;
        setTitle("Redigera butikslayout");
        setSize(760, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(new JLabel("Välj butikslayout:"), BorderLayout.WEST);

        layoutComboModel = new DefaultComboBoxModel<>();
        layoutComboBox = new JComboBox<>(layoutComboModel);
        loadLayoutNames();
        topPanel.add(layoutComboBox, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Uppdatera");
        refreshButton.addActionListener(e -> loadLayoutNames());
        topPanel.add(refreshButton, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Butiksnamn:"));
        layoutNameField = new JTextField(24);
        namePanel.add(layoutNameField);
        centerPanel.add(namePanel, BorderLayout.NORTH);

        orderModel = new DefaultListModel<>();
        orderList = new JList<>(orderModel);
        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderList.setVisibleRowCount(10);

        JScrollPane listScrollPane = new JScrollPane(orderList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Kategoriordning i butiken"));
        centerPanel.add(listScrollPane, BorderLayout.CENTER);

        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Lägg till / flytta kategorier"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        editPanel.add(new JLabel("Kategori:"), gbc);

        categoryComboModel = new DefaultComboBoxModel<>(Kategori.values());
        categoryCombo = new JComboBox<>(categoryComboModel);
        categoryCombo.setPreferredSize(new Dimension(220, 26));
        gbc.gridx = 1;
        editPanel.add(categoryCombo, gbc);

        addLastButton = new JButton("Lägg till sist");
        gbc.gridx = 0;
        gbc.gridy = 1;
        editPanel.add(addLastButton, gbc);

        insertButton = new JButton("Infoga här");
        gbc.gridx = 1;
        editPanel.add(insertButton, gbc);

        removeButton = new JButton("Ta bort vald");
        gbc.gridx = 0;
        gbc.gridy = 2;
        editPanel.add(removeButton, gbc);

        upButton = new JButton("Flytta upp");
        gbc.gridx = 1;
        editPanel.add(upButton, gbc);

        downButton = new JButton("Flytta ner");
        gbc.gridx = 0;
        gbc.gridy = 3;
        editPanel.add(downButton, gbc);

        JButton clearButton = new JButton("Rensa lista");
        gbc.gridx = 1;
        editPanel.add(clearButton, gbc);

        centerPanel.add(editPanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        newButton = new JButton("Ny layout");
        saveButton = new JButton("Spara layout");
        deleteButton = new JButton("Ta bort layout");
        closeButton = new JButton("Stäng");

        bottomPanel.add(newButton);
        bottomPanel.add(saveButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(closeButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        layoutComboBox.addActionListener(e -> loadSelectedLayout());
        addLastButton.addActionListener(e -> addCategory(false));
        insertButton.addActionListener(e -> addCategory(true));
        removeButton.addActionListener(e -> removeSelectedCategory());
        upButton.addActionListener(e -> moveSelectedCategory(-1));
        downButton.addActionListener(e -> moveSelectedCategory(1));
        clearButton.addActionListener(e -> orderModel.clear());
        newButton.addActionListener(e -> clearEditor());
        saveButton.addActionListener(e -> saveLayout());
        deleteButton.addActionListener(e -> deleteLayout());
        closeButton.addActionListener(e -> dispose());
    }

    private void loadLayoutNames() {
        layoutComboModel.removeAllElements();
        try {
            Files.list(FileButiksLayoutRepository.DEFAULT_LAYOUTS_DIR)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> {
                        String name = p.getFileName().toString().replace(".json", "");
                        layoutComboModel.addElement(name);
                    });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Kunde inte läsa butikslayouter: " + e.getMessage(), "Fel", JOptionPane.ERROR_MESSAGE);
        }
        layoutComboBox.setSelectedIndex(-1);
    }

    private void loadSelectedLayout() {
        String selectedName = (String) layoutComboBox.getSelectedItem();
        if (selectedName == null || selectedName.isBlank()) {
            return;
        }

        try {
            ButiksLayout layout = layoutRepository.ladda(selectedName);
            loadLayout(layout);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Kunde inte ladda layout: " + e.getMessage(), "Fel", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLayout(ButiksLayout layout) {
        layoutNameField.setText(layout.getButiksNamn());
        orderModel.clear();
        if (layout.getKategoriOrdning() != null) {
            for (Kategori kategori : layout.getKategoriOrdning()) {
                orderModel.addElement(kategori);
            }
        }
    }

    private void clearEditor() {
        layoutNameField.setText("");
        orderModel.clear();
        layoutComboBox.setSelectedIndex(-1);
    }

    private void addCategory(boolean insertAtSelection) {
        Kategori kategori = (Kategori) categoryCombo.getSelectedItem();
        if (kategori == null) {
            return;
        }
        if (orderModel.contains(kategori)) {
            JOptionPane.showMessageDialog(this, "Kategorin finns redan i listan.", "Duplicerad kategori", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedIndex = orderList.getSelectedIndex();
        if (insertAtSelection && selectedIndex >= 0) {
            orderModel.add(selectedIndex, kategori);
            orderList.setSelectedIndex(selectedIndex);
        } else {
            orderModel.addElement(kategori);
            orderList.setSelectedIndex(orderModel.getSize() - 1);
        }
    }

    private void removeSelectedCategory() {
        int selectedIndex = orderList.getSelectedIndex();
        if (selectedIndex >= 0) {
            orderModel.remove(selectedIndex);
        }
    }

    private void moveSelectedCategory(int direction) {
        int selectedIndex = orderList.getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }

        int targetIndex = selectedIndex + direction;
        if (targetIndex < 0 || targetIndex >= orderModel.getSize()) {
            return;
        }

        Kategori selected = orderModel.getElementAt(selectedIndex);
        orderModel.remove(selectedIndex);
        orderModel.add(targetIndex, selected);
        orderList.setSelectedIndex(targetIndex);
    }

    private void saveLayout() {
        String name = layoutNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ange ett namn för butikslayouten.", "Fel", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (orderModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lägg till minst en kategori i layouten.", "Fel", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Kategori> kategoriOrdning = new ArrayList<>();
        for (int i = 0; i < orderModel.getSize(); i++) {
            kategoriOrdning.add(orderModel.getElementAt(i));
        }

        ButiksLayout layout = new ButiksLayout(name, kategoriOrdning);
        try {
            layoutRepository.spara(layout);
            JOptionPane.showMessageDialog(this, "Butikslayout sparad.", "Sparad", JOptionPane.INFORMATION_MESSAGE);
            loadLayoutNames();
            layoutComboBox.setSelectedItem(name);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Kunde inte spara layout: " + e.getMessage(), "Fel", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteLayout() {
        String name = layoutNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Välj eller skriv in en layout att ta bort.", "Fel", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Är du säker på att du vill ta bort layouten '" + name + "'?",
                "Bekräfta borttagning",
                JOptionPane.YES_NO_OPTION);

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            layoutRepository.ta_bort(name);
            JOptionPane.showMessageDialog(this, "Layouten har tagits bort.", "Borttagen", JOptionPane.INFORMATION_MESSAGE);
            loadLayoutNames();
            clearEditor();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Kunde inte ta bort layouten: " + e.getMessage(), "Fel", JOptionPane.ERROR_MESSAGE);
        }
    }
}
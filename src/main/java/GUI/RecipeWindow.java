package GUI;
import javax.swing.*;

import Recept.FileReceptRepository;
import Recept.ReceptFabrik;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RecipeWindow extends JFrame {
    private FileReceptRepository receptRepository;
    private List<ReceptFabrik.ReceptIngrediens> nuvarandeIngredienser = new ArrayList<>();

    // Komponenter i gränssnittet
    private JComboBox<String> recipeComboBox;
    private DefaultComboBoxModel<String> recipeComboBoxModel;
    private JTextField nameField;
    private JTextArea ingredientsArea;
    private JButton NewRecipeButton;
    private JButton saveButton;
    private JButton deleteButton;
    private JButton backButton;

    private JTextField ingNameField, ingAmountField;
    private JComboBox<String> ingUnitCombo;
    private JComboBox<String> ingCategoryCombo;
    private JButton addIngrediensButton;

    public RecipeWindow(FileReceptRepository receptRepository) {
        this.receptRepository = receptRepository;

        setTitle("Recepthanterare");
        setSize(800, 600);
        setLocationRelativeTo(null);
        // Stänger ENDAST detta fönster, inte hela programmet
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 

        // Huvudpanel med BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOPPARREAN: Välja befintliga recept (Motsvarar CLI val 2) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Välj recept:"));
        
        // Hämta receptnamn från ditt repository för att fylla rullgardinen
        recipeComboBoxModel = new DefaultComboBoxModel<>();
        try {
            Files.list(FileReceptRepository.DEFAULT_RECIPES_DIR)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(p -> {
                    String recipeName = p.getFileName().toString().replace(".json", "");
                    recipeComboBoxModel.addElement(recipeName);
                });
        } catch (IOException e) {
            System.err.println("Kunde inte läsa recept: " + e.getMessage());
        }
        recipeComboBox = new JComboBox<>(recipeComboBoxModel);
        topPanel.add(recipeComboBox);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- MITTAREAN: Formulär för att skapa/visa (Motsvarar CLI val 1 & 2) ---
        JPanel centerPanel = new JPanel(new BorderLayout(4, 4));
        
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Receptnamn:"));
        nameField = new JTextField(20);
        namePanel.add(nameField);
        centerPanel.add(namePanel, BorderLayout.NORTH);

         // Visningsyta för ingredienser
        ingredientsArea = new JTextArea(12, 40);
        ingredientsArea.setEditable(false); // Gör den skrivskyddad så rp.output() inte förstörs
        centerPanel.add(new JScrollPane(ingredientsArea), BorderLayout.CENTER);

        // FORMULÄR FÖR ATT LÄGGA TILL EN INGREDIENS (Motsvarar CLI-frågorna)
        JPanel addIngPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ingNameField = new JTextField(8);
        ingAmountField = new JTextField(4);
        // Skapa kombo för kategorier med giltiga värden
        String[] enheter = ReceptFabrik.ReceptIngrediens.getValidEnheter().split(", ");
        ingUnitCombo = new JComboBox<>(enheter);
        // Skapa kombo för kategorier med giltiga värden
        String[] kategorier = ReceptFabrik.ReceptIngrediens.getValidKategorier().split(", ");
        ingCategoryCombo = new JComboBox<>(kategorier);
        addIngrediensButton = new JButton("Lägg till ingrediens");

        addIngPanel.add(new JLabel("Ingrediensnamn:"));
        addIngPanel.add(ingNameField);
        addIngPanel.add(new JLabel("Mängd:"));
        addIngPanel.add(ingAmountField);
        addIngPanel.add(new JLabel("Enhet:"));
        addIngPanel.add(ingUnitCombo);
        addIngPanel.add(new JLabel("Kategori:"));
        addIngPanel.add(ingCategoryCombo);
        addIngPanel.add(addIngrediensButton);
        centerPanel.add(addIngPanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // --- BOTTENAREAN: Spara och ta bort knapp ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Spara helt recept");
        deleteButton = new JButton("Ta bort recept");
        backButton = new JButton("Stäng");
        NewRecipeButton = new JButton("Nytt recept");
        bottomPanel.add(NewRecipeButton);
        bottomPanel.add(saveButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(backButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setupActions(saveButton, deleteButton, backButton, NewRecipeButton);
        setContentPane(mainPanel);
    }

    private void setupActions(JButton saveButton, JButton deleteButton, JButton backButton, JButton newRecipeButton) {
        // LÄSA IN (Hämta befintligt)
        recipeComboBox.addActionListener(e -> {
            String valtRecept = (String) recipeComboBox.getSelectedItem();
            if (valtRecept == null || valtRecept.isEmpty()) return;
            
            nameField.setText(valtRecept);
            nameField.setEditable(false); // Lås namnet vid visning
            nuvarandeIngredienser.clear();
            try {
                ReceptFabrik rp = receptRepository.ladda(valtRecept);
                ingredientsArea.setText(rp.output());
                nuvarandeIngredienser.addAll(rp.getIngrediensList());
            } catch (Exception ex) {
                ingredientsArea.setText("Kunde inte ladda recept.");
            }
        });

        // NYTT RECEPT (Rensa formuläret för att skapa ett nytt recept)
        newRecipeButton.addActionListener(e -> {
            nameField.setText("");
            nameField.setEditable(true); // Gör namnfältet redigerbart för nytt recept
            ingredientsArea.setText("");
            nuvarandeIngredienser.clear();
            recipeComboBox.setSelectedIndex(-1); // Avmarkera eventuellt valt recept

        });

        // SKAPA: Lägg till en enskild ingrediens i listan
        addIngrediensButton.addActionListener(e -> {
            try {
                String namn = ingNameField.getText().trim();
                if (namn.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Ingrediensnamn kan inte vara tomt.");
                    return;
                }
                
                double mangd = Double.parseDouble(ingAmountField.getText().trim());
                String enhet = (String) ingUnitCombo.getSelectedItem();
                String kategori = (String) ingCategoryCombo.getSelectedItem();

                ReceptFabrik.ReceptIngrediens nyIng = new ReceptFabrik.ReceptIngrediens(namn, kategori, mangd, enhet);
                nuvarandeIngredienser.add(nyIng);

                // Uppdatera textarean så användaren ser vad som lagts till
                ingredientsArea.append("- " + mangd + " " + enhet + " " + namn + " (" + kategori + ")\n");

                // Rensa inmatningsfälten för nästa ingrediens
                ingNameField.setText(""); 
                ingAmountField.setText(""); 
                ingUnitCombo.setSelectedIndex(0);
                ingCategoryCombo.setSelectedIndex(0);
                ingNameField.requestFocus();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Mängd måste vara ett tal (t.ex. 500.0)");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Ogiltig kategori", JOptionPane.ERROR_MESSAGE);
            }
        });

        // SKRIVA TILL FIL (Spara hela receptet)
        saveButton.addActionListener(e -> {
            String receptNamn = nameField.getText().trim();
            if (receptNamn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ange ett receptnamn.");
                return;
            }else if (nuvarandeIngredienser.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lägg till minst en ingrediens innan du sparar.");
                return;
            }

            ReceptFabrik nyttRecept = new ReceptFabrik(receptNamn, nuvarandeIngredienser);

            try {
                if (receptRepository.exists(receptNamn)) {
                    int option = JOptionPane.showConfirmDialog(this, 
                        "Ett recept med namnet '" + receptNamn + "' finns redan. Vill du ersätta det?", 
                        "Bekräfta", 
                        JOptionPane.YES_NO_OPTION);
                    if (option != JOptionPane.YES_OPTION) return;
                }
                
                receptRepository.spara(nyttRecept);
                JOptionPane.showMessageDialog(this, "Receptet " + receptNamn + " har sparats!");
                
                // Rensa formuläret efter lyckad lagring
                nameField.setText("");
                nameField.setEditable(true);
                ingredientsArea.setText("");
                nuvarandeIngredienser.clear();
                recipeComboBoxModel.removeAllElements();
                
                // Läs in recepten igen
                läsInRecept();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Fel vid sparande: " + ex.getMessage());
            }
        });
        
        // TA BORT RECEPT
        deleteButton.addActionListener(e -> {
            String receptNamn = nameField.getText().trim();
            if (receptNamn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Välj ett recept att ta bort.");
                return;
            }
            
            int option = JOptionPane.showConfirmDialog(this, 
                "Är du säker på att du vill ta bort '" + receptNamn + "'?", 
                "Bekräfta borttagning", 
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                try {
                    receptRepository.ta_bort(receptNamn);
                    JOptionPane.showMessageDialog(this, "Receptet borttaget.");
                    
                    // Uppdatera gränssnittet
                    nameField.setText("");
                    ingredientsArea.setText("");
                    nuvarandeIngredienser.clear();
                    recipeComboBoxModel.removeAllElements();
                    
                    läsInRecept();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Fel vid borttagning: " + ex.getMessage());
                }
            }
        });
        
        // STÄNG FÖNSTRET
        backButton.addActionListener(e -> dispose());
    }

    private void läsInRecept() {
        try {
            Files.list(FileReceptRepository.DEFAULT_RECIPES_DIR)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(p -> {
                    String recipeName = p.getFileName().toString().replace(".json", "");
                    recipeComboBoxModel.addElement(recipeName);
                });
        } catch (IOException ex) {
            System.err.println("Kunde inte uppdatera receptlistan: " + ex.getMessage());
        }
    }
}

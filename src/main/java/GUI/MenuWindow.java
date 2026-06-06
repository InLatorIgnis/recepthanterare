package GUI;
import javax.swing.*;

import Recept.FileReceptRepository;
import Recept.ReceptFabrik;
import Veckomeny.FileVeckomenyRepository;
import Veckomeny.Veckomeny;
import Veckomeny.Veckomeny.VeckomenyPost;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MenuWindow extends JFrame {
    private FileVeckomenyRepository veckomenyRepository;
    private FileReceptRepository receptRepository;
    

    // Komponenter i gränssnittet

    private JComboBox<String> MenuComboBox;
    private List<String> veckodagar;
    private DefaultComboBoxModel<String> MenuComboBoxModel;
    private JTextField nameField;
    private JTextArea ingredientsArea;

   
    private List<JComboBox<String>> dayComboBoxes = new ArrayList<>();
    private List<JSpinner> daySpinners = new ArrayList<>();
    private List<JButton> dayRecommendButtons = new ArrayList<>();
    private JComboBox<String> MondayComboBox, TuesdayComboBox, WednesdayComboBox, ThursdayComboBox, FridayComboBox, SaturdayComboBox, SundayComboBox;
    private JSpinner MondaySpinner, TuesdaySpinner, WednesdaySpinner, ThursdaySpinner, FridaySpinner, SaturdaySpinner, SundaySpinner;
    private JButton MondayRecommendButton, TuesdayRecommendButton, WednesdayRecommendButton, ThursdayRecommendButton, FridayRecommendButton, SaturdayRecommendButton, SundayRecommendButton;
    private JButton NewMenuButton;
    private JButton saveButton;
    private JButton deleteButton;
    private JButton backButton;

    public MenuWindow(FileVeckomenyRepository veckomenyRepository , FileReceptRepository receptRepository) {

        this.veckomenyRepository = veckomenyRepository;
        this.receptRepository = receptRepository;
        dayComboBoxes.add(MondayComboBox);
        dayComboBoxes.add(TuesdayComboBox);
        dayComboBoxes.add(WednesdayComboBox);
        dayComboBoxes.add(ThursdayComboBox);
        dayComboBoxes.add(FridayComboBox);
        dayComboBoxes.add(SaturdayComboBox);
        dayComboBoxes.add(SundayComboBox);

        daySpinners.add(MondaySpinner);
        daySpinners.add(TuesdaySpinner);
        daySpinners.add(WednesdaySpinner);
        daySpinners.add(ThursdaySpinner);
        daySpinners.add(FridaySpinner);
        daySpinners.add(SaturdaySpinner);
        daySpinners.add(SundaySpinner);

        dayRecommendButtons.add(MondayRecommendButton);
        dayRecommendButtons.add(TuesdayRecommendButton);
        dayRecommendButtons.add(WednesdayRecommendButton);
        dayRecommendButtons.add(ThursdayRecommendButton);
        dayRecommendButtons.add(FridayRecommendButton);
        dayRecommendButtons.add(SaturdayRecommendButton);
        dayRecommendButtons.add(SundayRecommendButton);

        setTitle("Veckomenyhanterare");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Huvudpanel med BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- TOPPARREAN: Välja befintliga veckomenyer (Motsvarar CLI val 2) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Välj veckomeny:"));
        
        // Hämta veckomenynamn från ditt repository för att fylla rullgardinen
        MenuComboBoxModel = new DefaultComboBoxModel<>();
        läsInMenyer();
        MenuComboBox = new JComboBox<>(MenuComboBoxModel);
        topPanel.add(MenuComboBox);
        mainPanel.add(topPanel, BorderLayout.NORTH);


        JPanel centerPanel = new JPanel(new BorderLayout(4, 4));
        
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Veckomenyname:"));
        nameField = new JTextField(20);
        namePanel.add(nameField);
        centerPanel.add(namePanel, BorderLayout.NORTH);

         // Visningsyta för ingredienser
        ingredientsArea = new JTextArea(5, 29);
        ingredientsArea.setEditable(false); // Gör den skrivskyddad så rp.output() inte förstörs
        centerPanel.add(new JScrollPane(ingredientsArea), BorderLayout.WEST);

        // FORMULÄR FÖR ATT LÄGGA TILL RECEPT PÅ VECKOMENYN

        veckodagar = Arrays.asList("Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag");
        JPanel daysPanel = new JPanel(new GridLayout(7, 3, 5, 5));
        daysPanel.setBorder(BorderFactory.createTitledBorder("Välj recept för veckan"));
        for (int index = 0; index < veckodagar.size(); index++) {
            String dag = veckodagar.get(index);
            JComboBox<String> comboBox = new JComboBox<>(new DefaultComboBoxModel<>( ));
            läsInRecept(comboBox);

            JSpinner portionSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 50, 1)); // För att välja portioner
            
            JButton recommendButton = new JButton("🔍");
            recommendButton.setToolTipText("Rekommendera recept baserat på dina ingredienser");
            final int dayIndex = index;
            recommendButton.addActionListener(e -> openRecommendationWindow(dayIndex));

            dayComboBoxes.set(index, comboBox);
            daySpinners.set(index, portionSpinner);
            dayRecommendButtons.set(index, recommendButton);
            
            daysPanel.add(new JLabel(dag + ":"));
            daysPanel.add(comboBox);
            daysPanel.add(recommendButton);
            daysPanel.add(new JLabel("Portioner:"));
            daysPanel.add(portionSpinner);
            daysPanel.add(new JLabel("")); // placeholder for alignment
                        
        }


        centerPanel.add(daysPanel, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // --- BOTTENAREAN: Spara och ta bort knapp ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Spara helt veckomeny");
        deleteButton = new JButton("Ta bort veckomeny");
        backButton = new JButton("Stäng");
        NewMenuButton = new JButton("Ny veckomeny");
        bottomPanel.add(NewMenuButton);
        bottomPanel.add(saveButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(backButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setupActions(saveButton, deleteButton, backButton, NewMenuButton);
        setContentPane(mainPanel);
    }

    private void läsInRecept(JComboBox<String> recipeComboBox) {
        try {
            Files.list(FileReceptRepository.DEFAULT_RECIPES_DIR)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(p -> {
                    String recipeName = p.getFileName().toString().replace(".json", "");
                    recipeComboBox.addItem(recipeName);
                });
        } catch (IOException e) {
            System.err.println("Kunde inte läsa recept: " + e.getMessage());
        }
    }

    private void läsInMenyer() {
        try {
            Files.list(FileVeckomenyRepository.DEFAULT_WEEKMENUS_DIR)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(p -> {
                    String menuName = p.getFileName().toString().replace(".json", "");
                    MenuComboBoxModel.addElement(menuName);
                });
        } catch (IOException e) {
            System.err.println("Kunde inte läsa veckomeny: " + e.getMessage());
        }
    }

    private DayOfWeek toDayOfWeek(String dag) {
        return switch (dag.toLowerCase(Locale.ROOT).trim()) {
            case "måndag", "mandag" -> DayOfWeek.MONDAY;
            case "tisdag" -> DayOfWeek.TUESDAY;
            case "onsdag" -> DayOfWeek.WEDNESDAY;
            case "torsdag" -> DayOfWeek.THURSDAY;
            case "fredag" -> DayOfWeek.FRIDAY;
            case "lördag", "loradag" -> DayOfWeek.SATURDAY;
            case "söndag", "sondag" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Okänd veckodag: " + dag);
        };
    }

    private void openRecommendationWindow(int dayIndex) {
        String dag = veckodagar.get(dayIndex);
        MenuWindowRekomenderare recommender = new MenuWindowRekomenderare(receptRepository, dayComboBoxes.get(dayIndex), daySpinners.get(dayIndex), dag);
        recommender.setVisible(true);
    }

    private void setupActions(JButton saveButton, JButton deleteButton, JButton backButton, JButton NewMenuButton) {
        // LÄSA IN (Hämta befintligt)
        MenuComboBox.addActionListener(e -> {
            String valdMeny = (String) MenuComboBox.getSelectedItem();
            if (valdMeny == null || valdMeny.isEmpty()) return;
            
            nameField.setText(valdMeny);
            nameField.setEditable(false); // Lås namnet vid visning
            // Töm nuvarande meny innan vi lägger till den nya
            try {
                Veckomeny vp = veckomenyRepository.ladda(valdMeny);
                ingredientsArea.setText(vp.output());

            } catch (Exception ex) {
                ingredientsArea.setText("Kunde inte ladda veckomeny.");
                System.err.println("Fel vid inläsning: " + ex.getMessage());
            }
        });

        // NYTT RECEPT (Rensa formuläret för att skapa ett nytt recept)
        NewMenuButton.addActionListener(e -> {
            nameField.setText("");
            nameField.setEditable(true); // Gör namnfältet redigerbart för nytt recept
            ingredientsArea.setText("");
            MenuComboBox.setSelectedIndex(-1); // Avmarkera eventuellt valt recept
        });
        
        // SKRIVA TILL FIL (Spara hela receptet)
        saveButton.addActionListener(e -> {
            String menyNamn = nameField.getText().trim();
            if (menyNamn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ange ett menynamn.");
                return;
            }


            Veckomeny nyMeny = new Veckomeny(menyNamn);
            

            // Kontrollera varje dag direkt och lägg till om något är valt
            for (int i = 0; i < veckodagar.size(); i++) {
                JComboBox<String> comboBox = dayComboBoxes.get(i);
                JSpinner portionSpinner = daySpinners.get(i);
                int portioner = (int) portionSpinner.getValue();
                String receptNamn = (String) comboBox.getSelectedItem();
                String dag = veckodagar.get(i);
                try {
                    if (receptNamn != null && !receptNamn.isEmpty()) {
                        ReceptFabrik recept = receptRepository.ladda(receptNamn);
                        DayOfWeek dayOfWeek = toDayOfWeek(dag);
                        nyMeny.setReceptForDag(dayOfWeek, new VeckomenyPost(recept, portioner));
                    }
                } catch (Exception e1) {
                    System.err.println("Kunde inte ladda recept: " + e1.getMessage());
                }
            }
            

            try {
                if (veckomenyRepository.exists(menyNamn)) {
                    int option = JOptionPane.showConfirmDialog(this,
                            "En veckomeny med namnet '" + menyNamn + "' finns redan. Vill du ersätta den?",
                            "Bekräfta",
                            JOptionPane.YES_NO_OPTION);
                    if (option != JOptionPane.YES_OPTION) return;
                }
                veckomenyRepository.spara(nyMeny);
                JOptionPane.showMessageDialog(this, "Veckomeny sparad!");
                MenuComboBoxModel.removeAllElements();
                läsInMenyer();
            } catch (Exception ex) {
                System.err.println("Fel vid sparning: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Fel vid sparning: " + ex.getMessage());
            }
        });
        // TA BORT VECKOMENY
        deleteButton.addActionListener(e -> {
            String veckomenyNamn = nameField.getText().trim();
            if (veckomenyNamn.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Välj en veckomeny att ta bort.");
                return;
            }
            
            int option = JOptionPane.showConfirmDialog(this, 
                "Är du säker på att du vill ta bort '" + veckomenyNamn + "'?", 
                "Bekräfta borttagning", 
                JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                try {
                    veckomenyRepository.ta_bort(veckomenyNamn);
                    JOptionPane.showMessageDialog(this, "Veckomeny borttagen.");
                    
                    // Uppdatera gränssnittet
                    nameField.setText("");
                    ingredientsArea.setText("");
                    
                    MenuComboBoxModel.removeAllElements();
                    
                    läsInMenyer();
                } catch (Exception ex) {
                    System.err.println("Fel vid borttagning: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, "Fel vid borttagning: " + ex.getMessage());
                }
            }
        });
        
        // STÄNG FÖNSTRET
        backButton.addActionListener(e -> dispose());

    }    

}

package GUI;

import javax.swing.*;

import Butikslayout.ButiksLayout;
import Butikslayout.FileButiksLayoutRepository;
import Recept.FileReceptRepository;
import Veckomeny.FileVeckomenyRepository;
import Veckomeny.Veckomeny;
import main.Inköpslista;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RecepthanterareGUI extends JFrame {
    private FileButiksLayoutRepository layoutRepository;
    private FileVeckomenyRepository veckomenyRepository;
    private FileReceptRepository receptRepository;
    private JComboBox<String> butikCombo;
    private JComboBox<String> menyCombo;
    private JTextArea resultArea;
    private JButton genereraButton;
    private JButton sparaButton;

    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem newJMenuItem;
    private JMenuItem exitMenuItem;

    private String currentOutput = "";

    public RecepthanterareGUI() {

        setTitle("Recepthanterare");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        try {
            layoutRepository = new FileButiksLayoutRepository(FileButiksLayoutRepository.DEFAULT_LAYOUTS_DIR);
            veckomenyRepository = new FileVeckomenyRepository(FileVeckomenyRepository.DEFAULT_WEEKMENUS_DIR);
            receptRepository = new FileReceptRepository(FileReceptRepository.DEFAULT_RECIPES_DIR);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Fel vid initialisering: " + e.getMessage(), "Fel",
                    JOptionPane.ERROR_MESSAGE);
        }

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel for selections
        JPanel selectPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        selectPanel.setBorder(BorderFactory.createTitledBorder("Inköpslista Generator"));

        // layoutCombo is unused in the current UI; keep repositories and combos for
        // butik/meny

        // Skapa menykomponenter
        menuBar = new JMenuBar();
        fileMenu = new JMenu("Verktyg");
        newJMenuItem = new JMenuItem("Nytt");
        exitMenuItem = new JMenuItem("Avsluta");

        // Alternativen för dialogrutan
        String[] alternativ = { "Butikslayout", "Veckomeny", "Recept" };

        // Lyssnare för "Nytt" som visar valmöjligheterna i en pop-up
        newJMenuItem.addActionListener(e -> {
            String val = (String) JOptionPane.showInputDialog(
                    this,
                    "Välj vad du vill skapa:",
                    "Skapa nytt",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    alternativ,
                    alternativ[0]);

            if (val != null) {
                switch (val) {
                    case "Butikslayout" -> {
                        LayoutWindow layoutWindow = new LayoutWindow(layoutRepository);
                        layoutWindow.setVisible(true);
                    }
                    case "Veckomeny" -> {
                        MenuWindow menuWindow = new MenuWindow(veckomenyRepository, receptRepository);
                        menuWindow.setVisible(true);
                    }
                    case "Recept" -> {
                        RecipeWindow recipeWindow = new RecipeWindow(receptRepository);
                        recipeWindow.setVisible(true);
                    }

                }
            }
        });

        // Avsluta-knappens logik
        exitMenuItem.addActionListener(e -> dispose());

        // Bygg menyn
        fileMenu.add(newJMenuItem);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        selectPanel.add(new JLabel("Välj butik:"));
        butikCombo = new JComboBox<>();
        selectPanel.add(butikCombo);

        selectPanel.add(new JLabel("Välj veckomeny:"));
        menyCombo = new JComboBox<>();
        selectPanel.add(menyCombo);

        genereraButton = new JButton("Generera inköpslista");
        genereraButton.addActionListener(e -> genereraInköpslista());
        selectPanel.add(genereraButton);

        sparaButton = new JButton("Spara som fil");
        sparaButton.setEnabled(false);
        sparaButton.addActionListener(e -> sparaFil());
        selectPanel.add(sparaButton);

        // Populate combos
        loadButikerCombo();
        loadMenyerCombo();

        mainPanel.add(selectPanel, BorderLayout.NORTH);

        // Text area for result
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Inköpslista"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Uppdatera");
        refreshButton.addActionListener(e -> {
            loadButikerCombo();
            loadMenyerCombo();
        });
        mainPanel.add(refreshButton, BorderLayout.EAST);

        add(mainPanel);
    }

    private void loadButikerCombo() {
        SwingUtilities.invokeLater(() -> {
            try {
                butikCombo.removeAllItems();
                Path layoutDir = FileButiksLayoutRepository.DEFAULT_LAYOUTS_DIR;
                if (Files.exists(layoutDir)) {
                    Files.list(layoutDir)
                            .filter(p -> p.toString().endsWith(".json"))
                            .forEach(p -> {
                                String fileName = p.getFileName().toString();
                                butikCombo.addItem(fileName.substring(0, fileName.length() - 5));
                            });
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Fel vid läsning av butiker: " + e.getMessage(), "Fel",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void loadMenyerCombo() {
        SwingUtilities.invokeLater(() -> {
            try {
                menyCombo.removeAllItems();
                Path menyDir = FileVeckomenyRepository.DEFAULT_WEEKMENUS_DIR;
                if (Files.exists(menyDir)) {
                    Files.list(menyDir)
                            .filter(p -> p.toString().endsWith(".json"))
                            .forEach(p -> {
                                String fileName = p.getFileName().toString();
                                menyCombo.addItem(fileName.substring(0, fileName.length() - 5));
                            });
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Fel vid läsning av menyer: " + e.getMessage(), "Fel",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void genereraInköpslista() {
        String selectedButik = (String) butikCombo.getSelectedItem();
        String selectedMeny = (String) menyCombo.getSelectedItem();

        if (selectedButik == null || selectedButik.isEmpty() || selectedMeny == null || selectedMeny.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vänligen välj både butik och veckomeny.", "Val saknas",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ButiksLayout layout = layoutRepository.ladda(selectedButik);
            Veckomeny veckomeny = veckomenyRepository.ladda(selectedMeny);

            Inköpslista inköpslista = new Inköpslista(selectedMeny, layout, veckomeny);
            inköpslista.aggregateIngredients(receptRepository);

            currentOutput = inköpslista.output();
            resultArea.setText(currentOutput);
            sparaButton.setEnabled(true);

            JOptionPane.showMessageDialog(this, "Inköpslista genererad!", "Framgång", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fel vid generering: " + e.getMessage(), "Fel",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sparaFil() {
        if (currentOutput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Det finns ingen inköpslista att spara.", "Fel",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedMeny = (String) menyCombo.getSelectedItem();
        try {
            Path outputDir = Inköpslista.DEFAULT_SHOPPINGLIST_DIR;
            Files.createDirectories(outputDir);

            Path filePath = outputDir.resolve(selectedMeny + "_inköpslista.txt");
            Files.writeString(filePath, currentOutput);

            JOptionPane.showMessageDialog(this, "Inköpslista sparad till:\n" + filePath.toAbsolutePath(), "Framgång",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Fel vid sparning: " + e.getMessage(), "Fel",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RecepthanterareGUI());
    }
}

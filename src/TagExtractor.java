import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class TagExtractor extends JFrame {
    private JTextArea outputArea;
    private JFileChooser fileChooser;
    private File textFile, stopWordsFile;
    private Set<String> stopWords;

    public TagExtractor() {
        setTitle("Tag Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // JTextArea to display output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        // Panel with buttons
        JPanel buttonPanel = new JPanel();
        JButton chooseTextFileButton = new JButton("Choose Text File");
        JButton chooseStopWordsFileButton = new JButton("Choose Stop Words File");
        JButton extractTagsButton = new JButton("Extract Tags");
        JButton saveToFileButton = new JButton("Save to File");

        buttonPanel.add(chooseTextFileButton);
        buttonPanel.add(chooseStopWordsFileButton);
        buttonPanel.add(extractTagsButton);
        buttonPanel.add(saveToFileButton);
        add(buttonPanel, BorderLayout.SOUTH);

        fileChooser = new JFileChooser();

        // Action listeners
        chooseTextFileButton.addActionListener(e -> chooseFile(true));
        chooseStopWordsFileButton.addActionListener(e -> chooseFile(false));
        extractTagsButton.addActionListener(e -> extractTags());
        saveToFileButton.addActionListener(e -> saveToFile());

        stopWords = new HashSet<>();
    }

    private void chooseFile(boolean isTextFile) {
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (isTextFile) {
                textFile = fileChooser.getSelectedFile();
                outputArea.append("Selected text file: " + textFile.getName() + "\n");
            } else {
                stopWordsFile = fileChooser.getSelectedFile();
                outputArea.append("Selected stop words file: " + stopWordsFile.getName() + "\n");
                loadStopWords();
            }
        }
    }

    private void loadStopWords() {
        if (stopWordsFile == null) {
            outputArea.append("No stop words file selected.\n");
            return;
        }
        stopWords.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(stopWordsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line.trim().toLowerCase());
            }
            outputArea.append("Loaded " + stopWords.size() + " stop words.\n");
        } catch (IOException e) {
            outputArea.append("Error loading stop words: " + e.getMessage() + "\n");
        }
    }

    private void extractTags() {
        if (textFile == null) {
            outputArea.append("No text file selected.\n");
            return;
        }
        if (stopWords.isEmpty()) {
            outputArea.append("No stop words loaded.\n");
            return;
        }

        Map<String, Integer> wordFrequency = new TreeMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(textFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.toLowerCase().replaceAll("[^a-zA-Z\\s]", "").split("\\s+");
                for (String word : words) {
                    if (!word.isEmpty() && !stopWords.contains(word)) {
                        wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                    }
                }
            }
        } catch (IOException e) {
            outputArea.append("Error reading text file: " + e.getMessage() + "\n");
            return;
        }

        outputArea.append("Tag extraction complete:\n");
        outputArea.append("Word - Frequency\n");
        for (Entry<String, Integer> entry : wordFrequency.entrySet()) {
            outputArea.append(entry.getKey() + " - " + entry.getValue() + "\n");
        }
    }

    private void saveToFile() {
        if (textFile == null) {
            outputArea.append("No data to save.\n");
            return;
        }
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(saveFile))) {
                writer.println("Word - Frequency");
                // Reconstruct the word frequency map
                String[] outputLines = outputArea.getText().split("\n");
                for (String line : outputLines) {
                    writer.println(line);
                }
                outputArea.append("Saved to file: " + saveFile.getName() + "\n");
            } catch (IOException e) {
                outputArea.append("Error saving to file: " + e.getMessage() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TagExtractor extractor = new TagExtractor();
            extractor.setVisible(true);
        });
    }
}
